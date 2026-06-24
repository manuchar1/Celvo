package com.mtislab.celvo.feature.store.presentation.verification

sealed interface PaymentVerificationEvent {
    data object NavigateToHome : PaymentVerificationEvent
    data object NavigateToMyEsims : PaymentVerificationEvent
    data class OpenEsimInstallUrl(val url: String) : PaymentVerificationEvent
}
