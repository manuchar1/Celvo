package com.mtislab.core.designsystem.components.payment

import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal object GooglePayConfig {

    fun createPaymentsClient(context: Context): PaymentsClient =
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // TODO: ENVIRONMENT_PRODUCTION
                .build()
        )

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
            put("merchantId", "YOUR_GOOGLE_PAY_MERCHANT_ID") // TODO: Replace
            put("merchantName", merchantName)
        })
    }
}