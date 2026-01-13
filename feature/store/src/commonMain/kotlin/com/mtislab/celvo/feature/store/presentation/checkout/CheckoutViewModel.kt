package com.mtislab.celvo.feature.store.presentation.checkout

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CheckoutViewModel : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    fun onAction(action: CheckoutAction) {
        when (action) {
            is CheckoutAction.Init -> {
                _state.update { it.copy(packageDetails = action.pkg) }
            }
            is CheckoutAction.ToggleAutoTopup -> {
                _state.update { it.copy(isAutoTopupEnabled = action.enabled) }
            }
            is CheckoutAction.SelectTopupOption -> {
                _state.update { it.copy(selectedTopupOption = action.option) }
            }
        }
    }
}