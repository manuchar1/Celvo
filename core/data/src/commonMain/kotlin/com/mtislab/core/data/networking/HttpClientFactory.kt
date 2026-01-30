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
                json(Json { ignoreUnknownKeys = true; prettyPrint = true; isLenient = true })
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        celvoLogger.debug(message)
                    }
                }
                level = LogLevel.ALL
            }
            install(WebSockets) { pingIntervalMillis = 20_000L }

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
                        val newTokens = sessionManager.refreshSession()
                        if (newTokens != null) {
                            BearerTokens(newTokens.first, newTokens.second)
                        } else {
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
}