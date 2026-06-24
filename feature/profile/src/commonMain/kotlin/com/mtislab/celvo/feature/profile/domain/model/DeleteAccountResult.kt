package com.mtislab.celvo.feature.profile.domain.model

sealed interface DeleteAccountResult {
    data object Success : DeleteAccountResult
    data object SessionExpired : DeleteAccountResult
    data object AuthProviderUnavailable : DeleteAccountResult
    data object Retryable : DeleteAccountResult
    data object Generic : DeleteAccountResult
}
