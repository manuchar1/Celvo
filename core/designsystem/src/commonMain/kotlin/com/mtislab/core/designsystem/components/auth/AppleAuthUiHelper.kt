package com.mtislab.core.designsystem.components.auth

import androidx.compose.runtime.Composable
import com.mtislab.core.domain.auth.AppleAuthProvider

/**
 * Provides a platform-specific [AppleAuthProvider].
 *
 * - **iOS:** Will use ASAuthorizationAppleIDProvider for native sign-in.
 *   Currently returns a stub that falls back to the web flow.
 * - **Android / JVM:** Returns a stub (Apple Sign-In is iOS-only in this app).
 */
@Composable
expect fun rememberAppleAuthProvider(): AppleAuthProvider
