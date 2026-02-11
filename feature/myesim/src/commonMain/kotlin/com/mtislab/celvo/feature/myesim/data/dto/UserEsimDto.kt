package com.mtislab.celvo.feature.myesim.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for user's eSIM from API response.
 * All fields have sensible defaults to handle partial/malformed responses safely.
 */
@Serializable
data class UserEsimDto(
    @SerialName("id") val id: String = "",
    @SerialName("iccid") val iccid: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("statusDisplayName") val statusDisplayName: String? = null,
    @SerialName("packageName") val packageName: String? = null,
    @SerialName("bundleSku") val bundleSku: String? = null,
    @SerialName("userLabel") val userLabel: String? = null,
    @SerialName("country") val country: EsimCountryDto? = null,
    @SerialName("dataUsage") val dataUsage: DataUsageDto? = null,
    @SerialName("validity") val validity: EsimValidityDto? = null,
    @SerialName("networkTypes") val networkTypes: List<String>? = null,
    @SerialName("networkSpeed") val networkSpeed: String? = null,
    @SerialName("operators") val operators: List<EsimOperatorDto>? = null,
    @SerialName("operatorsSummary") val operatorsSummary: String? = null,
    @SerialName("installation") val installation: InstallationInfoDto? = null,
    @SerialName("autoRenewalEnabled") val autoRenewalEnabled: Boolean? = null,
    @SerialName("canTopUp") val canTopUp: Boolean? = null,
    @SerialName("canRenew") val canRenew: Boolean? = null,
    @SerialName("purchaseDate") val purchaseDate: String? = null,
    @SerialName("purchaseDateFormatted") val purchaseDateFormatted: String? = null
)

@Serializable
data class EsimCountryDto(
    @SerialName("code") val code: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("flagUrl") val flagUrl: String? = null,
    @SerialName("isRegion") val isRegion: Boolean? = null
)

@Serializable
data class DataUsageDto(
    @SerialName("totalGB") val totalGB: Double? = null,
    @SerialName("usedGB") val usedGB: Double? = null,
    @SerialName("remainingGB") val remainingGB: Double? = null,
    @SerialName("usagePercent") val usagePercent: Float? = null,
    @SerialName("totalFormatted") val totalFormatted: String? = null,
    @SerialName("usedFormatted") val usedFormatted: String? = null,
    @SerialName("remainingFormatted") val remainingFormatted: String? = null,
    @SerialName("isUnlimited") val isUnlimited: Boolean? = null,
    @SerialName("lastSyncedAt") val lastSyncedAt: String? = null
)

@Serializable
data class EsimValidityDto(
    @SerialName("validityDays") val validityDays: Int? = null,
    @SerialName("remainingDays") val remainingDays: Int? = null,
    @SerialName("activationDate") val activationDate: String? = null,
    @SerialName("activationDateFormatted") val activationDateFormatted: String? = null,
    @SerialName("expirationDate") val expirationDate: String? = null,
    @SerialName("expirationDateFormatted") val expirationDateFormatted: String? = null,
    @SerialName("isExpired") val isExpired: Boolean? = null,
    @SerialName("isActivated") val isActivated: Boolean? = null
)

@Serializable
data class EsimOperatorDto(
    @SerialName("name") val name: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("networkTypes") val networkTypes: List<String>? = null,
    @SerialName("fullDisplayName") val fullDisplayName: String? = null,
    @SerialName("networkTypesFormatted") val networkTypesFormatted: String? = null
)

@Serializable
data class InstallationInfoDto(
    @SerialName("smdpAddress") val smdpAddress: String? = null,
    @SerialName("activationCode") val activationCode: String? = null,
    @SerialName("manualCode") val manualCode: String? = null,
    @SerialName("qrCodeUrl") val qrCodeUrl: String? = null,
    @SerialName("isReady") val isReady: Boolean? = null
)

/**
 * Request DTO for updating eSIM label.
 */
@Serializable
data class UpdateLabelRequestDto(
    @SerialName("label") val label: String
)