package com.mtislab.core.designsystem.components.auth


import androidx.compose.runtime.Composable
import com.mtislab.core.domain.auth.GoogleAuthProvider

@Composable
expect fun rememberGoogleAuthProvider(): GoogleAuthProvider