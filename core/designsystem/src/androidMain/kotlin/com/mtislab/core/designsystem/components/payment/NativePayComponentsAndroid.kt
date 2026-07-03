package com.mtislab.core.designsystem.components.payment

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.payment_google_pay_error
import com.celvo.core.designsystem.resources.payment_no_data_received
import com.celvo.core.designsystem.resources.payment_no_response
import com.celvo.core.designsystem.resources.payment_token_extraction_failed
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import org.jetbrains.compose.resources.stringResource
import org.json.JSONObject

@Composable
actual fun NativePayButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    // Google Pay brand guidelines: black (Dark) button on light backgrounds,
    // white (Light) button on dark backgrounds; matching combos fail GPay
    // integration review (flagged by Google Pay API support, 2026-07-03).
    // Celvo has an in-app theme override, so the button must follow the
    // theme's actual background — not the system theme.
    val onDarkBackground = MaterialTheme.colorScheme.background.luminance() < 0.5f
    PayButton(
        onClick = onClick,
        allowedPaymentMethods = GooglePayConfig.isReadyToPayJson()
            .getJSONArray("allowedPaymentMethods")
            .toString(),
        theme = if (onDarkBackground) ButtonTheme.Light else ButtonTheme.Dark,
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

    // Resolve localized error strings inside @Composable scope and capture them
    // so the off-main-thread Tasks callbacks below can surface translated text
    // without re-entering composition.
    val tokenError = stringResource(Res.string.payment_token_extraction_failed)
    val noPaymentDataError = stringResource(Res.string.payment_no_data_received)
    val noResponseError = stringResource(Res.string.payment_no_response)
    val genericError = stringResource(Res.string.payment_google_pay_error)

    val resolvePaymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let { paymentData ->
                        extractToken(paymentData)?.let(onTokenReceived)
                            ?: onError(tokenError)
                    } ?: onError(noPaymentDataError)
                } ?: onError(noResponseError)
            }
            Activity.RESULT_CANCELED -> onCancelled()
            else -> onError(genericError)
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
                                ?: onError(tokenError)
                        }
                    } else {
                        when (val exception = completedTask.exception) {
                            is ResolvableApiException -> {
                                resolvePaymentLauncher.launch(
                                    IntentSenderRequest.Builder(exception.resolution).build()
                                )
                            }
                            else -> onError(exception?.message ?: genericError)
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