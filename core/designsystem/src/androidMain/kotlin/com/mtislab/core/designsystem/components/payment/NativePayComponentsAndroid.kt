package com.mtislab.core.designsystem.components.payment

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import org.json.JSONObject

@Composable
actual fun NativePayButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    PayButton(
        onClick = onClick,
        allowedPaymentMethods = GooglePayConfig.isReadyToPayJson()
            .getJSONArray("allowedPaymentMethods")
            .toString(),
        theme = ButtonTheme.Dark,
        type = ButtonType.Pay,
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
actual fun rememberNativePayLauncher(
    onTokenReceived: (token: String) -> Unit,
    onCancelled: () -> Unit,
    onError: (message: String) -> Unit
): NativePayLauncher {
    val context = LocalContext.current
    val paymentsClient = remember { GooglePayConfig.createPaymentsClient(context) }

    val resolvePaymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let { paymentData ->
                        extractToken(paymentData)?.let(onTokenReceived)
                            ?: onError("ტოკენის ამოღება ვერ მოხერხდა")
                    } ?: onError("გადახდის მონაცემები ვერ მოიძებნა")
                } ?: onError("Google Pay-დან პასუხი არ მიღებულა")
            }
            Activity.RESULT_CANCELED -> onCancelled()
            else -> onError("Google Pay შეცდომა")
        }
    }

    return remember(paymentsClient, resolvePaymentLauncher) {
        object : NativePayLauncher {
            override fun launch(amountCents: Int, currencyCode: String, merchantName: String) {
                val requestJson = GooglePayConfig.paymentDataRequestJson(
                    amountCents = amountCents,
                    currencyCode = currencyCode,
                    merchantName = merchantName
                )
                val request = PaymentDataRequest.fromJson(requestJson.toString())
                val task = paymentsClient.loadPaymentData(request)

                task.addOnCompleteListener { completedTask ->
                    if (completedTask.isSuccessful) {
                        completedTask.result?.let { paymentData ->
                            extractToken(paymentData)?.let(onTokenReceived)
                                ?: onError("ტოკენის ამოღება ვერ მოხერხდა")
                        }
                    } else {
                        when (val exception = completedTask.exception) {
                            is ResolvableApiException -> {
                                resolvePaymentLauncher.launch(
                                    IntentSenderRequest.Builder(exception.resolution).build()
                                )
                            }
                            else -> onError(exception?.message ?: "Google Pay შეცდომა")
                        }
                    }
                }
            }
        }
    }
}

private fun extractToken(paymentData: PaymentData): String? {
    return try {
        JSONObject(paymentData.toJson())
            .getJSONObject("paymentMethodData")
            .getJSONObject("tokenizationData")
            .getString("token")
    } catch (e: Exception) {
        null
    }
}