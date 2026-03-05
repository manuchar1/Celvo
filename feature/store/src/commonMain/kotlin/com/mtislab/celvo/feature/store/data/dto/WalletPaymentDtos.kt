package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletPaymentRequestDto(
    @SerialName("sku") val sku: String,
    @SerialName("bundleName") val bundleName: String,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("paymentMethod") val paymentMethod: String,
    @SerialName("walletToken") val walletToken: String,
    @SerialName("promoCodeId") val promoCodeId: String? = null
)

@Serializable
data class WalletPaymentResponseDto(
    @SerialName("orderId") val orderId: String,
    @SerialName("status") val status: String,
    @SerialName("redirectUrl") val redirectUrl: String? = null
)