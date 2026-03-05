package com.mtislab.celvo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())

    val state = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MainScreenState()
    )

    init {
        viewModelScope.launch {
            sessionManager.state.collect { authState ->
                _state.update {
                    it.copy(
                        isAuthLoading = false,                              // ✅ Auth state is now known
                        isLoggedIn = authState is AuthState.Authenticated
                    )
                }
            }
        }
    }

    fun onAction(action: MainScreenAction) {
        when (action) {
            else -> {}
        }
    }
}