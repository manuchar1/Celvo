package com.mtislab.celvo.feature.store.presentation.checkout

import CheckoutState
import PaymentMethod
import PromoState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateRequest
import com.mtislab.celvo.feature.store.domain.model.PromoValidationRequest
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentRequest
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentStatus
import com.mtislab.celvo.feature.store.domain.model.WalletType
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.auth.GoogleAuthProvider
import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.payment.NativePayManager
import com.mtislab.core.domain.repository.AuthRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository,
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val nativePayManager: NativePayManager // NEW: injected via Koin
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    private val _events = Channel<CheckoutEvent>()
    val events = _events.receiveAsFlow()

    private val routeArgs = savedStateHandle.toRoute<Route.CheckoutRoute>()

    init {
        loadPackage(routeArgs.packageId)
        observeSession()
        checkWalletAvailability() // NEW
    }

    // -------------------------------------------------------------------------
    // NEW: Check if native wallet (Google Pay / Apple Pay) is available
    // -------------------------------------------------------------------------

    private fun checkWalletAvailability() {
        viewModelScope.launch {
            val available = nativePayManager.isAvailable()
            _state.update {
                it.copy(
                    isWalletAvailable = available,
                    // If wallet is NOT available, default to CARD
                    selectedPaymentMethod = if (available) it.selectedPaymentMethod
                    else PaymentMethod.CARD
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Session Observation + Auto-Resume
    // -------------------------------------------------------------------------

    private fun observeSession() {
        sessionManager.state
            .onEach { authState ->
                val isLoggedIn = authState is AuthState.Authenticated
                _state.update { it.copy(isLoggedIn = isLoggedIn) }

                if (isLoggedIn && _state.value.showLoginSheet) {
                    _state.update { it.copy(showLoginSheet = false) }
                    // Resume payment after login
                    handlePayClick()
                }
            }
            .launchIn(viewModelScope)
    }

    // -------------------------------------------------------------------------
    // Action Dispatcher (MVI single entry-point)
    // -------------------------------------------------------------------------

    fun onAction(action: CheckoutAction) {
        when (action) {
            is CheckoutAction.PayClicked -> handlePayClick()

            is CheckoutAction.SelectPaymentMethod -> {
                _state.update { it.copy(selectedPaymentMethod = action.method) }
            }

            // --- Wallet Payment Actions (NEW) ---
            is CheckoutAction.WalletTokenReceived -> processWalletPayment(action.token)

            is CheckoutAction.WalletPaymentCancelled -> {
                _state.update { it.copy(isLoading = false) }
            }

            is CheckoutAction.WalletPaymentFailed -> {
                _state.update { it.copy(isLoading = false, error = action.message) }
                viewModelScope.launch {
                    _events.send(CheckoutEvent.ShowError(action.message))
                }
            }

            // --- Auth ---
            is CheckoutAction.DismissLoginSheet -> {
                _state.update { it.copy(showLoginSheet = false) }
            }
            is CheckoutAction.LoginWithGoogle -> loginWithGoogle(action.provider)
            is CheckoutAction.LoginWithApple -> loginWithApple()

            // --- Promo Code ---
            is CheckoutAction.OpenPromoSheet -> {
                _state.update {
                    it.copy(promo = it.promo.copy(showSheet = true, errorMessage = null))
                }
            }
            is CheckoutAction.DismissPromoSheet -> {
                _state.update {
                    it.copy(
                        promo = it.promo.copy(
                            showSheet = false,
                            errorMessage = null,
                            isValidating = false
                        )
                    )
                }
            }
            is CheckoutAction.PromoCodeChanged -> {
                _state.update {
                    it.copy(
                        promo = it.promo.copy(code = action.code, errorMessage = null)
                    )
                }
            }
            is CheckoutAction.ApplyPromoCode -> validatePromoCode()
            is CheckoutAction.ClearPromoCode -> {
                _state.update { it.copy(promo = PromoState()) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Pay Click — routes to wallet or card flow
    // -------------------------------------------------------------------------

    private fun handlePayClick() {
        if (!_state.value.isLoggedIn) {
            _state.update { it.copy(showLoginSheet = true) }
            return
        }

        val currentState = _state.value
        val pkg = currentState.packageDetails ?: return

        when (currentState.selectedPaymentMethod) {
            PaymentMethod.NATIVE_WALLET -> {
                // Step 1: Set loading, emit event for UI to launch Google Pay sheet
                _state.update { it.copy(isLoading = true, error = null) }
                val amountCents = (currentState.effectivePrice * 100).toInt()
                viewModelScope.launch {
                    _events.send(
                        CheckoutEvent.LaunchNativeWalletPayment(
                            amountCents = amountCents,
                            currencyCode = pkg.currency.uppercase()
                                .let { if (it == "₾" || it == "GEL") "GEL" else it }
                        )
                    )
                }
                // Step 2 continues when WalletTokenReceived action arrives
            }

            PaymentMethod.CARD -> initiateCardPayment()
        }
    }

    // -------------------------------------------------------------------------
    // NEW: Wallet Payment — send token to Celvo backend
    // -------------------------------------------------------------------------

    private fun processWalletPayment(token: String) {
        val currentState = _state.value
        val pkg = currentState.packageDetails ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val request = WalletPaymentRequest(
                sku = pkg.id,
                bundleName = pkg.name,
                amount = currentState.effectivePrice,
                currency = "GEL",
                paymentMethod = WalletType.GOOGLE_PAY,
                walletToken = token,
                promoCodeId = currentState.promo.appliedResult?.codeId
            )

            when (val result = storeRepository.processWalletPayment(request)) {
                is Resource.Success -> {
                    val payment = result.data
                    _state.update { it.copy(isLoading = false) }

                    when (payment.status) {
                        WalletPaymentStatus.COMPLETED -> {
                            _events.send(
                                CheckoutEvent.NavigateToPaymentResult(
                                    isSuccess = true,
                                    orderId = payment.orderId
                                )
                            )
                        }

                        WalletPaymentStatus.REQUIRES_3DS -> {
                            // Redirect to 3DS page in browser
                            payment.redirectUrl?.let { url ->
                                _events.send(CheckoutEvent.OpenWebUrl(url))
                            } ?: _events.send(
                                CheckoutEvent.ShowError("3DS გადამისამართება ვერ მოხერხდა")
                            )
                        }

                        WalletPaymentStatus.FAILED -> {
                            _events.send(
                                CheckoutEvent.NavigateToPaymentResult(isSuccess = false)
                            )
                        }
                    }
                }

                is Resource.Failure -> {
                    _state.update { it.copy(isLoading = false) }
                    val message = when (result.error) {
                        DataError.Remote.NO_INTERNET -> "ინტერნეტ კავშირი არ არის"
                        DataError.Remote.SERVER_ERROR -> "სერვერის შეცდომა, სცადეთ მოგვიანებით"
                        else -> "გადახდა ვერ მოხერხდა, სცადეთ თავიდან"
                    }
                    _state.update { it.copy(error = message) }
                    _events.send(CheckoutEvent.ShowError(message))
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Card Payment — existing redirect-based flow (renamed for clarity)
    // -------------------------------------------------------------------------

    private fun initiateCardPayment() {
        val currentState = _state.value
        val pkg = currentState.packageDetails ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val request = PaymentInitiateRequest(
                amount = currentState.effectivePrice,
                sku = pkg.id,
                bundleName = pkg.name,
                currency = "GEL",
                language = "ka",
                theme = "dark"
            )

            val result = storeRepository.initiatePayment(request)

            _state.update { it.copy(isLoading = false) }

            when (result) {
                is Resource.Success -> {
                    _events.send(CheckoutEvent.OpenWebUrl(result.data.redirectUrl))
                }
                is Resource.Failure -> {
                    val errorMessage = result.error.toString()
                    _state.update { it.copy(error = errorMessage) }
                    _events.send(
                        CheckoutEvent.NavigateToPaymentResult(isSuccess = false)
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Promo Code Validation (unchanged from previous implementation)
    // -------------------------------------------------------------------------

    private fun validatePromoCode() {
        val currentPromo = _state.value.promo
        val pkg = _state.value.packageDetails ?: return

        val trimmedCode = currentPromo.code.trim()
        if (trimmedCode.isEmpty()) {
            _state.update {
                it.copy(promo = it.promo.copy(errorMessage = "შეიყვანეთ პრომოკოდი"))
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(promo = it.promo.copy(isValidating = true, errorMessage = null))
            }

            val isoCode = routeArgs.region

            val request = PromoValidationRequest(
                code = trimmedCode,
                cartValue = pkg.price,
                bundleId = pkg.id,
                countryIso = pkg.isoCode,
                regionId = isoCode
            )

            when (val result = storeRepository.validatePromo(request)) {
                is Resource.Success -> {
                    val validation = result.data
                    if (validation.valid) {
                        _state.update {
                            it.copy(
                                promo = it.promo.copy(
                                    isValidating = false,
                                    appliedResult = validation,
                                    showSheet = false,
                                    errorMessage = null
                                )
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                promo = it.promo.copy(
                                    isValidating = false,
                                    errorMessage = validation.errorMessage
                                        ?: "პრომოკოდი არასწორია"
                                )
                            )
                        }
                    }
                }
                is Resource.Failure -> {
                    val message = when (result.error) {
                        DataError.Remote.NO_INTERNET -> "ინტერნეტ კავშირი არ არის"
                        DataError.Remote.SERVER_ERROR -> "სერვერის შეცდომა, სცადეთ მოგვიანებით"
                        DataError.Remote.BAD_REQUEST -> "არასწორი მონაცემები"
                        else -> "შეცდომა, სცადეთ თავიდან"
                    }
                    _state.update {
                        it.copy(promo = it.promo.copy(isValidating = false, errorMessage = message))
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Authentication (unchanged)
    // -------------------------------------------------------------------------

    private fun loginWithGoogle(provider: GoogleAuthProvider?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            if (provider != null) {
                when (val tokenResult = provider.getGoogleIdToken()) {
                    is Resource.Success -> handleAuthResult(authRepository.signInWithGoogle(tokenResult.data))
                    is Resource.Failure -> _state.update {
                        it.copy(isLoading = false, error = tokenResult.error.toString())
                    }
                }
            } else {
                handleAuthResult(authRepository.signInWithGoogleWeb())
            }
        }
    }

    private fun loginWithApple() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            handleAuthResult(authRepository.signInWithApple())
        }
    }

    private suspend fun handleAuthResult(result: Resource<AuthData, DataError.Remote>) {
        when (result) {
            is Resource.Success -> {
                sessionManager.onLoginSuccess(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                    userId = result.data.userId
                )
            }
            is Resource.Failure -> {
                _state.update { it.copy(isLoading = false, error = result.error.toString()) }
                _events.send(CheckoutEvent.ShowError("ავტორიზაცია ვერ მოხერხდა: ${result.error}"))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Data Loading (unchanged)
    // -------------------------------------------------------------------------

    private fun loadPackage(id: String) {
        if (_state.value.packageDetails?.id == id) return
        viewModelScope.launch {
            val pkg = storeRepository.getPackageById(id)
            if (pkg != null) {
                _state.update { it.copy(packageDetails = pkg) }
            }
        }
    }
}