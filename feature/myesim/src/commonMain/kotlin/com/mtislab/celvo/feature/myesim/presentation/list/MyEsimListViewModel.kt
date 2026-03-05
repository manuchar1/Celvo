package com.mtislab.celvo.feature.myesim.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.core.domain.esim.EsimInstallStatus
import com.mtislab.core.domain.esim.InstallEsimUseCase
import com.mtislab.core.domain.esim.InstallError
import com.mtislab.core.domain.logging.CelvoLogger
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for My eSIM List screen.
 *
 * ## Installation Flow (post-refactor)
 *
 * 1. User taps "Activate" → [ActivateClick] → [activateEsim] collects install flow
 * 2. Flow emits [Installing] → show progress on the specific eSIM card
 * 3. Flow emits [ResolutionRequired] → UI launches PendingIntent, dispatches [ResolutionLaunched]
 * 4. Flow emits [Success] or [Error] → update state, refresh list on success
 *
 * CRITICAL: The install flow is NOT cancelled after [ResolutionRequired].
 * The [AndroidEsimInstaller]'s BroadcastReceiver stays alive and catches the
 * follow-up broadcast from Samsung/EuiccManager with the real result.
 * The ViewModel's installJob keeps collecting until a terminal state arrives.
 */
class MyEsimListViewModel(
    private val repository: MyEsimRepository,
    private val installEsimUseCase: InstallEsimUseCase,
    private val logger: CelvoLogger
) : ViewModel() {

    private val _state = MutableStateFlow(MyEsimListState())

    private var loadJob: Job? = null
    private var installJob: Job? = null

    val state = _state
        .onStart {
            if (_state.value.esims.isEmpty()) {
                loadEsims(forceRefresh = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MyEsimListState()
        )

    fun onAction(action: MyEsimListAction) {
        when (action) {
            MyEsimListAction.LoadEsims,
            MyEsimListAction.RetryClick -> loadEsims(forceRefresh = true)

            // Navigation — handled by UI
            is MyEsimListAction.TopUpClick -> Unit
            is MyEsimListAction.EsimClick -> Unit
            is MyEsimListAction.DetailsClick -> Unit
            MyEsimListAction.AddEsimClick -> Unit

            // Installation
            is MyEsimListAction.ActivateClick -> activateEsim(action.esim)

            // Resolution — just clear the PendingIntent from state
            // The install flow continues collecting in the background
            MyEsimListAction.ResolutionLaunched -> {
                logger.debug("[MyEsimListViewModel] Resolution launched — clearing resolutionRequired")
                _state.update { it.copy(resolutionRequired = null) }
            }

            MyEsimListAction.DismissError -> {
                _state.update { it.copy(installationError = null) }
            }

            else -> {}
        }
    }

    /**
     * Initiates eSIM activation flow.
     *
     * The install flow emits states that drive the UI:
     * - [Installing] → show spinner on the eSIM card
     * - [ResolutionRequired] → UI launches the PendingIntent
     * - [Success] → refresh eSIM list
     * - [Error] → show error message
     *
     * IMPORTANT: We do NOT cancel installJob when ResolutionRequired is emitted.
     * The flow stays open to receive the follow-up broadcast from Samsung.
     */
    private fun activateEsim(esim: UserEsim) {
        // Cancel any previous installation attempt
        installJob?.cancel()

        installJob = viewModelScope.launch {
            logger.info("[MyEsimListViewModel] Starting activation for eSIM: ${esim.id}")
            logger.debug("[MyEsimListViewModel] SMDP: ${esim.installation.smdpAddress}")

            installEsimUseCase(
                smdpAddress = esim.installation.smdpAddress,
                activationCode = esim.installation.activationCode,
                manualCode = esim.installation.manualCode
            ).collect { status ->
                logger.debug("[MyEsimListViewModel] Install status: $status")

                when (status) {
                    EsimInstallStatus.Idle -> Unit

                    EsimInstallStatus.Installing -> {
                        _state.update {
                            it.copy(
                                isInstalling = true,
                                installingEsimId = esim.id,
                                installationError = null,
                                resolutionRequired = null
                            )
                        }
                    }

                    EsimInstallStatus.Success -> {
                        logger.info("[MyEsimListViewModel] Installation successful for eSIM: ${esim.id}")
                        _state.update {
                            it.copy(
                                isInstalling = false,
                                installingEsimId = null,
                                installationError = null,
                                resolutionRequired = null
                            )
                        }
                        // Refresh to get updated eSIM status from server
                        loadEsims(forceRefresh = true)
                    }

                    is EsimInstallStatus.ResolutionRequired -> {
                        logger.debug("[MyEsimListViewModel] Resolution required — passing to UI")
                        // Keep isInstalling = true so the UI shows the spinner
                        // while Samsung's consent dialog is visible.
                        // The flow is still alive — AndroidEsimInstaller's
                        // BroadcastReceiver will emit Success/Error next.
                        _state.update {
                            it.copy(
                                resolutionRequired = status.resolutionData
                            )
                        }
                    }

                    is EsimInstallStatus.Error -> {
                        logger.error("[MyEsimListViewModel] Installation error: ${status.error}")
                        _state.update {
                            it.copy(
                                isInstalling = false,
                                installingEsimId = null,
                                installationError = mapErrorToInstallationError(status.error),
                                resolutionRequired = null
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Maps [InstallError] to [InstallationError] with Georgian user-facing messages.
     */
    private fun mapErrorToInstallationError(error: InstallError): InstallationError {
        return when (error) {
            InstallError.DeviceNotSupported -> InstallationError.deviceNotSupported()
            InstallError.NotAllowed -> InstallationError(
                message = "eSIM ინსტალაცია არ არის ნებადართული",
                type = InstallationError.Type.DEVICE_ERROR
            )
            InstallError.SystemError -> InstallationError.systemError()
            InstallError.Cancelled -> InstallationError.userCancelled()
            InstallError.InvalidActivationCode -> InstallationError.invalidCode()
            InstallError.InsufficientMemory -> InstallationError(
                message = "არასაკმარისი მეხსიერება eSIM-ისთვის",
                type = InstallationError.Type.DEVICE_ERROR
            )
            InstallError.CarrierLocked -> InstallationError(
                message = "მოწყობილობა დაბლოკილია ოპერატორზე",
                type = InstallationError.Type.CARRIER_ERROR
            )
            InstallError.Timeout -> InstallationError.networkError()
        }
    }

    /**
     * Smart loading strategy for eSIMs.
     *
     * 1. If we have cached data, show it immediately (no loading indicator)
     * 2. Always sync with server in background
     * 3. Update UI when fresh data arrives
     * 4. Only show error if we have no data at all
     */
    private fun loadEsims(forceRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val hasExistingData = _state.value.esims.isNotEmpty()

            if (!hasExistingData) {
                _state.update { it.copy(isLoading = true, error = null) }
            }

            // Fast load from cache
            if (!forceRefresh && !hasExistingData) {
                when (val result = repository.getActiveEsims(sync = false)) {
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                esims = result.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Failure -> {
                        logger.debug("[MyEsimListViewModel] Cache miss, proceeding to network sync")
                    }
                }
            }

            // Network sync
            when (val syncResult = repository.getActiveEsims(sync = true)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            esims = syncResult.data,
                            error = null
                        )
                    }
                }
                is Resource.Failure -> {
                    if (_state.value.esims.isEmpty()) {
                        _state.update {
                            it.copy(isLoading = false, error = syncResult.error)
                        }
                    } else {
                        logger.warn("[MyEsimListViewModel] Network sync failed, showing cached data")
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}