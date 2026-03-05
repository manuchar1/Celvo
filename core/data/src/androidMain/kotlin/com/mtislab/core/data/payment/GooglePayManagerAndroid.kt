package com.mtislab.core.data.payment

import android.content.Context
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.mtislab.core.domain.payment.NativePayManager
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class GooglePayManagerAndroid(
    context: Context
) : NativePayManager {

    private val paymentsClient: PaymentsClient = Wallet.getPaymentsClient(
        context,
        Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()
    )

    override suspend fun isAvailable(): Boolean {
        return try {
            val json = JSONObject().apply {
                put("apiVersion", 2)
                put("apiVersionMinor", 0)
                put("allowedPaymentMethods", JSONArray().put(
                    JSONObject().apply {
                        put("type", "CARD")
                        put("parameters", JSONObject().apply {
                            put("allowedAuthMethods", JSONArray().apply {
                                put("PAN_ONLY")
                                put("CRYPTOGRAM_3DS")
                            })
                            put("allowedCardNetworks", JSONArray().apply {
                                put("VISA")
                                put("MASTERCARD")
                                put("AMEX")
                            })
                        })
                    }
                ))
            }
            val request = IsReadyToPayRequest.fromJson(json.toString())
            paymentsClient.isReadyToPay(request).await()
        } catch (e: Exception) {
            false
        }
    }
}