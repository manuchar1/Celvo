package com.mtislab.core.data.payment

import android.content.Context
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.mtislab.core.designsystem.components.payment.GooglePayConfig
import com.mtislab.core.domain.payment.NativePayManager
import kotlinx.coroutines.tasks.await

/**
 * Android availability check for Google Pay.
 *
 * Delegates both the PaymentsClient creation and the isReadyToPay JSON to
 * [GooglePayConfig] — the same object that backs the Composable PayButton
 * launcher. This guarantees a SINGLE source of truth for:
 *   - Google Pay environment (TEST / PRODUCTION)
 *   - Allowed card networks (VISA / MasterCard / Amex)
 *   - Allowed auth methods (PAN_ONLY / CRYPTOGRAM_3DS)
 *
 * When flipping to production, [GooglePayConfig.createPaymentsClient] is the
 * ONLY place to update — this class will automatically pick up the change.
 */
class GooglePayManagerAndroid(
    context: Context
) : NativePayManager {

    private val paymentsClient = GooglePayConfig.createPaymentsClient(context)

    override suspend fun isAvailable(): Boolean {
        return try {
            val request = IsReadyToPayRequest.fromJson(
                GooglePayConfig.isReadyToPayJson().toString()
            )
            paymentsClient.isReadyToPay(request).await()
        } catch (e: Exception) {
            false
        }
    }
}
