package com.mtislab.celvo.feature.store.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain model representing a marketing banner.
 * Updated to support remote assets and dynamic styling from API.
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
    val type: BannerType
)

enum class BannerType {
    HERO,
    SECONDARY,
    UNKNOWN
}