package com.mtislab.celvo.feature.store.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentInitiateRequest(
    val amount: Double,
    val sku: String,
    val bundleName: String,
    val currency: String = "GEL",
    val language: String = "en",
    val theme: String = "dark"
)

data class PaymentInitiateResult(
    val redirectUrl: String,
    val orderId: String? = null
)