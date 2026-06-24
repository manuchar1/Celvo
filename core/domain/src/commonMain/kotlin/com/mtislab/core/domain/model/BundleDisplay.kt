package com.mtislab.core.domain.model

/**
 * UI projection of a bundle. The single place that decides unlimited rendering
 * rules — gauge fill, primary/secondary labels, throttle disclosure, tier.
 *
 * Both the home gauge (StoreScreen) and the eSIM details screen consume this,
 * so unlimited semantics live in exactly one mapper. `isUnlimited` is the
 * source of truth: never branch on `remainingBytes == -1`, `initialFormatted`
 * string contents, or any other derived signal.
 */
data class BundleDisplay(
    val assignmentId: AssignmentId,
    val bundleName: String,
    val displayName: String,
    val status: BundleStatus,
    val statusLabel: String,
    val isUnlimited: Boolean,

    /** 0f..1f. 1f for unlimited so callers don't divide by zero. */
    val gaugeFillFraction: Float,
    /** "∞" for unlimited, otherwise the remaining-bytes label. */
    val primaryAmountLabel: String,
    /** "Unlimited" for unlimited, "/ 1 GB" for metered. */
    val secondaryAmountLabel: String?,

    val daysLeftLabel: String?,
    val expiryIso: String?,

    val countryCode: String?,
    val countryName: String?,
    val flagUrl: String?,

    /** Non-null only when [isUnlimited] == true and throttle data is present. */
    val throttle: ThrottleTerms?,
    /** Marketing tier resolved from `bundleGroups[0]`; null for metered. */
    val tier: UnlimitedTier?,
    val roamingCountries: List<String>,
    val networkTypes: List<String>,
    val description: String?
)

enum class BundleStatus {
    ACTIVE, QUEUED, PROCESSING, ASSIGNED, DEPLETED, EXPIRED, REVOKED, LAPSED, UNKNOWN;

    val isTerminal: Boolean
        get() = this in setOf(DEPLETED, EXPIRED, REVOKED, LAPSED)

    val isQueued: Boolean
        get() = this in setOf(QUEUED, PROCESSING, ASSIGNED)
}

data class ThrottleTerms(
    /** "1 GB at full speed" or "1 GB / day at full speed". */
    val capLabel: String,
    /** "then 1.3 Mbps". */
    val throttledLabel: String
)

enum class UnlimitedTier(val displayName: String) {
    ESSENTIAL("Essential"),
    PLUS("Plus"),
    PREMIUM("Premium"),
    PROMO("Promo"),
    OTHER("Unlimited")
}
