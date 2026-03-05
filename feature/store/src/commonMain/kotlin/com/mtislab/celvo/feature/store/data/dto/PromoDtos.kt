package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromoValidationRequestDto(
    @SerialName("code") val code: String,
    @SerialName("cartValue") val cartValue: Double,
    @SerialName("bundleId") val bundleId: String,
    @SerialName("countryIso") val countryIso: String,
    @SerialName("regionId") val regionId: String
)


@Serializable
data class PromoValidationResponseDto(
    @SerialName("valid") val valid: Boolean,
    @SerialName("codeId") val codeId: String? = null,
    @SerialName("codeType") val codeType: String? = null,
    @SerialName("discountAmount") val discountAmount: Double? = null,
    @SerialName("discountDisplay") val discountDisplay: String? = null,
    @SerialName("originalPrice") val originalPrice: Double? = null,
    @SerialName("finalPrice") val finalPrice: Double? = null,
    @SerialName("walletBalance") val walletBalance: Double? = null,
    @SerialName("isReferral") val isReferral: Boolean? = null,
    @SerialName("referrerUserId") val referrerUserId: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("errorCode") val errorCode: String? = null,
    @SerialName("errorMessage") val errorMessage: String? = null
)