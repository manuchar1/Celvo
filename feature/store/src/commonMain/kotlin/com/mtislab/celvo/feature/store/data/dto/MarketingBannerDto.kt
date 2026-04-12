package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketingBannerDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String? = null,
    @SerialName("placement") val placement: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("ctaText") val ctaText: String? = null,
    @SerialName("ctaLink") val ctaLink: String? = null,
    @SerialName("backgroundColor") val backgroundColor: String? = null,
    @SerialName("textColor") val textColor: String? = null,
    @SerialName("promoCode") val promoCode: String? = null,
    @SerialName("claimedTitle") val claimedTitle: String? = null,
    @SerialName("claimedDescription") val claimedDescription: String? = null,
    @SerialName("sortOrder") val sortOrder: Int? = null,
    @SerialName("isActive") val isActive: Boolean = true,
    @SerialName("validFrom") val validFrom: String? = null,
    @SerialName("validUntil") val validUntil: String? = null,
    @SerialName("targetAudience") val targetAudience: String? = null,
    @SerialName("expiresAt") val expiresAt: String? = null,
    @SerialName("showCountdown") val showCountdown: Boolean = false,
)