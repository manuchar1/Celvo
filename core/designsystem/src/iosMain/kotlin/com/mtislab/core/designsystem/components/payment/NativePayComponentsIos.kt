package com.mtislab.core.designsystem.components.payment

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mtislab.core.designsystem.components.buttons.CelvoButton

@Composable
actual fun NativePayButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    // Won't render — isWalletAvailable = false on iOS hides this button
    CelvoButton(
        text = "Apple Pay",
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled
    )
}

@Composable
actual fun rememberNativePayLauncher(
    onTokenReceived: (token: String) -> Unit,
    onCancelled: () -> Unit,
    onError: (message: String) -> Unit
): NativePayLauncher {
    return remember {
        object : NativePayLauncher {
            override fun launch(amountCents: Int, currencyCode: String, merchantName: String) {
                onError("Apple Pay is not yet supported")
            }
        }
    }
}