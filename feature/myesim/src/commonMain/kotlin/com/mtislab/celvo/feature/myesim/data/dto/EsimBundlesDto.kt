package com.mtislab.celvo.feature.myesim.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsimBundlesResponseDto(
    @SerialName("iccid") val iccid: String,
    @SerialName("bundles") val bundles: List<BundleDto> = emptyList(),
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
    // Backend field name is `unlimited` (not `isUnlimited`) — mismatching the
    // SerialName silently parses every bundle as metered, so the unlimited UI
    // never fires.
    @SerialName("unlimited") val isUnlimited: Boolean = false,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("remainingDays") val remainingDays: Int? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("expiryDate") val expiryDate: String? = null,
    @SerialName("assignmentId") val assignmentId: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("countryName") val countryName: String? = null,
    @SerialName("flagUrl") val flagUrl: String? = null,

    // Per-assignment backend model (Option C).
    // `purchasedAt` drives queue ordering on the server.
    // `queuePosition`: 0 = active, 1..N = queued, null = terminal.
    @SerialName("purchasedAt") val purchasedAt: String? = null,
    @SerialName("queuePosition") val queuePosition: Int? = null,

    // Catalogue-sourced bundle metadata (backfilled by catalogue sync; safely
    // absent on older payloads, so all defaults keep deserialisation stable).
    @SerialName("description") val description: String? = null,
    @SerialName("throttleSpeedKbps") val throttleSpeedKbps: Int? = null,
    @SerialName("throttleAfterMb") val throttleAfterMb: Int? = null,
    @SerialName("bundleGroups") val bundleGroups: List<String> = emptyList(),
    @SerialName("networkTypes") val networkTypes: List<String> = emptyList(),
    @SerialName("roamingCountries") val roamingCountries: List<String> = emptyList()
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
    @SerialName("totalDataUsedFormatted") val totalDataUsedFormatted: String = "",

    // Option C additions: aggregate remaining + active-bundle projection.
    @SerialName("totalDataRemainingBytes") val totalDataRemainingBytes: Long = 0,
    @SerialName("totalDataRemainingFormatted") val totalDataRemainingFormatted: String = "",
    @SerialName("activeRemainingBytes") val activeRemainingBytes: Long? = null,
    @SerialName("activeRemainingFormatted") val activeRemainingFormatted: String? = null,
    @SerialName("nextExpiryAt") val nextExpiryAt: String? = null
)
