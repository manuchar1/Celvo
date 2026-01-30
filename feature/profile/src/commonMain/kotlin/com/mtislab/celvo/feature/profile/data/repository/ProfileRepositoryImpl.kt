package com.mtislab.celvo.feature.profile.data.repository

import com.mtislab.celvo.feature.profile.data.dto.toDomain
import com.mtislab.celvo.feature.profile.data.remote.ProfileService
import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import com.mtislab.celvo.feature.profile.domain.repository.ProfileRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.domain.utils.map

class ProfileRepositoryImpl(
    private val api: ProfileService
) : ProfileRepository {

    override suspend fun getUserProfile(): Resource<UserProfile, DataError.Remote> {
        return api.getUserProfile().map { dto ->
            dto.toDomain()
        }
    }
}