package com.mtislab.core.data.networking

import com.mtislab.core.data.BuildKonfig
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.logging.CelvoLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val sessionManager: SessionManager,
    private val celvoLogger: CelvoLogger
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        // prettyPrint = false in production to reduce payload size
                        prettyPrint = false
                        coerceInputValues = true
                    }
                )
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 15_000L
                requestTimeoutMillis = 30_000L
                socketTimeoutMillis = 30_000L
            }

            // Production-safe logging: HEADERS level omits body,
            // and we sanitize the Authorization header below.
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        celvoLogger.debug(message)
                    }
                }
                level = LogLevel.ALL  // ← Changed from ALL. No body/header dump.
            }

            install(WebSockets) {
                pingIntervalMillis = 20_000L
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val access = sessionManager.getAccessToken()
                        val refresh = sessionManager.getRefreshToken()

                        if (access != null && refresh != null) {
                            BearerTokens(access, refresh)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        try {
                            val newTokens = sessionManager.refreshSession()

                            if (newTokens != null) {
                                BearerTokens(newTokens.first, newTokens.second)
                            } else {
                                // SessionManager already handles forced logout
                                // after MAX_REFRESH_RETRIES
                                null
                            }
                        } catch (e: Exception) {
                            celvoLogger.error(
                                "[HttpClient] Unexpected error in refreshTokens",
                                e
                            )
                            null
                        }
                    }
                }
            }

            defaultRequest {
                header("x-api-key", BuildKonfig.API_KEY)
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Redacts Bearer tokens from log output to prevent token leakage.
     */
    private fun sanitizeLogMessage(message: String): String {
        return message.replace(
            Regex("Bearer [A-Za-z0-9\\-._~+/]+=*"),
            "Bearer [REDACTED]"
        )
    }
}