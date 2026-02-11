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

            // შეცვლილი: გავზარდეთ Timeout 60 წამამდე, რომ "Request timeout" არ მივიღოთ
            // სანამ რეფრეში მიმდინარეობს
            install(HttpTimeout) {
                socketTimeoutMillis = 60_000L
                requestTimeoutMillis = 60_000L
                connectTimeoutMillis = 60_000L
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        // Debug ლეველი საკმარისია, რომ ლოგები არ გადაიტვირთოს
                        celvoLogger.debug(message)
                    }
                }
                level = LogLevel.ALL
            }

            install(WebSockets) { pingIntervalMillis = 20_000L }

            install(Auth) {
                bearer {
                    loadTokens {
                        // აქ ვიყენებთ განახლებულ SessionManager-ს (suspend ფუნქციებს)
                        val access = sessionManager.getAccessToken()
                        val refresh = sessionManager.getRefreshToken()

                        if (access != null && refresh != null) {
                            BearerTokens(access, refresh)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        // კრიტიკული ცვლილება: Try-Catch და ლოგირება
                        try {
                            celvoLogger.debug("Auth: 🔄 Starting token refresh...")

                            val newTokens = sessionManager.refreshSession()

                            if (newTokens != null) {
                                celvoLogger.info("Auth: ✅ Token refreshed successfully")
                                BearerTokens(newTokens.first, newTokens.second)
                            } else {
                                celvoLogger.warn("Auth: ⚠️ Refresh returned null. Force logout needed.")
                                null
                            }
                        } catch (e: Exception) {
                            // აუცილებელია Exception-ის დაჭერა, თორემ Ktor-ი გაიჭედება და Timeout-ს ისვრის
                            celvoLogger.error("Auth: ❌ Error during refresh", e)
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