package com.mtislab.celvo.feature.myesim.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.core.domain.connectivity.ConnectivityObserver
import com.mtislab.core.domain.connectivity.onBackOnline
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.logging.CelvoLogger
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.domain.utils.isConnectivityError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MyEsimListUiEvent {
    data class OpenUrl(val url: String) : MyEsimListUiEvent
    data object ShowInstallNotReady : MyEsimListUiEvent
    data object ShowInstallFailed : MyEsimListUiEvent
}

class MyEsimListViewModel(
    private val repository: MyEsimRepository,
    private val linkGenerator: EsimLinkGenerator,
    private val logger: CelvoLogger,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _state = MutableStateFlow(MyEsimListState())
    val state = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MyEsimListState()
    )

    private val _uiEvent = Channel<MyEsimListUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var loadingJob: kotlinx.coroutines.Job? = null

    init {
        observeConnectivity()
    }

    fun onAction(action: MyEsimListAction) {
        when (action) {
            is MyEsimListAction.LoadEsims -> loadEsims(sync = false)
            is MyEsimListAction.Refresh -> {
                logger.info("[MyEsimList] Refresh action triggered (ON_RESUME)")
                loadEsims(sync = true)
            }
            is MyEsimListAction.RetryClick -> loadEsims(sync = true)
            is MyEsimListAction.ActivateClick -> {
                val installation = action.esim.installation
                handleActivateClick(
                    esimId = action.esim.id,
                    smdpAddress = installation.smdpAddress,
                    activationCode = installation.activationCode
                )
            }
            else -> Unit
        }
    }

    private fun loadEsims(sync: Boolean) {
        // ✅ Guard checks the actual Job, not the UI state flag
        if (loadingJob?.isActive == true) {
            logger.debug("[MyEsimList] loadEsims skipped — already loading")
            return
        }

        loadingJob = viewModelScope.launch {
            logger.info("[MyEsimList] loadEsims START, sync=$sync")
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getActiveEsims(sync = sync)) {
                is Resource.Success -> {
                    logger.debug("[MyEsimList] Loaded ${result.data.size} eSIMs successfully")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            esims = result.data,
                            error = null,
                            isInstalling = false,
                            installingEsimId = null
                        )
                    }
                }
                is Resource.Failure -> {
                    logger.error("[MyEsimList] Failed to load eSIMs: ${result.error}", null)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.error,
                            isInstalling = false,
                            installingEsimId = null
                        )
                    }
                }
            }
        }
    }

    /**
     * Auto-recovers from a no-internet failure: when connectivity returns while
     * the list is still showing a connectivity error, the eSIMs are reloaded so
     * the placeholder gives way to content. A server error is left untouched —
     * the fox placeholder carries its own manual retry.
     */
    private fun observeConnectivity() {
        connectivityObserver.onBackOnline(viewModelScope) {
            if (_state.value.error?.isConnectivityError == true) {
                loadEsims(sync = true)
            }
        }
    }

    private fun handleActivateClick(esimId: String, smdpAddress: String, activationCode: String) {
        viewModelScope.launch {
            if (smdpAddress.isBlank() || activationCode.isBlank()) {
                logger.warn("[MyEsimList] ActivateClick aborted — smdpAddress or activationCode is blank")
                _uiEvent.send(MyEsimListUiEvent.ShowInstallNotReady)
                return@launch
            }

            _state.update { it.copy(isInstalling = true, installingEsimId = esimId) }
            try {
                val url = linkGenerator.generateInstallLink(
                    smdpAddress = smdpAddress,
                    activationCode = activationCode
                )
                logger.info("[MyEsimList] Opening install URL: $url")
                _uiEvent.send(MyEsimListUiEvent.OpenUrl(url))
                // Leave isInstalling = true until the user returns to the screen
                // (ON_RESUME triggers Refresh, which clears it). This prevents
                // double-taps while the OS installer is being handed off.
            } catch (e: Exception) {
                logger.error("[MyEsimList] Failed to generate install link", e)
                _uiEvent.send(MyEsimListUiEvent.ShowInstallFailed)
                _state.update { it.copy(isInstalling = false, installingEsimId = null) }
            }
        }
    }
}