package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mtislab.core.domain.auth.GoogleAuthProvider


@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    val context = LocalContext.current
    return remember(context) {
        AndroidGoogleAuthProvider(context)
    }
}