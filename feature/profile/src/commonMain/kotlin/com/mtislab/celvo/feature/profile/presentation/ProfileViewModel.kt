package com.mtislab.celvo.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.profile.domain.repository.ProfileRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())

    val state = _state
        .onStart {
            if (_state.value.userProfile == null) {
                loadProfile()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLogoutClick -> logout()
            ProfileAction.OnRetry -> loadProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null)
            }

            when (val result = profileRepository.getUserProfile()) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            userProfile = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Failure -> {
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

    private fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}