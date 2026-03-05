package com.mtislab.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ActiveEsim(
    val id: String,
    val iccid: String,
    val status: String,
    val statusDisplayName: String,
    val packageName: String,
    val bundleSku: String,
    val userLabel: String?,
    val country: EsimCountry,
    val dataUsage: DataUsage,
    val validity: Validity,
    val purchaseDate: String,
    val purchaseDateFormatted: String,
    val canTopUp: Boolean,
    val autoRenewalEnabled: Boolean
)

@Serializable
data class EsimCountry(
    val code: String,
    val name: String,
    val flagUrl: String,
    val isRegion: Boolean
)

@Serializable
data class DataUsage(
    val totalGB: Double,
    val usedGB: Double,
    val remainingGB: Double,
    val usagePercent: Int,
    val totalFormatted: String,
    val usedFormatted: String,
    val remainingFormatted: String,
    val isUnlimited: Boolean
)

@Serializable
data class Validity(
    val validityDays: Int,
    val remainingDays: Int,
    val activationDate: String?,
    val activationDateFormatted: String?,
    val expirationDate: String?,
    val expirationDateFormatted: String?,
    val isExpired: Boolean,
    val isActivated: Boolean
)