package com.mtislab.celvo.feature.profile.presentation

import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import com.mtislab.core.domain.utils.DataError

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: DataError? = null
)