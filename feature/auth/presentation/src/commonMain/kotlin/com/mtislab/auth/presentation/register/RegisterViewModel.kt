package com.mtislab.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.core.domain.repository.AuthRepository
import com.mtislab.core.domain.utils.Resource
import com.mtislab.domain.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
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
        }
    }

    private fun loginWithGoogle(provider: GoogleAuthProvider) {
        viewModelScope.launch {

            _state.update { it.copy(isLoading = true, error = null) }

            when(val tokenResult = provider.getGoogleIdToken()) {
                is Resource.Success -> {
                    when(val authResult = authRepository.signInWithGoogle(tokenResult.data)) {
                        is Resource.Success -> {
                            _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                        }
                        is Resource.Failure -> {
                            _state.update { it.copy(isLoading = false, error = authResult.error) }
                        }
                    }
                }
                is Resource.Failure -> {
                    println("Google Sign-In Error: ${tokenResult.error}")
                    _state.update { it.copy(isLoading = false, error = tokenResult.error) }
                }
            }
        }
    }
}