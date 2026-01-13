package com.mtislab.auth.presentation.register

import com.mtislab.domain.GoogleAuthProvider

sealed interface RegisterAction {
    data class OnGoogleSignInClick(val provider: GoogleAuthProvider) : RegisterAction
}