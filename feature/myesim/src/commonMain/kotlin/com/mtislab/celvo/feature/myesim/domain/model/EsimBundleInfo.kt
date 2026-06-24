package com.mtislab.celvo.feature.myesim.domain.model

import com.mtislab.core.domain.model.AssignmentId

data class EsimBundleInfo(
    val iccid: String,
    val bundles: List<EsimBundle>,
    val totalBundles: Int,
    val summary: EsimBundleSummary? = null
) {
    /** Active bundles: currently consuming data */
    val activeBundles: List<EsimBundle>
        get() = bundles.filter { it.state.lowercase() == "active" }

    /** Pending bundles: queued/processing/assigned — waiting to activate */
    val pendingBundles: List<EsimBundle>
        get() = bundles.filter {
            it.state.lowercase() in listOf("queued", "processing", "assigned")
        }

    /** Current tab: active + pending */
    val currentBundles: List<EsimBundle>
        get() = activeBundles + pendingBundles

    /** History tab: depleted/expired/revoked/lapsed */
    val historyBundles: List<EsimBundle>
        get() = bundles.filter {
            it.state.lowercase() in listOf("depleted", "expired", "revoked", "lapsed")
        }

    val hasHistory: Boolean
        get() = historyBundles.isNotEmpty()
}

data class EsimBundle(
    val assignmentId: AssignmentId,
    val bundleName: String,
    val displayName: String,
    val state: String,
    val stateDisplayName: String,
    val initialBytes: Long,
    val remainingBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val initialFormatted: String,
    val remainingFormatted: String,
    val usedFormatted: String,
    val isUnlimited: Boolean,
    val startTime: String?,
    val endTime: String?,
    val remainingDays: Int?,
    val duration: String?,
    val expiryDate: String?,
    val purchasedAt: String?,
    /** 0 = active, 1..N = queued, null = terminal. */
    val queuePosition: Int?,
    val countryCode: String,
    val countryName: String,
    val flagUrl: String,
    val description: String? = null,
    val throttleSpeedKbps: Int? = null,
    val throttleAfterMb: Int? = null,
    val bundleGroups: List<String> = emptyList(),
    val networkTypes: List<String> = emptyList(),
    val roamingCountries: List<String> = emptyList()
) {
    val isActive: Boolean get() = state.lowercase() == "active"
    val isPending: Boolean get() = state.lowercase() in listOf("queued", "processing", "assigned")

    /** UI-friendly status: map "Queued" → "Pending" */
    val displayStatus: String
        get() = if (isPending) "Pending" else stateDisplayName
}

/**
 * Aggregate information about the bundle set.
 *
 * `activeRemainingFormatted` and `totalDataRemainingFormatted` are pre-formatted
 * server-side so they match push notifications and emails — prefer verbatim use
 * over client-side math.
 */
data class EsimBundleSummary(
    val totalBundles: Int,
    val activeBundleCount: Int,
    val queuedBundleCount: Int,
    val historyBundleCount: Int,
    val totalDataPurchasedBytes: Long,
    val totalDataUsedBytes: Long,
    val totalDataRemainingBytes: Long,
    val totalDataPurchasedFormatted: String,
    val totalDataUsedFormatted: String,
    val totalDataRemainingFormatted: String,
    val activeRemainingBytes: Long?,
    val activeRemainingFormatted: String?,
    val nextExpiryAt: String?
)
