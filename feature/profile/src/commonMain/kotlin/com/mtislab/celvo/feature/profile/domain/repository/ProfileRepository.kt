package com.mtislab.celvo.feature.profile.domain.repository

import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface ProfileRepository {
    suspend fun getUserProfile(): Resource<UserProfile, DataError.Remote>
}