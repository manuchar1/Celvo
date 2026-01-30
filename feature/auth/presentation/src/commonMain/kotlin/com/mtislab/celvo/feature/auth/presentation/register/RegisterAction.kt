package com.mtislab.celvo.feature.auth.presentation.register

import com.mtislab.core.domain.auth.GoogleAuthProvider

sealed interface RegisterAction {
    data class OnGoogleSignInClick(val provider: GoogleAuthProvider?) : RegisterAction
    data object OnAppleSignInClick : RegisterAction
}