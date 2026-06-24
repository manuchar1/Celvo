package com.mtislab.celvo.feature.profile.data.remote

import com.mtislab.celvo.feature.profile.data.dto.AccountDeletionErrorDto
import com.mtislab.celvo.feature.profile.data.dto.ProfileDto
import com.mtislab.celvo.feature.profile.domain.model.DeleteAccountResult
import com.mtislab.core.data.networking.constructRoute
import com.mtislab.core.data.networking.get
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.url

class ProfileService(private val httpClient: HttpClient) {

    suspend fun getUserProfile(): Resource<ProfileDto, DataError.Remote> {
        return httpClient.get(route = "/api/users/me")
    }

    suspend fun deleteAccount(): DeleteAccountResult {
        return try {
            val response = httpClient.delete {
                url(constructRoute("/api/users/me"))
                header(CONFIRM_DELETION_HEADER, CONFIRM_DELETION_VALUE)
            }
            when (response.status.value) {
                in 200..299 -> DeleteAccountResult.Success
                401 -> DeleteAccountResult.SessionExpired
                502 -> {
                    val errorCode = runCatching { response.body<AccountDeletionErrorDto>().error }.getOrNull()
                    if (errorCode == AUTH_PROVIDER_UNAVAILABLE) {
                        DeleteAccountResult.AuthProviderUnavailable
                    } else {
                        DeleteAccountResult.Retryable
                    }
                }
                500, 503, 504 -> DeleteAccountResult.Retryable
                else -> DeleteAccountResult.Generic
            }
        } catch (_: Throwable) {
            DeleteAccountResult.Retryable
        }
    }

    private companion object {
        const val CONFIRM_DELETION_HEADER = "X-Confirm-Deletion"
        const val CONFIRM_DELETION_VALUE = "I-UNDERSTAND-THIS-IS-PERMANENT"
        const val AUTH_PROVIDER_UNAVAILABLE = "AUTH_PROVIDER_UNAVAILABLE"
    }
}