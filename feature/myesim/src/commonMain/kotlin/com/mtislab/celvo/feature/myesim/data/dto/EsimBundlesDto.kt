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
    @SerialName("initialFormatted") val initialFormatted: String,
    @SerialName("remainingFormatted") val remainingFormatted: String,
    @SerialName("usedFormatted") val usedFormatted: String,
    @SerialName("isUnlimited") val isUnlimited: Boolean = false,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("remainingDays") val remainingDays: Int? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("expiryDate") val expiryDate: String? = null,
    @SerialName("assignmentId") val assignmentId: String? = null,
    @SerialName("countryCode") val countryCode: String,
    @SerialName("countryName") val countryName: String,
    @SerialName("flagUrl") val flagUrl: String
)

@Serializable
data class BundleSummaryDto(
    @SerialName("totalBundles") val totalBundles: Int = 0,
    @SerialName("activeBundleCount") val activeBundleCount: Int = 0,
    @SerialName("queuedBundleCount") val queuedBundleCount: Int = 0,
    @SerialName("historyBundleCount") val historyBundleCount: Int = 0,
    @SerialName("totalDataPurchasedBytes") val totalDataPurchasedBytes: Long = 0,
    @SerialName("totalDataUsedBytes") val totalDataUsedBytes: Long = 0,
    @SerialName("totalDataPurchasedFormatted") val totalDataPurchasedFormatted: String = "",
    @SerialName("totalDataUsedFormatted") val totalDataUsedFormatted: String = ""
)