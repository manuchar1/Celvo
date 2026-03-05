package com.mtislab.core.designsystem.components.payment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-native branded payment button.
 *
 * Android: Google Pay official PayButton (required by brand guidelines)
 * iOS: Stub (Apple Pay PKPaymentButton in Phase 2)
 */
@Composable
expect fun NativePayButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
)

/**
 * Callback-based interface for launching the native payment sheet.
 * Follows the same pattern as [rememberGoogleAuthProvider].
 */
interface NativePayLauncher {
    fun launch(amountCents: Int, currencyCode: String, merchantName: String)
}

/**
 * Remembers a platform-specific NativePayLauncher.
 *
 * Android: Google Pay → ActivityResultLauncher → token extraction
 * iOS: Stub → calls onError
 */
@Composable
expect fun rememberNativePayLauncher(
    onTokenReceived: (token: String) -> Unit,
    onCancelled: () -> Unit,
    onError: (message: String) -> Unit
): NativePayLauncher