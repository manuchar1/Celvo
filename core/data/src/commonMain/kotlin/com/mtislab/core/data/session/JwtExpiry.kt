package com.mtislab.core.data.session

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Lightweight JWT expiry inspection — no signature verification, we only need
 * the `exp` claim to know whether the backend would reject the token anyway.
 *
 * Used to refresh a stale access token BEFORE the first request after long
 * inactivity, instead of letting every startup call fail with 401.
 */
internal object JwtExpiry {

    /** Safety margin so a token about to expire mid-request counts as expired. */
    private const val EXPIRY_MARGIN_SECONDS = 30L

    @OptIn(ExperimentalTime::class)
    fun isExpired(accessToken: String): Boolean {
        val exp = expirationEpochSeconds(accessToken) ?: return false
        return exp <= Clock.System.now().epochSeconds + EXPIRY_MARGIN_SECONDS
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun expirationEpochSeconds(jwt: String): Long? {
        return try {
            val payload = jwt.split(".").getOrNull(1) ?: return null
            val json = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                .decode(payload)
                .decodeToString()
            Json.parseToJsonElement(json).jsonObject["exp"]?.jsonPrimitive?.longOrNull
        } catch (_: Exception) {
            // Malformed token — let the server be the judge.
            null
        }
    }
}
