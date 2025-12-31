package com.mtislab.auth


import androidx.compose.runtime.Composable
import com.mtislab.domain.GoogleAuthProvider

@Composable
expect fun rememberGoogleAuthProvider(): GoogleAuthProvider