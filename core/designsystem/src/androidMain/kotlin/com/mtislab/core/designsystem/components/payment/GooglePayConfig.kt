package com.mtislab.core.designsystem.components.payment

import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import android.content.Context
import android.content.pm.ApplicationInfo
import org.json.JSONArray
import org.json.JSONObject

/**
 * Google Pay configuration for Bank of Georgia (Georgian Card processor).
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * IMPORTANT CONFIGURATION NOTES
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * 1. [createPaymentsClient] auto-selects the Google Pay environment from the
 *    build: a debuggable (debug) build uses `ENVIRONMENT_TEST` so the flow can
 *    be exercised locally without an approved merchant, while a release build
 *    uses [WalletConstants.ENVIRONMENT_PRODUCTION]. PRODUCTION only succeeds
 *    once the merchant is approved in the Google Pay & Wallet Console
 *    (https://pay.google.com/business/console/) and the release-signing SHA is
 *    registered — otherwise live traffic fails silently.
 *
 * 2. [tokenizationSpecification] declares `gateway = "georgiancard"` and
 *    `gatewayMerchantId = "BCR2DN4TXKPITITV"` — both values are taken directly
 *    from BOG's official Google Pay External integration documentation
 *    (api.bog.ge/docs/payments/external-orders/external-googlepay). The
 *    gatewayMerchantId identifies the Georgian Card acquirer profile on BOG's
 *    side; it is NOT per-merchant, it is the shared BOG gateway identifier
 *    that Google Pay uses to route the tokenized payload.
 *
 * 3. [paymentDataRequestJson] sets `merchantId` to the Google-issued value
 *    found in the Google Pay & Wallet Console → Setup → Integration profile.
 *    This is a SEPARATE identifier from `gatewayMerchantId` — it is Google's
 *    10-character ID for this merchant (Celvo / Mtislab LLC), issued AFTER
 *    Google Pay Business Console approval. It is NOT a BOG ID.
 *
 * 4. Authentication methods:
 *    - `PAN_ONLY` covers cards added to Google Pay from a physical card scan.
 *    - `CRYPTOGRAM_3DS` covers tokenized (device-bound) cards.
 *    Both are required; BOG's wallet endpoint handles both payloads.
 *
 * 5. The encrypted token lives at
 *    `paymentMethodData.tokenizationData.token` in the PaymentData JSON —
 *    [NativePayComponentsAndroid.extractToken] reads it and forwards it to
 *    Celvo's backend, which proxies to BOG's `/payments/google-pay` endpoint.
 */
object GooglePayConfig {

    fun createPaymentsClient(context: Context): PaymentsClient {
        val isDebuggable =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val environment = if (isDebuggable) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }
        return Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(environment)
                .build()
        )
    }

    private val allowedCardNetworks = JSONArray().apply {
        put("VISA")
        put("MASTERCARD")
        put("AMEX")
    }

    private val allowedCardAuthMethods = JSONArray().apply {
        put("PAN_ONLY")
        put("CRYPTOGRAM_3DS")
    }

    private fun tokenizationSpecification() = JSONObject().apply {
        put("type", "PAYMENT_GATEWAY")
        put("parameters", JSONObject().apply {
            // Verbatim from BOG's Google Pay External docs — do not change.
            put("gateway", "georgiancard")
            put("gatewayMerchantId", "BCR2DN4TXKPITITV")
        })
    }

    private fun baseCardPaymentMethod() = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put("allowedAuthMethods", allowedCardAuthMethods)
            put("allowedCardNetworks", allowedCardNetworks)
        })
    }

    private fun cardPaymentMethodWithTokenization() =
        baseCardPaymentMethod().apply {
            put("tokenizationSpecification", tokenizationSpecification())
        }

    fun isReadyToPayJson() = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
    }

    fun paymentDataRequestJson(
        amountCents: Int,
        currencyCode: String,
        merchantName: String
    ) = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("allowedPaymentMethods", JSONArray().put(cardPaymentMethodWithTokenization()))
        put("transactionInfo", JSONObject().apply {
            put("totalPrice", "%.2f".format(amountCents / 100.0))
            put("totalPriceStatus", "FINAL")
            put("currencyCode", currencyCode)
            put("countryCode", "GE")
        })
        put("merchantInfo", JSONObject().apply {
            // Google Pay & Wallet Console merchant ID for Celvo / Mtislab LLC.
            // Issued by Google automatically on Console sign-up; distinct from
            // gatewayMerchantId above (that one is BOG's Georgian Card gateway
            // identifier — this one identifies US to Google Pay itself).
            //
            // Validated by Google ONLY in ENVIRONMENT_PRODUCTION — TEST env
            // accepts any value. Before flipping [createPaymentsClient] to
            // PRODUCTION, ensure the Business Profile is marked "Complete" in
            // pay.google.com/business/console (otherwise live traffic fails
            // silently with resultCode=CANCELED).
            put("merchantId", "BCR2DN5T62U3BTQQ")
            put("merchantName", merchantName)
        })
    }
}