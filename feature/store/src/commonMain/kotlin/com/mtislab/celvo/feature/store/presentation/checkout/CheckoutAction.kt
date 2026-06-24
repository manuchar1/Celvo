package com.mtislab.celvo.feature.store.presentation.checkout

import PaymentMethod
import com.mtislab.celvo.feature.store.domain.model.WalletType
import com.mtislab.core.domain.auth.AppleAuthProvider
import com.mtislab.core.domain.auth.GoogleAuthProvider

sealed interface CheckoutAction {

    // --- Payment Flow ---
    data object PayClicked : CheckoutAction
    data class SelectPaymentMethod(val method: PaymentMethod) : CheckoutAction

    // --- Wallet Payment (NEW) ---
    /**
     * Google Pay / Apple Pay SDK returned an encrypted token successfully.
     * [walletType] is decided at the call-site in the Composable where the
     * platform is known — the ViewModel stays platform-agnostic.
     */
    data class WalletTokenReceived(
        val token: String,
        val walletType: WalletType
    ) : CheckoutAction
    /** User dismissed the Google Pay / Apple Pay sheet. */
    data object WalletPaymentCancelled : CheckoutAction
    /** Google Pay / Apple Pay SDK returned an error. */
    data class WalletPaymentFailed(val message: String) : CheckoutAction

    // --- Auth (Login Sheet) ---
    data object DismissLoginSheet : CheckoutAction
    data class LoginWithGoogle(val provider: GoogleAuthProvider? = null) : CheckoutAction
    data class LoginWithApple(val provider: AppleAuthProvider? = null) : CheckoutAction

    // --- Promo Code ---
    data object OpenPromoSheet : CheckoutAction
    data object DismissPromoSheet : CheckoutAction
    data class PromoCodeChanged(val code: String) : CheckoutAction
    data object ApplyPromoCode : CheckoutAction
    data object ClearPromoCode : CheckoutAction
}