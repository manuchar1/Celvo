package com.mtislab.celvo.feature.store.domain.model


data class WalletPaymentRequest(
    val sku: String,
    val bundleName: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: WalletType,
    val walletToken: String,
    val promoCodeId: String? = null
)

enum class WalletType {
    GOOGLE_PAY,
    APPLE_PAY
}

/**
 * Result from the Celvo backend after processing the wallet payment
 * through BOG. Two possible outcomes:
 *
 * 1. COMPLETED — payment done, no further action needed.
 * 2. REQUIRES_3DS — must redirect user to [redirectUrl] for 3DS auth.
 */
data class WalletPaymentResult(
    val orderId: String,
    val status: WalletPaymentStatus,
    val redirectUrl: String? = null
)

enum class WalletPaymentStatus {
    COMPLETED,
    REQUIRES_3DS,
    FAILED
}