package com.mtislab.core.domain.model

data class AuthData(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)