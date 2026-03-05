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

/**
 * ViewModel for the eSim Details Screen.
 * Handles displaying and managing a specific eSIM.
 */
class EsimDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MyEsimRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EsimDetailsState())

    // One-shot events for navigation and messages
    private val _events = Channel<EsimDetailsEvent>()
    val events = _events.receiveAsFlow()

    // Get eSIM ID from navigation arguments
    private val routeArgs = savedStateHandle.toRoute<Route.EsimDetailsRoute>()

    val state = _state
        .onStart {
            if (_state.value.esim == null) {
                loadEsimDetails()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = EsimDetailsState()
        )

    fun onAction(action: EsimDetailsAction) {
        when (action) {
            EsimDetailsAction.Refresh -> loadEsimDetails()
            EsimDetailsAction.BackClick -> Unit // Handled in UI

            EsimDetailsAction.ShowQrCode -> {
                _state.update { it.copy(showQrCodeSheet = true) }
            }
            EsimDetailsAction.DismissQrCode -> {
                _state.update { it.copy(showQrCodeSheet = false) }
            }

            EsimDetailsAction.ShowOperators -> {
                _state.update { it.copy(showOperatorsSheet = true) }
            }
            EsimDetailsAction.DismissOperators -> {
                _state.update { it.copy(showOperatorsSheet = false) }
            }

            EsimDetailsAction.StartEditLabel -> startEditLabel()
            is EsimDetailsAction.UpdateLabelText -> {
                _state.update { it.copy(editLabelText = action.text) }
            }
            EsimDetailsAction.SaveLabel -> saveLabel()
            EsimDetailsAction.CancelEditLabel -> cancelEditLabel()

            EsimDetailsAction.TopUpClick -> {
                viewModelScope.launch {
                    _events.send(EsimDetailsEvent.NavigateToTopUp(routeArgs.esimId))
                }
            }

            EsimDetailsAction.DeleteClick -> {
                // TODO: Show delete confirmation dialog
            }
            EsimDetailsAction.ConfirmDelete -> {
                // TODO: Delete eSIM
            }
            EsimDetailsAction.CancelDelete -> {
                // TODO: Dismiss delete dialog
            }
        }
    }

    private fun loadEsimDetails() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

           /* when (val result = repository.getEsimById(routeArgs.esimId)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            esim = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            }*/
        }
    }

    private fun startEditLabel() {
        val currentLabel = _state.value.esim?.userLabel ?: ""
        _state.update {
            it.copy(
                isEditingLabel = true,
                editLabelText = currentLabel
            )
        }
    }

    private fun saveLabel() {
        val newLabel = _state.value.editLabelText.trim()
        if (newLabel.isEmpty()) {
            cancelEditLabel()
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUpdatingLabel = true) }

            /*when (val result = repository.updateEsimLabel(routeArgs.esimId, newLabel)) {
                is Resource.Success -> {
                    // Update local state with new label
                    _state.update { state ->
                        state.copy(
                            isUpdatingLabel = false,
                            isEditingLabel = false,
                            esim = state.esim?.copy(userLabel = newLabel)
                        )
                    }
                    _events.send(EsimDetailsEvent.ShowMessage("ლეიბლი განახლდა"))
                }
                is Resource.Failure -> {
                    _state.update { it.copy(isUpdatingLabel = false) }
                    _events.send(EsimDetailsEvent.ShowError("ლეიბლის განახლება ვერ მოხერხდა"))
                }
            }*/
        }
    }

    private fun cancelEditLabel() {
        _state.update {
            it.copy(
                isEditingLabel = false,
                editLabelText = ""
            )
        }
    }
}

/**
 * One-shot events emitted by the ViewModel.
 */
sealed interface EsimDetailsEvent {
    data class NavigateToTopUp(val esimId: String) : EsimDetailsEvent
    data class ShowMessage(val message: String) : EsimDetailsEvent
    data class ShowError(val message: String) : EsimDetailsEvent
}