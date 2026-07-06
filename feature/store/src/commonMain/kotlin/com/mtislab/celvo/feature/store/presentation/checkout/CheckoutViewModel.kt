package com.mtislab.celvo.feature.store.presentation.checkout

import CheckoutState
import PaymentMethod
import PromoState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.error_3ds_redirect_failed
import celvo.feature.store.generated.resources.error_auth_failed
import celvo.feature.store.generated.resources.error_bad_request
import celvo.feature.store.generated.resources.error_enter_promo_code
import celvo.feature.store.generated.resources.error_generic_try_again
import celvo.feature.store.generated.resources.error_no_internet
import celvo.feature.store.generated.resources.error_payment_failed
import celvo.feature.store.generated.resources.error_price_updated_retry
import celvo.feature.store.generated.resources.error_promo_invalid
import celvo.feature.store.generated.resources.error_server_error_try_later
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateRequest
import com.mtislab.celvo.feature.store.domain.model.PromoValidationRequest
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentRequest
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentStatus
import com.mtislab.celvo.feature.store.domain.model.WalletType
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AppleAuthProvider
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.auth.GoogleAuthProvider
import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.payment.NativePayManager
import com.mtislab.core.domain.payment.PendingPaymentStore
import com.mtislab.core.domain.repository.AuthRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.presentation.util.UiText
import kotlin.math.roundToInt
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
    private val nativePayManager: NativePayManager
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    private val _events = Channel<CheckoutEvent>()
    val events = _events.receiveAsFlow()

    private val routeArgs = savedStateHandle.toRoute<Route.CheckoutRoute>()

    init {
        loadPackage(routeArgs.packageId)
        observeSession()
        checkWalletAvailability()
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
            is CheckoutAction.WalletTokenReceived ->
                processWalletPayment(token = action.token, walletType = action.walletType)

            is CheckoutAction.WalletPaymentCancelled -> {
                _state.update { it.copy(isLoading = false) }
            }

            is CheckoutAction.WalletPaymentFailed -> {
                _state.update { it.copy(isLoading = false, error = action.message) }
                viewModelScope.launch {
                    _events.send(CheckoutEvent.ShowError(UiText.DynamicString(action.message)))
                }
            }

            // --- Auth ---
            is CheckoutAction.DismissLoginSheet -> {
                _state.update { it.copy(showLoginSheet = false) }
            }

            is CheckoutAction.LoginWithGoogle -> loginWithGoogle(action.provider)
            is CheckoutAction.LoginWithApple -> loginWithApple(action.provider)

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
            PaymentMethod.NATIVE_WALLET -> launchWalletPayment(pkg.id)
            PaymentMethod.CARD -> initiateCardPayment()
        }
    }

    /**
     * Step 1 of the wallet flow: fetch the server-authoritative GEL amount and
     * open the native sheet with exactly it.
     *
     * The wallet token is cryptographically bound to the amount/currency the
     * user authorizes on the sheet, and Georgian Card compares them against the
     * BOG order (which settles the authoritative GEL amount) — opening the
     * sheet with the catalogue USD price fails every payment with
     * AMOUNT_MISMATCH. The quote is fetched fresh on every attempt so a stale
     * NBG rate can't linger; /wallet-pay re-validates and answers 409 if the
     * rate rolls over between the quote and the payment.
     *
     * Step 2 continues when WalletTokenReceived action arrives.
     */
    private fun launchWalletPayment(sku: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = storeRepository.getWalletQuote(sku)) {
                is Resource.Success -> {
                    val quote = result.data
                    _state.update { it.copy(walletQuote = quote) }
                    _events.send(
                        CheckoutEvent.LaunchNativeWalletPayment(
                            amountCents = (quote.amount * 100).roundToInt(),
                            currencyCode = quote.currency
                        )
                    )
                }

                is Resource.Failure -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.error.toString())
                    }
                    val messageRes = when (result.error) {
                        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
                        DataError.Remote.SERVER_ERROR -> Res.string.error_server_error_try_later
                        else -> Res.string.error_payment_failed
                    }
                    _events.send(CheckoutEvent.ShowError(UiText.Resource(messageRes)))
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // NEW: Wallet Payment — send token to Celvo backend
    // -------------------------------------------------------------------------

    private fun processWalletPayment(token: String, walletType: WalletType) {
        val currentState = _state.value
        val pkg = currentState.packageDetails ?: return

        // The sheet was opened with the quoted amount; the same values MUST go
        // to /wallet-pay. A missing quote means the sheet was somehow launched
        // without one — refuse rather than send an amount the user didn't see.
        val quote = currentState.walletQuote
        if (quote == null) {
            _state.update { it.copy(isLoading = false) }
            viewModelScope.launch {
                _events.send(
                    CheckoutEvent.ShowError(UiText.Resource(Res.string.error_payment_failed))
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val request = WalletPaymentRequest(
                sku = pkg.id,
                bundleName = pkg.name,
                amount = quote.amount,
                currency = quote.currency,
                paymentMethod = walletType,
                walletToken = token,
                promoCodeId = currentState.promo.appliedResult?.codeId
            )

            when (val result = storeRepository.processWalletPayment(request)) {
                is Resource.Success -> {
                    val payment = result.data
                    _state.update { it.copy(isLoading = false) }

                    // Cache orderId for deep link fallback
                    PendingPaymentStore.storeOrderId(payment.orderId)

                    when (payment.status) {
                        WalletPaymentStatus.COMPLETED -> {
                            // Navigate to PaymentVerification so the screen can poll
                            // /verify/{orderId} and surface the eSIM provisioning state.
                            // Even on synchronous wallet COMPLETED, provisioning is an
                            // async backend step — the verify poll handles both.
                            _events.send(
                                CheckoutEvent.NavigateToPaymentResult(
                                    isSuccess = true,
                                    orderId = payment.orderId
                                )
                            )
                        }

                        WalletPaymentStatus.REQUIRES_3DS -> {
                            // Redirect to 3DS page in browser. orderId was cached above
                            // in PendingPaymentStore, so the deep-link return path will
                            // resolve into PaymentVerification automatically.
                            payment.redirectUrl?.let { url ->
                                _events.send(CheckoutEvent.OpenWebUrl(url))
                            } ?: _events.send(
                                CheckoutEvent.ShowError(
                                    UiText.Resource(Res.string.error_3ds_redirect_failed)
                                )
                            )
                        }

                        WalletPaymentStatus.FAILED -> {
                            // Still route through PaymentVerification (with the orderId)
                            // so the same error UI is shown whether the payment failed
                            // synchronously here or asynchronously via BOG callback.
                            _events.send(
                                CheckoutEvent.NavigateToPaymentResult(
                                    isSuccess = false,
                                    orderId = payment.orderId
                                )
                            )
                        }
                    }
                }

                is Resource.Failure -> {
                    // Clear the quote so the next attempt re-fetches a fresh one.
                    _state.update { it.copy(isLoading = false, walletQuote = null) }
                    val messageRes = when (result.error) {
                        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
                        DataError.Remote.SERVER_ERROR -> Res.string.error_server_error_try_later
                        // 409 WALLET_AMOUNT_MISMATCH: the NBG rate rolled over
                        // between the quote and the payment. Nothing was charged —
                        // the user just needs to confirm again at the fresh price.
                        DataError.Remote.CONFLICT -> Res.string.error_price_updated_retry
                        else -> Res.string.error_payment_failed
                    }
                    _state.update { it.copy(error = result.error.toString()) }
                    _events.send(CheckoutEvent.ShowError(UiText.Resource(messageRes)))
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
                    // Cache orderId for deep link fallback.
                    // Currently the backend only returns redirectUrl. When orderId
                    // is added to InitiateResponse, this will automatically work.
                    result.data.orderId?.let { PendingPaymentStore.storeOrderId(it) }
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
                it.copy(
                    promo = it.promo.copy(
                        errorMessage = UiText.Resource(Res.string.error_enter_promo_code)
                    )
                )
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
                        val errorText = validation.errorMessage
                            ?.let { UiText.DynamicString(it) }
                            ?: UiText.Resource(Res.string.error_promo_invalid)
                        _state.update {
                            it.copy(
                                promo = it.promo.copy(
                                    isValidating = false,
                                    errorMessage = errorText
                                )
                            )
                        }
                    }
                }

                is Resource.Failure -> {
                    val messageRes = when (result.error) {
                        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
                        DataError.Remote.SERVER_ERROR -> Res.string.error_server_error_try_later
                        DataError.Remote.BAD_REQUEST -> Res.string.error_bad_request
                        else -> Res.string.error_generic_try_again
                    }
                    _state.update {
                        it.copy(
                            promo = it.promo.copy(
                                isValidating = false,
                                errorMessage = UiText.Resource(messageRes)
                            )
                        )
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
                    is Resource.Success -> handleAuthResult(
                        authRepository.signInWithGoogle(
                            tokenResult.data
                        )
                    )

                    is Resource.Failure -> _state.update {
                        it.copy(isLoading = false, error = tokenResult.error.toString())
                    }
                }
            } else {
                handleAuthResult(authRepository.signInWithGoogleWeb())
            }
        }
    }

    private fun loginWithApple(provider: AppleAuthProvider?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            if (provider != null) {
                // Native Apple Sign-In (iOS) — same fast IDToken path the login
                // screen uses. Falls back to the web flow if the token fails.
                when (val tokenResult = provider.getAppleIdToken()) {
                    is Resource.Success -> {
                        val (idToken, nonce) = tokenResult.data
                        handleAuthResult(
                            authRepository.signInWithAppleNative(
                                idToken = idToken,
                                nonce = nonce
                            )
                        )
                    }

                    is Resource.Failure -> _state.update {
                        it.copy(isLoading = false, error = tokenResult.error.toString())
                    }
                }
            } else {
                handleAuthResult(authRepository.signInWithApple())
            }
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
                _events.send(
                    CheckoutEvent.ShowError(
                        UiText.Resource(
                            Res.string.error_auth_failed,
                            arrayOf(result.error.toString())
                        )
                    )
                )
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
