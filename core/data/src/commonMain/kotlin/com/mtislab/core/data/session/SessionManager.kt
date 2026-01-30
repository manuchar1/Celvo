package com.mtislab.core.data.session

import com.mtislab.core.domain.auth.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SessionManager(
    private val tokenStorage: TokenStorage,
    private val supabase: SupabaseClient,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var cachedAccessToken: String? = null
    private var cachedRefreshToken: String? = null

    val state: StateFlow<AuthState> = combine(
        tokenStorage.getAccessToken(),
        tokenStorage.getRefreshToken(),
        tokenStorage.getUserId()
    ) { accessToken, refreshToken, userId ->
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken

        if (accessToken != null && userId != null) {
            AuthState.Authenticated(userId)
        } else {
            AuthState.Guest
        }
    }.stateIn(scope, SharingStarted.Eagerly, AuthState.Initial)

    fun onLoginSuccess(accessToken: String, refreshToken: String, userId: String) {
        scope.launch {
            tokenStorage.saveSession(accessToken, refreshToken, userId)
        }
    }

    fun logout() {
        scope.launch {
            try {
                supabase.auth.signOut()
            } catch (e: Exception) {
            }
            tokenStorage.clearSession()
        }
    }


    suspend fun refreshSession(): Pair<String, String>? {
        return try {
            supabase.auth.refreshCurrentSession()

            val session = supabase.auth.currentSessionOrNull() ?: return null

            val newAccess = session.accessToken
            val newRefresh = session.refreshToken
            val userId = session.user?.id ?: return null

            tokenStorage.saveSession(newAccess, newRefresh, userId)

            newAccess to newRefresh
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAccessToken(): String? = cachedAccessToken
    fun getRefreshToken(): String? = cachedRefreshToken
}