package com.mtislab.celvo.feature.profile.data.remote

import com.mtislab.celvo.feature.profile.data.dto.ProfileDto
import com.mtislab.core.data.networking.get
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import io.ktor.client.HttpClient

class ProfileService(private val httpClient: HttpClient) {

    suspend fun getUserProfile(): Resource<ProfileDto, DataError.Remote> {
        return httpClient.get(route = "/api/users/me")
    }
}