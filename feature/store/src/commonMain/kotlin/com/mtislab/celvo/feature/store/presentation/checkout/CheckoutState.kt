

import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.topup_other
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.PromoValidationResult
import com.mtislab.celvo.feature.store.domain.model.WalletQuote
import com.mtislab.core.presentation.util.UiText

data class CheckoutState(
    val packageDetails: EsimPackage? = null,

    // --- Wallet quote ---
    // Server-authoritative GEL amount the wallet sheet authorized. Set when the
    // sheet is launched; /wallet-pay must send exactly these values. Cleared on
    // payment failure so a retry always re-fetches a fresh quote.
    val walletQuote: WalletQuote? = null,

    // --- Destination Context (for promo validation) ---
    val countryIso: String = "",
    val regionId: String = "",

    // --- Auth ---
    val isLoggedIn: Boolean = false,
    val showLoginSheet: Boolean = false,

    // --- Loading / Error ---
    val isLoading: Boolean = false,
    val error: String? = null,

    // --- Payment Method Selection ---
    val isWalletAvailable: Boolean = true,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.NATIVE_WALLET,

    // --- Promo Code ---
    val promo: PromoState = PromoState()
) {
    val effectivePrice: Double
        get() = promo.appliedResult?.finalPrice ?: packageDetails?.price ?: 0.0

    val promoDiscount: Double
        get() = promo.appliedResult?.discountAmount ?: 0.0
}

data class PromoState(
    val showSheet: Boolean = false,
    val code: String = "",
    val isValidating: Boolean = false,
    val errorMessage: UiText? = null,
    val appliedResult: PromoValidationResult? = null
) {
    val appliedCodeDisplay: String?
        get() = appliedResult?.let { code.ifEmpty { null } }
}

enum class PaymentMethod {
    NATIVE_WALLET,
    CARD
}


data class TopupOption(
    val id: String,
    val label: UiText,
    val price: Double,
    val currency: String = "₾"
)

val TopupOptions = listOf(
    TopupOption("1", UiText.DynamicString("20 GB"), 55.00),
    TopupOption("2", UiText.DynamicString("10 GB"), 30.00),
    TopupOption("3", UiText.DynamicString("5 GB"), 15.00),
    TopupOption("4", UiText.Resource(Res.string.topup_other), 0.0)
)
