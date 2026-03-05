package com.mtislab.celvo.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.core.domain.auth.GoogleAuthProvider
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.model.AuthData
import com.mtislab.core.domain.repository.AuthRepository
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterState()
        )

    fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.OnGoogleSignInClick -> loginWithGoogle(action.provider)
            is RegisterAction.OnAppleSignInClick -> loginWithApple()
        }
    }

    private fun loginWithApple() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Apple Logic: Supabase Handles everything
            // შედეგს ვატარებთ handleAuthResult-ში, რადგან ისიც აბრუნებს Resource<AuthData>
            handleAuthResult(authRepository.signInWithApple())
        }
    }

    private fun loginWithGoogle(provider: GoogleAuthProvider?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            if (provider != null) {
                // --- ANDROID SCENARIO (Native) ---
                // 1. Get Token from Credential Manager
                when(val tokenResult = provider.getGoogleIdToken()) {
                    is Resource.Success -> {
                        // 2. Send Token to Supabase & Handle Result
                        handleAuthResult(authRepository.signInWithGoogle(tokenResult.data))
                    }
                    is Resource.Failure -> {
                        // Error getting token from Android System
                        _state.update { it.copy(isLoading = false, error = tokenResult.error) }
                    }
                }
            } else {
                // --- iOS SCENARIO (Web Flow) ---
                // Supabase handles the flow via Browser
                handleAuthResult(authRepository.signInWithGoogleWeb())
            }
        }
    }

    // ✅ განახლებული Helper Function
    // იღებს AuthData-ს და ინახავს SessionManager-ში
    private fun handleAuthResult(result: Resource<AuthData, DataError.Remote>) {
        when(result) {
            is Resource.Success -> {
                val data = result.data

                // 💾 ვინახავთ სესიას ლოკალურად
                sessionManager.onLoginSuccess(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    userId = data.userId
                )

                // UI-ს ვატყობინებთ წარმატებას
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            }
            is Resource.Failure -> {
                _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}