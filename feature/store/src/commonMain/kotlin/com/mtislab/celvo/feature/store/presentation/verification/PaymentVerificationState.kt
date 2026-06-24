package com.mtislab.celvo.feature.store.presentation.verification

import com.mtislab.core.domain.esim.EsimInstallationData
import com.mtislab.core.presentation.util.UiText

sealed interface PaymentVerificationState {

    data object Loading : PaymentVerificationState

    data class SuccessNewEsim(
        val esimData: EsimInstallationData,
        val bundleName: String
    ) : PaymentVerificationState

    data class SuccessTopUp(
        val bundleName: String
    ) : PaymentVerificationState

    data class Error(
        val message: UiText
    ) : PaymentVerificationState
}
