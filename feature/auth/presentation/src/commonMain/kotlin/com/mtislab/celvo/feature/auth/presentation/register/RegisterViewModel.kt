package com.mtislab.celvo.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.core.domain.auth.AppleAuthProvider
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
            is RegisterAction.OnAppleSignInClick -> loginWithApple(action.provider)
        }
    }

    private fun loginWithApple(provider: AppleAuthProvider?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            if (provider != null) {
                // --- NATIVE APPLE SIGN-IN (via ASAuthorization) ---
                // Try native first; if the provider returns a token, use the
                // fast IDToken path (no browser redirect).
                when (val tokenResult = provider.getAppleIdToken()) {
                    is Resource.Success -> {
                        handleAuthResult(authRepository.signInWithAppleNative(tokenResult.data))
                    }
                    is Resource.Failure -> {
                        // Native unavailable — fall back to web OAuth.
                        handleAuthResult(authRepository.signInWithApple())
                    }
                }
            } else {
                // --- WEB FALLBACK ---
                handleAuthResult(authRepository.signInWithApple())
            }
        }
    }

   /* private fun loginWithGoogle(provider: GoogleAuthProvider?) {
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
    }*/

    private fun loginWithGoogle(provider: GoogleAuthProvider?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            if (provider == null) {
                _state.update { it.copy(isLoading = false, error = DataError.Local.UNKNOWN) }
                return@launch
            }

            // ახლა iOS-იც და Android-იც ერთსა და იმავე Native ლოგიკას გამოიყენებს!
            when(val tokenResult = provider.getGoogleIdToken()) {
                is Resource.Success -> {
                    // ორივე პლატფორმა პირდაპირ idToken-ს აწვდის Supabase-ს
                    handleAuthResult(authRepository.signInWithGoogle(tokenResult.data))
                }
                is Resource.Failure -> {
                    _state.update { it.copy(isLoading = false, error = tokenResult.error) }
                }
            }
        }
    }


    private suspend fun handleAuthResult(result: Resource<AuthData, DataError.Remote>) {
        when(result) {
            is Resource.Success -> {
                val data = result.data

                sessionManager.onLoginSuccess(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    userId = data.userId
                )

                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            }
            is Resource.Failure -> {
                _state.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}