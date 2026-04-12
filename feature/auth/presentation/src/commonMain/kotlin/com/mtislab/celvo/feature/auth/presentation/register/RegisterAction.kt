package com.mtislab.celvo.feature.auth.presentation.register

import com.mtislab.core.domain.auth.AppleAuthProvider
import com.mtislab.core.domain.auth.GoogleAuthProvider

sealed interface RegisterAction {
    data class OnGoogleSignInClick(val provider: GoogleAuthProvider?) : RegisterAction
    data class OnAppleSignInClick(val provider: AppleAuthProvider?) : RegisterAction
}