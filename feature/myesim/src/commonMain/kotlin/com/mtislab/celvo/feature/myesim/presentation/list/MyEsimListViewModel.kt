package com.mtislab.celvo.feature.myesim.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.logging.CelvoLogger
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MyEsimListUiEvent {
    data class OpenUrl(val url: String) : MyEsimListUiEvent
}

class MyEsimListViewModel(
    private val repository: MyEsimRepository,
    private val linkGenerator: EsimLinkGenerator,
    private val logger: CelvoLogger
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
                    smdpAddress = installation.smdpAddress,
                    activationCode = installation.activationCode
                )
            }
            is MyEsimListAction.DismissError -> {
                _state.update { it.copy(error = null, installationError = null) }
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
                            error = null
                        )
                    }
                }
                is Resource.Failure -> {
                    logger.error("[MyEsimList] Failed to load eSIMs: ${result.error}", null)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            }
        }
    }

    private fun handleActivateClick(smdpAddress: String, activationCode: String) {
        if (smdpAddress.isBlank() || activationCode.isBlank()) {
            logger.warn("[MyEsimList] ActivateClick aborted — smdpAddress or activationCode is blank")
            return
        }
        viewModelScope.launch {
            try {
                val url = linkGenerator.generateInstallLink(
                    smdpAddress = smdpAddress,
                    activationCode = activationCode
                )
                logger.info("[MyEsimList] Opening install URL: $url")
                _uiEvent.send(MyEsimListUiEvent.OpenUrl(url))
            } catch (e: Exception) {
                logger.error("[MyEsimList] Failed to generate install link", e)
                _state.update { it.copy(installationError = "ინსტალაციის გმული ვერ შეიქმნა") }
            }
        }
    }
}