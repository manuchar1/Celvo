package com.mtislab.celvo.feature.myesim.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EsimDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MyEsimRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EsimDetailsState())

    private val _events = Channel<EsimDetailsEvent>()
    val events = _events.receiveAsFlow()

    private val routeArgs = savedStateHandle.toRoute<Route.EsimDetailsRoute>()

    val state = _state
        .onStart {
            if (_state.value.esim == null) {
                loadEsimDetails()
            }
            if (_state.value.bundleInfo == null) {
                loadBundles(isRefresh = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _state.value
        )

    fun onAction(action: EsimDetailsAction) {
        when (action) {
            is EsimDetailsAction.Refresh -> {
                loadBundles(isRefresh = true)
                loadEsimDetails()
            }
            EsimDetailsAction.BackClick -> Unit // Handled in UI
            EsimDetailsAction.ShowQrCode -> _state.update { it.copy(showQrCodeSheet = true) }
            EsimDetailsAction.DismissQrCode -> _state.update { it.copy(showQrCodeSheet = false) }
            EsimDetailsAction.ShowOperators -> _state.update { it.copy(showOperatorsSheet = true) }
            EsimDetailsAction.DismissOperators -> _state.update { it.copy(showOperatorsSheet = false) }
            EsimDetailsAction.StartEditLabel -> _state.update {
                it.copy(isEditingLabel = true, editLabelText = it.esim?.userLabel ?: "")
            }
            is EsimDetailsAction.UpdateLabelText -> _state.update { it.copy(editLabelText = action.text) }
            EsimDetailsAction.SaveLabel -> saveLabel()
            EsimDetailsAction.CancelEditLabel -> cancelEditLabel()
            EsimDetailsAction.TopUpClick -> viewModelScope.launch {
                _events.send(EsimDetailsEvent.NavigateToTopUp(routeArgs.esimId))
            }
            // დანარჩენი Action-ები...
            else -> Unit
        }
    }

    private fun loadEsimDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // TODO: აქ შენი ლოგიკით ამოიღებ ე-სიმის დეტალებს (მაგალითად ლოკალური ბაზიდან ან API-დან)
            // ამ ეტაპზე ველოდებით bundleInfo-ს, ამიტომ isLoading სტატუსს getEsimBundles დაარეგულირებს
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadBundles(isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                _state.update { it.copy(isRefreshing = true, bundlesError = null) }
            } else {
                _state.update { it.copy(isLoading = true, bundlesError = null) }
            }

            when (val result = repository.getEsimBundles(routeArgs.esimId)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            bundleInfo = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            bundlesError = null
                        )
                    }
                }
                is Resource.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            bundlesError = result.error
                        )
                    }
                    _events.send(EsimDetailsEvent.ShowError("Failed to load bundles"))
                }
            }
        }
    }

    private fun saveLabel() {
        // არსებული ლოგიკა...
    }

    private fun cancelEditLabel() {
        _state.update { it.copy(isEditingLabel = false, editLabelText = "") }
    }
}

sealed interface EsimDetailsEvent {
    data class NavigateToTopUp(val esimId: String) : EsimDetailsEvent
    data class ShowMessage(val message: String) : EsimDetailsEvent
    data class ShowError(val message: String) : EsimDetailsEvent
}