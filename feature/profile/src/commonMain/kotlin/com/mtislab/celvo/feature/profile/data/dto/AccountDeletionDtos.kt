package com.mtislab.celvo.feature.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountDeletionResponseDto(
    val userId: String,
    val ordersAnonymised: Int,
    val paymentsAnonymised: Int,
    val walletRowsPurged: Int,
    val promoRowsPurged: Int,
    val deletedAt: String
)

@Serializable
data class AccountDeletionErrorDto(
    val error: String,
    val message: String,
    val timestamp: String
)
