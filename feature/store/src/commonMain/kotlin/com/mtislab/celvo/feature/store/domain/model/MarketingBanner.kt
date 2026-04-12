package com.mtislab.celvo.feature.store.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a marketing banner.
 *
 * Banners with a non-null [promoCode] are "interactive" — they support
 * a claim flow where tapping the CTA grants the user a discount code.
 * After claiming, the UI swaps [title]/[description] for [claimedTitle]/[claimedDescription].
 *
 * The [isClaimed] flag is a presentation-layer concern and is NOT part of
 * the raw API response. It's merged in by the ViewModel after comparing
 * against the local [PromoClaimRepository].
 */
data class MarketingBanner(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val ctaText: String,
    val deepLink: String,
    val backgroundColor: Color,
    val textColor: Color,
    val type: BannerType,
    val placement: BannerPlacement = BannerPlacement.STORE,

    // ── Promo claim fields (nullable = standard non-interactive banner) ──
    val promoCode: String? = null,
    val claimedTitle: String? = null,
    val claimedDescription: String? = null,

    // ── Runtime state (merged by ViewModel, NOT from API) ──
    val isClaimed: Boolean = false,
) {
    /** The title to display — swaps to [claimedTitle] when claimed. */
    val displayTitle: String
        get() = if (isClaimed && claimedTitle != null) claimedTitle else title

    /** The description to display — swaps to [claimedDescription] when claimed. */
    val displayDescription: String
        get() = if (isClaimed && claimedDescription != null) claimedDescription else description

    /** Whether this banner supports the interactive claim flow. */
    val isInteractive: Boolean get() = promoCode != null
}

enum class BannerType {
    HERO,
    SECONDARY,
    UNKNOWN,
}

enum class BannerPlacement {
    STORE,
    POST_PURCHASE,
}