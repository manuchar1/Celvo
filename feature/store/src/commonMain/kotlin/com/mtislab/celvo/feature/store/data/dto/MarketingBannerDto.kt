package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketingBannerDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    @SerialName("targetUrl") val targetUrl: String? = null,
    @SerialName("promoCode") val promoCode: String? = null,
    @SerialName("claimedTitle") val claimedTitle: String? = null,
    @SerialName("claimedDescription") val claimedDescription: String? = null
)