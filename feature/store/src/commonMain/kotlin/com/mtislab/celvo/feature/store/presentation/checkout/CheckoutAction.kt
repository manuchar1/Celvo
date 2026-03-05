package com.mtislab.celvo.feature.store.presentation.checkout

import PaymentMethod
import com.mtislab.core.domain.auth.GoogleAuthProvider

sealed interface CheckoutAction {

    // --- Payment Flow ---
    data object PayClicked : CheckoutAction
    data class SelectPaymentMethod(val method: PaymentMethod) : CheckoutAction

    // --- Wallet Payment (NEW) ---
    /** Google Pay SDK returned an encrypted token successfully. */
    data class WalletTokenReceived(val token: String) : CheckoutAction
    /** User dismissed the Google Pay / Apple Pay sheet. */
    data object WalletPaymentCancelled : CheckoutAction
    /** Google Pay / Apple Pay SDK returned an error. */
    data class WalletPaymentFailed(val message: String) : CheckoutAction

    // --- Auth (Login Sheet) ---
    data object DismissLoginSheet : CheckoutAction
    data class LoginWithGoogle(val provider: GoogleAuthProvider? = null) : CheckoutAction
    data object LoginWithApple : CheckoutAction

    // --- Promo Code ---
    data object OpenPromoSheet : CheckoutAction
    data object DismissPromoSheet : CheckoutAction
    data class PromoCodeChanged(val code: String) : CheckoutAction
    data object ApplyPromoCode : CheckoutAction
    data object ClearPromoCode : CheckoutAction
}