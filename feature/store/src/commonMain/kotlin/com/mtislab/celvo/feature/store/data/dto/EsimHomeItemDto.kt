package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root response for GET /api/v1/esims/home.
 * [esims] is sorted by relevance — index 0 is the primary eSIM.
 */
@Serializable
data class EsimHomeResponseDto(
    @SerialName("esims") val esims: List<EsimHomeItemDto>,
    @SerialName("totalEsimCount") val totalEsimCount: Int
)

@Serializable
data class EsimHomeItemDto(
    @SerialName("iccid") val iccid: String,
    @SerialName("esimNumber") val esimNumber: Int,
    @SerialName("displayName") val displayName: String,
    @SerialName("profileStatus") val profileStatus: String,
    @SerialName("profileStatusDisplay") val profileStatusDisplay: String,
    @SerialName("isInstalled") val isInstalled: Boolean = false,
    @SerialName("wasEverInstalled") val wasEverInstalled: Boolean = false,

    // ეს ველები გახდა Nullable, რადგან API აბრუნებს null-ს
    @SerialName("smdpAddress") val smdpAddress: String? = null,
    @SerialName("activationCode") val activationCode: String? = null,
    @SerialName("manualInstallCode") val manualInstallCode: String? = null,

    @SerialName("primaryCountryCode") val primaryCountryCode: String?,
    @SerialName("primaryFlagUrl") val primaryFlagUrl: String,
    @SerialName("packages") val packages: List<EsimHomePackageDto> = emptyList(),
    @SerialName("hasActivePackage") val hasActivePackage: Boolean = false,
    @SerialName("totalPackageCount") val totalPackageCount: Int = 0,
    @SerialName("packagesLoaded") val packagesLoaded: Boolean = true,
    @SerialName("dataFreshnessAt") val dataFreshnessAt: String? = null,
    @SerialName("isDataLive") val isDataLive: Boolean = true,
)

@Serializable
data class EsimHomePackageDto(
    @SerialName("bundleName") val bundleName: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("packageStatus") val packageStatus: String,
    @SerialName("packageStatusDisplay") val packageStatusDisplay: String,
    @SerialName("initialBytes") val initialBytes: Long,
    @SerialName("remainingBytes") val remainingBytes: Long? = null,
    @SerialName("usedBytes") val usedBytes: Long? = null,
    @SerialName("usagePercent") val usagePercent: Int? = null,
    @SerialName("initialFormatted") val initialFormatted: String,
    @SerialName("remainingFormatted") val remainingFormatted: String? = null,
    @SerialName("usedFormatted") val usedFormatted: String? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("remainingDays") val remainingDays: Int? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("expiryDate") val expiryDate: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("countryName") val countryName: String? = null,
    @SerialName("flagUrl") val flagUrl: String? = null,
    @SerialName("isActivePackage") val isActivePackage: Boolean = false,
    @SerialName("isUnlimited") val isUnlimited: Boolean = false,
)