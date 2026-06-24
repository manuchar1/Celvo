package com.mtislab.celvo.feature.store.presentation.verification

sealed interface PaymentVerificationAction {
    data object RetryClicked : PaymentVerificationAction
    data object InstallEsimClicked : PaymentVerificationAction
    data object GoToDashboardClicked : PaymentVerificationAction
    data object GoToMyEsimsClicked : PaymentVerificationAction
}
