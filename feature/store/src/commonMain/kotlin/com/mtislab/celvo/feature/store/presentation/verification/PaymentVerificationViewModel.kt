package com.mtislab.celvo.feature.store.presentation.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.verification_error_generic
import celvo.feature.store.generated.resources.verification_error_no_internet
import celvo.feature.store.generated.resources.verification_error_not_found
import celvo.feature.store.generated.resources.verification_error_payment_failed
import celvo.feature.store.generated.resources.verification_error_payment_refunded
import celvo.feature.store.generated.resources.verification_error_request_timeout
import celvo.feature.store.generated.resources.verification_error_server
import celvo.feature.store.generated.resources.verification_error_timeout
import celvo.feature.store.generated.resources.verification_error_unauthorized
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.payment.OrderType
import com.mtislab.core.domain.payment.VerificationStatus
import com.mtislab.core.domain.payment.VerifyPaymentUseCase
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.presentation.util.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PaymentVerificationViewModel(
    savedStateHandle: SavedStateHandle,
    private val verifyPaymentUseCase: VerifyPaymentUseCase,
    private val linkGenerator: EsimLinkGenerator
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<Route.PaymentVerification>()
    private val orderId: String = routeArgs.orderId

    private val _state = MutableStateFlow<PaymentVerificationState>(PaymentVerificationState.Loading)
    val state = _state.asStateFlow()

    private val _events = Channel<PaymentVerificationEvent>()
    val events = _events.receiveAsFlow()

    private var pollingJob: Job? = null

    init {
        verifyPayment()
    }

    fun onAction(action: PaymentVerificationAction) {
        when (action) {
            is PaymentVerificationAction.RetryClicked -> verifyPayment()
            is PaymentVerificationAction.InstallEsimClicked -> installEsim()
            is PaymentVerificationAction.GoToDashboardClicked -> {
                viewModelScope.launch {
                    _events.send(PaymentVerificationEvent.NavigateToHome)
                }
            }
            is PaymentVerificationAction.GoToMyEsimsClicked -> {
                viewModelScope.launch {
                    _events.send(PaymentVerificationEvent.NavigateToMyEsims)
                }
            }
        }
    }

    private fun verifyPayment() {
        pollingJob?.cancel()
        _state.value = PaymentVerificationState.Loading

        pollingJob = viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 15 // 15 * 2s = 30s max polling
            val pollIntervalMs = 2000L

            while (attempts < maxAttempts) {
                when (val result = verifyPaymentUseCase(orderId)) {
                    is Resource.Success -> {
                        val data = result.data

                        when (data.status) {
                            VerificationStatus.COMPLETED -> {
                                when (data.orderType) {
                                    OrderType.NEW_ESIM -> {
                                        if (data.provisioningStatus == "PENDING") {
                                            // Provisioning still in progress, keep polling
                                            attempts++
                                            delay(pollIntervalMs)
                                            continue
                                        }
                                        val esimData = data.esimData
                                        if (esimData != null) {
                                            _state.value = PaymentVerificationState.SuccessNewEsim(
                                                esimData = esimData,
                                                bundleName = data.bundleName
                                            )
                                        } else {
                                            // Payment completed but provisioning data not ready
                                            attempts++
                                            delay(pollIntervalMs)
                                            continue
                                        }
                                    }
                                    OrderType.TOP_UP -> {
                                        _state.value = PaymentVerificationState.SuccessTopUp(
                                            bundleName = data.bundleName
                                        )
                                    }
                                }
                                return@launch
                            }

                            VerificationStatus.PENDING -> {
                                attempts++
                                delay(pollIntervalMs)
                                continue
                            }

                            VerificationStatus.FAILED -> {
                                _state.value = PaymentVerificationState.Error(
                                    message = UiText.Resource(Res.string.verification_error_payment_failed)
                                )
                                return@launch
                            }

                            VerificationStatus.REFUNDED -> {
                                _state.value = PaymentVerificationState.Error(
                                    message = UiText.Resource(Res.string.verification_error_payment_refunded)
                                )
                                return@launch
                            }
                        }
                    }

                    is Resource.Failure -> {
                        _state.value = PaymentVerificationState.Error(
                            message = mapErrorToUiText(result.error)
                        )
                        return@launch
                    }
                }
            }

            // Polling timeout
            if (_state.value is PaymentVerificationState.Loading) {
                _state.value = PaymentVerificationState.Error(
                    message = UiText.Resource(Res.string.verification_error_timeout)
                )
            }
        }
    }

    private fun installEsim() {
        val currentState = _state.value
        if (currentState !is PaymentVerificationState.SuccessNewEsim) return

        val installUrl = linkGenerator.generateInstallLink(
            smdpAddress = currentState.esimData.smdpAddress,
            activationCode = currentState.esimData.activationCode
        )
        viewModelScope.launch {
            _events.send(PaymentVerificationEvent.OpenEsimInstallUrl(installUrl))
        }
    }

    private fun mapErrorToUiText(error: DataError.Remote): UiText {
        return when (error) {
            DataError.Remote.NO_INTERNET -> UiText.Resource(Res.string.verification_error_no_internet)
            DataError.Remote.SERVER_ERROR -> UiText.Resource(Res.string.verification_error_server)
            DataError.Remote.UNAUTHORIZED -> UiText.Resource(Res.string.verification_error_unauthorized)
            DataError.Remote.NOT_FOUND -> UiText.Resource(Res.string.verification_error_not_found)
            DataError.Remote.REQUEST_TIMEOUT -> UiText.Resource(Res.string.verification_error_request_timeout)
            else -> UiText.Resource(Res.string.verification_error_generic)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
