package com.mtislab.celvo.feature.store.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing an informational marketing banner.
 *
 * Tapping a banner opens its [deepLink] — banners carry no promo-claim
 * behaviour.
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
)

enum class BannerType {
    HERO,
    SECONDARY,
    UNKNOWN,
}

enum class BannerPlacement {
    STORE,
    POST_PURCHASE,
}
