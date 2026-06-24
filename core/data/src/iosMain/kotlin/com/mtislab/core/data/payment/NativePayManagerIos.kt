package com.mtislab.core.data.payment

import com.mtislab.core.domain.payment.NativePayManager
import platform.PassKit.PKPaymentAuthorizationController
import platform.PassKit.PKPaymentNetworkAmex
import platform.PassKit.PKPaymentNetworkMasterCard
import platform.PassKit.PKPaymentNetworkVisa

/**
 * Apple Pay availability manager using PassKit.
 *
 * Returns `true` only when BOTH:
 *  - the device hardware supports Apple Pay (Secure Element present), AND
 *  - the user has at least one card provisioned for a network BOG accepts
 *    (Visa / Mastercard / Amex).
 *
 * The supported-networks list is kept in sync with
 * `core/designsystem/.../payment/ApplePayConfig.kt` (same module boundary
 * prevents a direct import — keep them aligned manually).
 */
class NativePayManagerIos : NativePayManager {

    // `PKPaymentNetwork*` bindings in Kotlin/Native are typed `String?` because
    // Apple's headers lack nullability annotations — assert non-null (these
    // string constants are guaranteed to exist on every supported iOS version).
    private val supportedNetworks: List<String> = listOf(
        PKPaymentNetworkVisa!!,
        PKPaymentNetworkMasterCard!!,
        PKPaymentNetworkAmex!!
    )

    override suspend fun isAvailable(): Boolean {
        // Fast path: device doesn't even have Apple Pay set up.
        if (!PKPaymentAuthorizationController.canMakePayments()) return false

        // Strict path: a compatible card is actually provisioned.
        return PKPaymentAuthorizationController.canMakePaymentsUsingNetworks(
            supportedNetworks
        )
    }
}
