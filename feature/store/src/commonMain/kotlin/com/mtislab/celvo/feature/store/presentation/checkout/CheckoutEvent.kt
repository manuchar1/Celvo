package com.mtislab.celvo.feature.store.presentation.checkout

sealed interface CheckoutEvent {

    /** Open payment redirect URL in in-app browser (Chrome Custom Tab / Safari VC). */
    data class OpenWebUrl(val url: String) : CheckoutEvent

    /** Show a Snackbar error message. */
    data class ShowError(val message: String) : CheckoutEvent

    /**
     * Navigate to PaymentResultScreen.
     */
    data class NavigateToPaymentResult(
        val isSuccess: Boolean,
        val orderId: String? = null
    ) : CheckoutEvent

    /**
     * NEW: Instructs the UI layer to launch the native wallet payment sheet
     * (Google Pay / Apple Pay). The ViewModel can't do this directly because
     * it requires an ActivityResult launcher which lives in the Composable scope.
     *
     * Same pattern as LoginWithGoogle → rememberGoogleAuthProvider.
     */
    data class LaunchNativeWalletPayment(
        val amountCents: Int,
        val currencyCode: String
    ) : CheckoutEvent
}