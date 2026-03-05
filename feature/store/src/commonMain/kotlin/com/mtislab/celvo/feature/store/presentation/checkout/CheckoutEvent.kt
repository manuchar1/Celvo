package com.mtislab.celvo.feature.store.presentation.checkout

sealed interface CheckoutEvent {
    data class OpenWebUrl(val url: String) : CheckoutEvent
    data class ShowError(val message: String) : CheckoutEvent
}