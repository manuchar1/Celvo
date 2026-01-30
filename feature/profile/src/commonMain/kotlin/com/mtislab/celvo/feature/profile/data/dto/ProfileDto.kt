package com.mtislab.celvo.feature.profile.data.dto

import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val email: String,
    val fullName: String?,
    val createdAt: String
)


fun ProfileDto.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        email = email,
        fullName = fullName ?: "User",
        createdAt = createdAt
    )
}