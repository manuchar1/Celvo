package com.mtislab.celvo.feature.myesim.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsimBundlesResponseDto(
    @SerialName("iccid") val iccid: String,
    @SerialName("activeBundle") val activeBundle: BundleDto? = null,
    @SerialName("queuedBundles") val queuedBundles: List<BundleDto> = emptyList(),
    @SerialName("historyBundles") val historyBundles: List<BundleDto> = emptyList(),
    @SerialName("summary") val summary: BundleSummaryDto? = null
)

@Serializable
data class BundleDto(
    @SerialName("bundleName") val bundleName: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("state") val state: String,
    @SerialName("stateDisplayName") val stateDisplayName: String,
    @SerialName("initialBytes") val initialBytes: Long = 0,
    @SerialName("remainingBytes") val remainingBytes: Long = 0,
    @SerialName("usedBytes") val usedBytes: Long = 0,
    @SerialName("usagePercent") val usagePercent: Int = 0,
    @SerialName("remainingDays") val remainingDays: Int? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("unlimited") val unlimited: Boolean = false
)

@Serializable
data class BundleSummaryDto(
    @SerialName("totalBundles") val totalBundles: Int = 0
)