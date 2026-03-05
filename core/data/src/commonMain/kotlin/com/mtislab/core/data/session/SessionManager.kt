package com.mtislab.core.data.session

import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.auth.SessionController
import com.mtislab.core.domain.auth.SessionEvent
import com.mtislab.core.domain.logging.CelvoLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Central session orchestrator. Implements [SessionController].
 *
 * Key behaviors:
 * - Emits [SessionEvent.UserChanged] on every successful login
 * - Emits [SessionEvent.LoggedOut] on every logout (voluntary or forced)
 * - ViewModels observe [sessionEvents] and reset their cached state
 */
class SessionManager(
    private val tokenStorage: TokenStorage,
    private val supabase: SupabaseClient,
    private val logger: CelvoLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : SessionController {

    private val refreshMutex = Mutex()

    // --- Session Event Bus ---
    private val _sessionEvents = MutableSharedFlow<SessionEvent>(
        extraBufferCapacity = 1  // Ensures emit doesn't suspend even if no collector yet
    )
    override val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    // --- Refresh Rate Limiting ---
    private var lastRefreshAttempt: ComparableTimeMark? = null
    private var consecutiveRefreshFailures: Int = 0

    override val state: StateFlow<AuthState> = combine(
        tokenStorage.getAccessToken(),
        tokenStorage.getRefreshToken(),
        tokenStorage.getUserId()
    ) { accessToken, _, userId ->
        when {
            accessToken != null && userId != null -> AuthState.Authenticated(userId)
            else -> AuthState.Guest
        }
    }.stateIn(scope, SharingStarted.Eagerly, AuthState.Initial)

    // ─────────────────────────────────────────────
    // Public API (SessionController)
    // ─────────────────────────────────────────────

    override suspend fun onLoginSuccess(
        accessToken: String,
        refreshToken: String,
        userId: String
    ) {
        tokenStorage.saveSession(accessToken, refreshToken, userId)
        consecutiveRefreshFailures = 0
        logger.info("[SessionManager] Session saved for userId=$userId")

        _sessionEvents.emit(SessionEvent.UserChanged(userId))
        logger.info("[SessionManager] Emitted SessionEvent.UserChanged($userId)")
    }

    override suspend fun logout() {
        logger.info("[SessionManager] Logging out...")
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            logger.warn("[SessionManager] Supabase signOut failed (non-fatal) $e" )
        }
        tokenStorage.clearSession()
        consecutiveRefreshFailures = 0
        logger.info("[SessionManager] Local session cleared")

        // 🔑 Signal all ViewModels to clear user-specific caches
        _sessionEvents.emit(SessionEvent.LoggedOut)
        logger.info("[SessionManager] Emitted SessionEvent.LoggedOut")
    }

    override fun isAuthenticated(): Boolean {
        return state.value is AuthState.Authenticated
    }

    // ─────────────────────────────────────────────
    // Internal — Used by HttpClientFactory Only
    // ─────────────────────────────────────────────

    internal suspend fun getAccessToken(): String? {
        return tokenStorage.getAccessToken().first()
    }

    internal suspend fun getRefreshToken(): String? {
        return tokenStorage.getRefreshToken().first()
    }

    internal suspend fun refreshSession(): Pair<String, String>? {
        return refreshMutex.withLock {
            // --- Rate Limiting ---
            val cooldown = calculateCooldown()
            val lastAttempt = lastRefreshAttempt
            if (lastAttempt != null && lastAttempt.elapsedNow() < cooldown) {
                logger.warn(
                    "[SessionManager] Refresh cooldown active " +
                            "(${cooldown}). Skipping."
                )
                return@withLock null
            }
            lastRefreshAttempt = TimeSource.Monotonic.markNow()

            try {
                logger.debug("[SessionManager] Starting token refresh...")

                supabase.auth.refreshCurrentSession()

                val session = supabase.auth.currentSessionOrNull()
                if (session == null) {
                    logger.warn("[SessionManager] Refresh returned null session")
                    handleRefreshFailure()
                    return@withLock null
                }

                val newAccess = session.accessToken
                val newRefresh = session.refreshToken
                val userId = session.user?.id

                if (userId == null) {
                    logger.warn("[SessionManager] Refreshed session has no userId")
                    handleRefreshFailure()
                    return@withLock null
                }

                tokenStorage.saveSession(newAccess, newRefresh, userId)
                consecutiveRefreshFailures = 0

                logger.info("[SessionManager] Token refreshed successfully")
                newAccess to newRefresh
            } catch (e: Exception) {
                logger.error("[SessionManager] Token refresh failed", e)
                handleRefreshFailure()
                null
            }
        }
    }

    // ─────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────

    private fun calculateCooldown(): Duration {
        if (consecutiveRefreshFailures == 0) return Duration.ZERO
        val backoffSeconds = minOf(
            (1L shl consecutiveRefreshFailures),
            60L
        )
        return backoffSeconds.seconds
    }

    private suspend fun handleRefreshFailure() {
        consecutiveRefreshFailures++

        if (consecutiveRefreshFailures >= MAX_REFRESH_RETRIES) {
            logger.warn(
                "[SessionManager] Max refresh retries ($MAX_REFRESH_RETRIES) reached. " +
                        "Forcing logout."
            )
            tokenStorage.clearSession()
            consecutiveRefreshFailures = 0
            _sessionEvents.emit(SessionEvent.LoggedOut)
        }
    }

    private companion object {
        const val MAX_REFRESH_RETRIES = 3
    }
}