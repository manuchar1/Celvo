package com.mtislab.core.data.auth

import com.mtislab.core.domain.utils.DeepLinkHandler
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseSessionFromUrl
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.http.Url
import io.ktor.http.parseQueryString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Listens to [DeepLinkHandler.events] and forwards OAuth callback URLs
 * to the Supabase Auth SDK.
 *
 * Supports both:
 *  - **PKCE flow** — callback URL has `?code=XXXX` → [exchangeCodeForSession]
 *  - **Implicit flow** — callback URL has `#access_token=...` → [parseSessionFromFragment] + [importSession]
 *
 * No changes needed on the Swift side — `iOSApp.swift` already calls
 * `DeepLinkHandler.handleDeepLink(url:)` for every incoming URL.
 * This class simply subscribes to that same event stream.
 *
 * On Android this is effectively a no-op (Credential Manager doesn't
 * use deep-link callbacks for Google auth).
 */
class SupabaseDeepLinkHandler(
    private val supabase: SupabaseClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Start collecting deep-link events. Call once during app init.
     */
    fun startListening() {
        scope.launch {
            DeepLinkHandler.events.collect { url ->
                handleUrl(url)
            }
        }
    }

    private suspend fun handleUrl(url: String) {
        try {
            // Only process auth-related callbacks
            if (!url.contains("login-callback")) return

            println("[SupabaseDeepLinkHandler] Processing auth callback: $url")

            val parsedUrl = Url(url)
            val fragment = parsedUrl.fragment
            val queryParams = parsedUrl.parameters

            // ── Scenario 1: PKCE flow — ?code=XXXX ──────────────────────
            val code = queryParams["code"]
                ?: parseQueryString(fragment)["code"]

            if (!code.isNullOrBlank()) {
                supabase.auth.exchangeCodeForSession(code)
                println("[SupabaseDeepLinkHandler] PKCE: exchanged code for session")
                return
            }

            // ── Scenario 2: Implicit flow — #access_token=...&refresh_token=... ──
            if (fragment.isNotBlank() && fragment.contains("access_token")) {
                val fragmentParams = parseQueryString(fragment)
                val accessToken = fragmentParams["access_token"]
                val refreshToken = fragmentParams["refresh_token"]
                val expiresIn = fragmentParams["expires_in"]?.toLongOrNull()
                val tokenType = fragmentParams["token_type"]

                if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                    supabase.auth.importSession(
                        UserSession(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            expiresIn = expiresIn ?: 3600,
                            tokenType = tokenType ?: "bearer",
                            user = null
                        ),
                       // source = io.github.jan.supabase.auth.user.UserSessionSource.External
                    )
                    // Refresh to get full user data
                    supabase.auth.refreshCurrentSession()
                    println("[SupabaseDeepLinkHandler] Implicit: imported session from fragment")
                    return
                }
            }

            println("[SupabaseDeepLinkHandler] No auth params found in callback URL")
        } catch (e: Exception) {
            println("[SupabaseDeepLinkHandler] Failed to handle URL: $url — ${e.message}")
            e.printStackTrace()
        }
    }
}
