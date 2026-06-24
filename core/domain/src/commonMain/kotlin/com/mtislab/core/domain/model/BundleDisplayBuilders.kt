package com.mtislab.core.domain.model

/**
 * Shared building blocks for converting feature-specific bundle models into a
 * [BundleDisplay]. Each feature module supplies its own thin extension
 * (`EsimHomePackage.toBundleDisplay()`, `EsimBundle.toBundleDisplay()`) that
 * calls these helpers, so unlimited semantics live in exactly one place.
 *
 * Rules encoded here:
 *  - `isUnlimited` is the sole branch point for unlimited rendering.
 *  - Unlimited packages never divide by zero — gauge is full, primary label
 *    is the infinity glyph, throttle terms are surfaced separately.
 *  - Throttle terms degrade gracefully: missing cap or speed → no disclosure.
 */
object BundleDisplayBuilders {

    const val INFINITY: String = "∞"

    fun parseStatus(raw: String): BundleStatus =
        runCatching { BundleStatus.valueOf(raw.uppercase()) }
            .getOrDefault(BundleStatus.UNKNOWN)

    fun gaugeFraction(isUnlimited: Boolean, usagePercent: Int?): Float = when {
        isUnlimited -> 1f
        usagePercent == null -> 1f
        else -> (100 - usagePercent).coerceIn(0, 100) / 100f
    }

    fun primaryAmount(
        isUnlimited: Boolean,
        remainingFormatted: String?,
        initialFormatted: String
    ): String = when {
        isUnlimited -> INFINITY
        else -> remainingFormatted ?: initialFormatted
    }

    fun secondaryAmount(
        isUnlimited: Boolean,
        initialFormatted: String
    ): String? = when {
        isUnlimited -> "Unlimited"
        else -> "/ $initialFormatted"
    }

    fun unlimitedDisplayName(isUnlimited: Boolean, rawDisplayName: String): String =
        if (isUnlimited) "$INFINITY Unlimited" else rawDisplayName

    fun formatDaysLeft(days: Int?): String? = when {
        days == null -> null
        days <= 0 -> "Expires today"
        days == 1 -> "1 day left"
        else -> "$days days left"
    }

    fun throttle(
        isUnlimited: Boolean,
        capMb: Int?,
        speedKbps: Int?,
        duration: String?
    ): ThrottleTerms? {
        if (!isUnlimited || capMb == null || speedKbps == null) return null
        val cap = formatThrottleCap(capMb)
        val perDay = duration?.contains("Day", ignoreCase = true) == true
        return ThrottleTerms(
            capLabel = if (perDay) "$cap / day at full speed" else "$cap at full speed",
            throttledLabel = "then ${formatThrottleSpeed(speedKbps)}"
        )
    }

    fun tier(groups: List<String>): UnlimitedTier? {
        val first = groups.firstOrNull() ?: return null
        return when {
            first.contains("essential", ignoreCase = true) -> UnlimitedTier.ESSENTIAL
            first.contains("plus", ignoreCase = true) -> UnlimitedTier.PLUS
            first.contains("premium", ignoreCase = true) ||
                first.contains("max", ignoreCase = true) -> UnlimitedTier.PREMIUM
            first.contains("promo", ignoreCase = true) -> UnlimitedTier.PROMO
            else -> UnlimitedTier.OTHER
        }
    }

    private fun formatThrottleCap(mb: Int): String =
        if (mb >= 1000 && mb % 1000 == 0) "${mb / 1000} GB" else "$mb MB"

    private fun formatThrottleSpeed(kbps: Int): String = when {
        kbps >= 1000 && kbps % 1000 == 0 -> "${kbps / 1000} Mbps"
        kbps >= 1000 -> "${(kbps / 100) / 10.0} Mbps"
        else -> "$kbps Kbps"
    }
}
