

import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.PromoValidationResult

data class CheckoutState(
    val packageDetails: EsimPackage? = null,

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
    val errorMessage: String? = null,
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
    val label: String,
    val price: Double,
    val currency: String = "₾"
)

val TopupOptions = listOf(
    TopupOption("1", "20 GB", 55.00),
    TopupOption("2", "10 GB", 30.00),
    TopupOption("3", "5 GB", 15.00),
    TopupOption("4", "სხვა", 0.0)
)
