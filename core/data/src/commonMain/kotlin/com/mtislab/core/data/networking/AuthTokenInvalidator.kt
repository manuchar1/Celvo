package com.mtislab.core.data.networking

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

/**
 * Drops the auth token that the Ktor client keeps cached in memory.
 *
 * Ktor's `Auth` (bearer) plugin invokes `loadTokens` only once and then reuses
 * the result for the whole lifetime of the [HttpClient] singleton. Without an
 * explicit invalidation a logout / login-as-a-different-user would keep sending
 * the previous user's token. SessionManager calls [invalidate] on every session
 * transition so the next request re-reads the current token from storage.
 */
interface AuthTokenInvalidator {
    fun invalidate()
}

/**
 * [AuthTokenInvalidator] backed by the Ktor bearer auth provider.
 *
 * The [HttpClient] is supplied lazily through a provider lambda on purpose:
 * the client itself depends on SessionManager, so resolving it eagerly here
 * would create a dependency cycle. The lambda is invoked only inside
 * [invalidate] — long after the object graph has finished building.
 */
class KtorAuthTokenInvalidator(
    private val httpClient: () -> HttpClient,
) : AuthTokenInvalidator {

    override fun invalidate() {
        httpClient().authProvider<BearerAuthProvider>()?.clearToken()
    }
}
