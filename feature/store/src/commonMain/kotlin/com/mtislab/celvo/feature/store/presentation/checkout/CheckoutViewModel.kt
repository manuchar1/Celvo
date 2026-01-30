package com.mtislab.celvo.feature.store.presentation.checkout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mtislab.celvo.feature.store.domain.model.PaymentInitiateRequest
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.auth.GoogleAuthProvider
import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.model.Route
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    // ✅ One-Shot Events (URL-ის გასახსნელად ან შეცდომის საჩვენებლად)
    private val _events = Channel<CheckoutEvent>()
    val events = _events.receiveAsFlow()

    private val routeArgs = savedStateHandle.toRoute<Route.CheckoutRoute>()

    init {
        loadPackage(routeArgs.packageId)
        observeSession()
    }

    /**
     * უსმენს სესიის სტატუსს.
     * თუ მომხმარებელი დალოგინდა და LoginSheet ღია იყო,
     * ავტომატურად ხურავს მას და იწყებს გადახდას (Auto-Resume).
     */
    private fun observeSession() {
        sessionManager.state
            .onEach { authState ->
                val isLoggedIn = authState is AuthState.Authenticated

                // განვაახლოთ UI სთეითი
                _state.update {
                    it.copy(isLoggedIn = isLoggedIn)
                }

                // 🔥 Auto-Resume Logic:
                // თუ ახლახანს შევიდა და შითი ღიაა -> ვხურავთ და ვაგრძელებთ გადახდას
                if (isLoggedIn && _state.value.showLoginSheet) {
                    _state.update { it.copy(showLoginSheet = false) }
                    initiatePayment() // 🚀 ავტომატური გაგრძელება
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CheckoutAction) {
        when (action) {
            is CheckoutAction.PayClicked -> handlePayClick()

            is CheckoutAction.DismissLoginSheet -> {
                _state.update { it.copy(showLoginSheet = false) }
            }

            // ✅ Google Login Implementation
            is CheckoutAction.LoginWithGoogle -> loginWithGoogle(action.provider)

            // ✅ Apple Login Implementation
            is CheckoutAction.LoginWithApple -> loginWithApple()

            is CheckoutAction.ToggleAutoTopup -> {
                _state.update { it.copy(isAutoTopupEnabled = action.enabled) }
            }

            is CheckoutAction.SelectTopupOption -> {
                _state.update { it.copy(selectedTopupOption = action.option) }
            }

            else -> {}
        }
    }

    private fun handlePayClick() {
        if (_state.value.isLoggedIn) {
            initiatePayment()
        } else {
            _state.update { it.copy(showLoginSheet = true) }
        }
    }

    // --- AUTHENTICATION LOGIC ---

    private fun loginWithGoogle(provider: GoogleAuthProvider?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            if (provider != null) {
                // --- ANDROID SCENARIO (Native Credential Manager) ---
                when (val tokenResult = provider.getGoogleIdToken()) {
                    is Resource.Success -> {
                        // ტოკენი მივიღეთ, ვაგზავნით Supabase-ში
                        handleAuthResult(authRepository.signInWithGoogle(tokenResult.data))
                    }
                    is Resource.Failure -> {
                        _state.update {
                            it.copy(isLoading = false, error = tokenResult.error.toString())
                        }
                    }
                }
            } else {
                // --- iOS / WEB SCENARIO ---
                // Supabase აგვარებს ბრაუზერით
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

    private fun handleAuthResult(result: Resource<AuthData, DataError.Remote>) {
        when (result) {
            is Resource.Success -> {
                val data = result.data
                // 💾 ვინახავთ სესიას.
                // შენიშვნა: UI-ს განახლება (isLoading = false, sheet close)
                // მოხდება observeSession()-ში, როცა SessionManager ახალ სთეითს დააემიტებს.
                sessionManager.onLoginSuccess(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    userId = data.userId
                )
            }
            is Resource.Failure -> {
                _state.update {
                    it.copy(isLoading = false, error = result.error.toString())
                }
                // ოპციური: ვაჩვენოთ Error Event-იც
                viewModelScope.launch {
                    _events.send(CheckoutEvent.ShowError("ავტორიზაციის შეცდომა: ${result.error}"))
                }
            }
        }
    }

    // --- PAYMENT LOGIC ---

    private fun initiatePayment() {
        val currentState = _state.value
        val pkg = currentState.packageDetails ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // 2. ვქმნით Request-ს
            // ⚠️ მნიშვნელოვანია: BOG/Stripe ხშირად ითხოვს თანხას თეთრებში/ცენტებში (Integer).
            // ამიტომ 55.00 ლარი -> 5500 თეთრი.
            val amountInTetri = (pkg.price * 100).toInt()

            val request = PaymentInitiateRequest(
                amount = pkg.price,
                sku = pkg.id,
                bundleName = pkg.name,
                currency = "GEL", // pkg.currency
                language = "ka", // ან დინამიურად ენის მიხედვით
                theme = "dark" // ან დინამიურად
            )

            // 3. ვიძახებთ API-ს
            val result = storeRepository.initiatePayment(request)

            _state.update { it.copy(isLoading = false) }

            // 4. ვამუშავებთ შედეგს
            when (result) {
                is Resource.Success -> {
                    // 🎉 წარმატება! ვუგზავნით ლინკს UI-ს
                    _events.send(CheckoutEvent.OpenWebUrl(result.data.redirectUrl))
                }
                is Resource.Failure -> {
                    // ❌ შეცდომა
                    val errorMessage = result.error.toString()
                    _state.update { it.copy(error = errorMessage) }
                    _events.send(CheckoutEvent.ShowError("გადახდის დაწყება ვერ მოხერხდა: $errorMessage"))
                }
            }
        }
    }

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