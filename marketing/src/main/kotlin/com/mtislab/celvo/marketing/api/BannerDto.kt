package com.mtislab.celvo.marketing.api

import com.mtislab.celvo.marketing.domain.BannerType
import com.mtislab.celvo.marketing.domain.MarketingBanner


data class BannerDto(
    val id: String,
    val title: String,
    val description: String?,
    val assetUrl: String,       // Mascot URL
    val action: BannerAction,   // Button details
    val style: BannerStyle      // Colors and type
)

data class BannerAction(
    val label: String?,
    val deepLink: String
)

data class BannerStyle(
    val backgroundColor: String,
    val textColor: String,
    val type: BannerType
)

// Extension function to map Entity -> DTO
fun MarketingBanner.toDto() = BannerDto(
    id = this.id.toString(),
    title = this.title,
    description = this.description,
    assetUrl = this.imageUrl,
    action = BannerAction(
        label = this.ctaText,
        deepLink = this.ctaLink
    ),
    style = BannerStyle(
        backgroundColor = this.backgroundColor,
        textColor = this.textColor,
        type = this.type
    )
)