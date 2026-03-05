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
    @SerialName("wasEverInstalled") val wasEverInstalled: Boolean = false,
    @SerialName("smdpAddress") val smdpAddress: String,
    @SerialName("activationCode") val activationCode: String,
    @SerialName("manualInstallCode") val manualInstallCode: String,
    @SerialName("primaryCountryCode") val primaryCountryCode: String,
    @SerialName("primaryFlagUrl") val primaryFlagUrl: String,
    @SerialName("packages") val packages: List<EsimHomePackageDto> = emptyList(),
    @SerialName("hasActivePackage") val hasActivePackage: Boolean = false,
    @SerialName("totalPackageCount") val totalPackageCount: Int = 0,
    @SerialName("dataFreshnessAt") val dataFreshnessAt: String? = null,
    @SerialName("dataLive") val dataLive: Boolean = true,
    @SerialName("installed") val installed: Boolean = false
)

@Serializable
data class EsimHomePackageDto(
    @SerialName("bundleName") val bundleName: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("packageStatus") val packageStatus: String,
    @SerialName("packageStatusDisplay") val packageStatusDisplay: String,
    @SerialName("initialBytes") val initialBytes: Long,
    @SerialName("remainingBytes") val remainingBytes: Long,
    @SerialName("usedBytes") val usedBytes: Long,
    @SerialName("usagePercent") val usagePercent: Int = 0,
    @SerialName("initialFormatted") val initialFormatted: String,
    @SerialName("remainingFormatted") val remainingFormatted: String,
    @SerialName("usedFormatted") val usedFormatted: String,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("remainingDays") val remainingDays: Int? = null,
    @SerialName("countryCode") val countryCode: String,
    @SerialName("countryName") val countryName: String,
    @SerialName("flagUrl") val flagUrl: String,
    @SerialName("activePackage") val activePackage: Boolean = false,
    @SerialName("unlimited") val unlimited: Boolean = false
)