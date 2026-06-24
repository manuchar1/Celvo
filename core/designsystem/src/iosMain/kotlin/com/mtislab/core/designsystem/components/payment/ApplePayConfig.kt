package com.mtislab.core.designsystem.components.payment

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDecimalNumber
import platform.PassKit.PKMerchantCapability3DS
import platform.PassKit.PKPaymentNetworkAmex
import platform.PassKit.PKPaymentNetworkMasterCard
import platform.PassKit.PKPaymentNetworkVisa
import platform.PassKit.PKPaymentRequest
import platform.PassKit.PKPaymentSummaryItem

/**
 * Apple Pay configuration for Bank of Georgia (Georgian Card processor).
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * IMPORTANT CONFIGURATION NOTES
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * 1. [MERCHANT_IDENTIFIER] MUST be registered in the Apple Developer Portal
 *    (Certificates, Identifiers & Profiles → Identifiers → Merchant IDs).
 *    Reverse-DNS format, e.g. "merchant.ge.mtislab.celvo".
 *
 * 2. The Apple Pay capability MUST be enabled in the iosApp Xcode target:
 *    Signing & Capabilities → + Capability → Apple Pay → check the merchant ID.
 *    This writes the "Merchant IDs" entry into `iosApp.entitlements`.
 *
 * 3. The Merchant ID certificate MUST be uploaded to Bank of Georgia so BOG /
 *    Georgian Card can decrypt the PKPaymentToken.paymentData blob. Provide
 *    BOG's merchant onboarding team the .cer generated in the Apple Developer
 *    Portal under your Merchant ID → Apple Pay Payment Processing Certificate.
 *
 * 4. Supported networks are aligned with BOG's published Google Pay config
 *    (VISA / MasterCard / Amex) and 3-D Secure merchant capability, which
 *    matches the PAN_ONLY + CRYPTOGRAM_3DS methods declared in [GooglePayConfig].
 *
 * The encrypted [platform.PassKit.PKPaymentToken.paymentData] NSData payload
 * is itself a UTF-8 JSON blob per the Apple Pay spec. `NativePayComponentsIos`
 * parses it and embeds it as a nested object inside the BOG `apple_pay_token`
 * envelope (see the BOG completion endpoint docs:
 * https://api.bog.ge/docs/en/payments/external-orders/complete-external-applepay).
 * The resulting string is forwarded to the Celvo backend the same way the
 * Google Pay `paymentMethodData.tokenizationData.token` string is forwarded.
 */
internal object ApplePayConfig {

    const val MERCHANT_IDENTIFIER: String = "merchant.com.mtislab.celvo"

    /** ISO 3166-1 alpha-2 country code where the merchant bank is located. */
    const val COUNTRY_CODE: String = "GE"

    /**
     * Card networks accepted by BOG's Apple Pay integration.
     * Matches [GooglePayConfig.allowedCardNetworks].
     *
     * The `PKPaymentNetwork*` bindings in Kotlin/Native are typed `String?`
     * (Apple declares them `NSString * const` without nullability annotations),
     * so we assert non-null — they're guaranteed to be present on every iOS
     * SDK version we target.
     */
    private val supportedNetworks: List<String> = listOf(
        PKPaymentNetworkVisa!!,
        PKPaymentNetworkMasterCard!!,
        PKPaymentNetworkAmex!!
    )

    /** Exposed so the availability check can reuse the exact same set. */
    fun supportedNetworks(): List<String> = supportedNetworks

    /**
     * Builds a [PKPaymentRequest] for the given amount.
     *
     * @param amountCents integer amount in minor units (e.g. tetri / cents).
     *                    Kept as Int to stay symmetrical with the Android side.
     * @param currencyCode ISO 4217 (e.g. "GEL", "USD", "EUR").
     * @param merchantName Human-readable label shown on the Apple Pay sheet.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun buildPaymentRequest(
        amountCents: Int,
        currencyCode: String,
        merchantName: String
    ): PKPaymentRequest {
        // NSDecimalNumber avoids float-formatting locale issues entirely.
        // The `numberWithDouble:` ObjC class method isn't auto-generated in
        // Kotlin/Native for NSDecimalNumber — use the `initWithDouble:` init.
        val amount = NSDecimalNumber(double = amountCents / 100.0)

        // The two-argument overload defaults to PKPaymentSummaryItemTypeFinal at
        // the ObjC layer. We avoid referencing the enum directly because the
        // PKPaymentSummaryItemType symbol isn't exposed in every Kotlin/Native
        // PassKit binding set.
        val totalItem = PKPaymentSummaryItem.summaryItemWithLabel(
            label = merchantName,
            amount = amount
        )

        return PKPaymentRequest().apply {
            merchantIdentifier = MERCHANT_IDENTIFIER
            countryCode = COUNTRY_CODE
            this.currencyCode = currencyCode.uppercase()
            supportedNetworks = this@ApplePayConfig.supportedNetworks
            // 3DS is required by BOG; EMV is optional and covers chip-authenticated cards.
            merchantCapabilities = PKMerchantCapability3DS
            paymentSummaryItems = listOf(totalItem)
        }
    }
}
