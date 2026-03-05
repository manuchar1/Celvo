package com.mtislab.celvo.feature.store.domain.model

import androidx.compose.ui.graphics.Color

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

    // ★ NEW FIELDS: Required for the interactive Promo Engine ★
    val promoCode: String? = null,
    val claimedTitle: String? = null,
    val claimedDescription: String? = null
)

enum class BannerType {
    HERO,
    SECONDARY,
    UNKNOWN
}