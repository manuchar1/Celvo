package com.mtislab.core.data.repository

import com.mtislab.core.data.networking.safeSupabaseCall
import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.repository.AuthRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    // ── Android: native Credential Manager (synchronous ID-token) ──────
    override suspend fun signInWithGoogle(idToken: String): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
            }
            fetchAuthData()
        }
    }

    // ── iOS: web-based OAuth (asynchronous redirect) ───────────────────
    override suspend fun signInWithGoogleWeb(): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {
            // signInWith(Google) opens the browser and returns immediately.
            // The actual session is established only after the deep-link
            // callback is processed by supabase.handleDeepLinks(url).
            supabase.auth.signInWith(Google)

            // Wait until Supabase Auth emits an Authenticated status
            // (triggered when AuthDeepLinkBridge forwards the callback URL).
            awaitAuthenticated()
            fetchAuthData()
        }
    }

    // ── iOS: Apple web-based OAuth (same async pattern) ────────────────
    // NOTE: When you implement native Apple Sign-In via ASAuthorization,
    // create a separate signInWithAppleNative(idToken) method that mirrors
    // the Android/Google IDToken path. Keep this web fallback for devices
    // where native Apple auth is unavailable.
    override suspend fun signInWithApple(): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {
            supabase.auth.signInWith(Apple)
            awaitAuthenticated()
            fetchAuthData()
        }
    }

    // ── iOS: native Apple Sign-In (IDToken — no browser redirect) ─────
    // TODO: Wire this up when you implement ASAuthorization on the iOS side.
    // The ViewModel should call this instead of signInWithApple() once
    // the native AppleAuthProvider is ready.
    override suspend fun signInWithAppleNative(idToken: String): Resource<AuthData, DataError.Remote> {
        return safeSupabaseCall {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Apple
            }
            fetchAuthData()
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    // ── Private helpers ────────────────────────────────────────────────

    /**
     * Suspends until Supabase Auth reports [SessionStatus.Authenticated].
     *
     * This bridges the gap between the browser redirect (fire-and-forget)
     * and the actual session creation that happens asynchronously when the
     * deep-link callback is processed.
     *
     * Times out after 120 seconds so the coroutine doesn't hang forever
     * if the user abandons the browser flow.
     */
    private suspend fun awaitAuthenticated() {
        withTimeout(120_000L) {
            supabase.auth.sessionStatus.first { status ->
                status is SessionStatus.Authenticated
            }
        }
    }

    private suspend fun fetchAuthData(): AuthData {
        val session = supabase.auth.currentSessionOrNull()
            ?: throw IllegalStateException("Session not found after login")

        val user = session.user
            ?: throw IllegalStateException("User not found in session")

        return AuthData(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            userId = user.id
        )
    }
}