package com.mtislab.celvo.feature.store.domain.model

data class PromoValidationRequest(
    val code: String,
    val cartValue: Double,
    val bundleId: String,
    val countryIso: String,
    val regionId: String
)


data class PromoValidationResult(
    val valid: Boolean,
    val codeId: String?,
    val codeType: String?,
    val discountAmount: Double,
    val discountDisplay: String?,
    val originalPrice: Double,
    val finalPrice: Double,
    val walletBalance: Double,
    val isReferral: Boolean,
    val referrerUserId: String?,
    val displayName: String?,
    val errorCode: String?,
    val errorMessage: String?
)