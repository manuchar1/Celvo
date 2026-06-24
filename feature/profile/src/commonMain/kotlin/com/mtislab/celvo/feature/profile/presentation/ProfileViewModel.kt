package com.mtislab.celvo.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import celvo.feature.profile.generated.resources.Res
import celvo.feature.profile.generated.resources.profile_delete_account_error_partial
import celvo.feature.profile.generated.resources.profile_delete_account_error_session
import celvo.feature.profile.generated.resources.profile_delete_account_success
import com.mtislab.celvo.feature.profile.domain.model.DeleteAccountResult
import com.mtislab.celvo.feature.profile.domain.repository.ProfileRepository
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationType
import com.mtislab.core.domain.auth.SessionController
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionController: SessionController,
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

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLogoutClick -> logout()
            ProfileAction.OnRetry -> loadProfile()
            ProfileAction.OnDeleteAccountClick -> openDeleteDialog()
            ProfileAction.OnConfirmDelete -> deleteAccount()
            ProfileAction.OnCancelDelete -> dismissDeleteDialog()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

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
            _state.update { it.copy(isLoading = true) }
            sessionController.logout()
            _state.update { ProfileState() }
        }
    }

    private fun openDeleteDialog() {
        _state.update { it.copy(deletionStatus = DeletionStatus.Confirming) }
    }

    private fun dismissDeleteDialog() {
        // Ignore cancel while a delete is mid-flight — the user cannot revoke
        // an in-flight destructive request safely.
        if (_state.value.deletionStatus == DeletionStatus.Deleting) return
        _state.update { it.copy(deletionStatus = DeletionStatus.Idle) }
    }

    private fun deleteAccount() {
        val current = _state.value.deletionStatus
        if (current == DeletionStatus.Deleting) return
        if (current != DeletionStatus.Confirming && current != DeletionStatus.RetryableError) return

        viewModelScope.launch {
            _state.update { it.copy(deletionStatus = DeletionStatus.Deleting) }

            when (profileRepository.deleteAccount()) {
                DeleteAccountResult.Success -> {
                    _events.send(
                        ProfileEvent.ShowNotification(
                            messageRes = Res.string.profile_delete_account_success,
                            type = CelvoNotificationType.Success
                        )
                    )
                    finalizeLogout()
                }
                DeleteAccountResult.SessionExpired -> {
                    _events.send(
                        ProfileEvent.ShowNotification(
                            messageRes = Res.string.profile_delete_account_error_session,
                            type = CelvoNotificationType.Warning
                        )
                    )
                    finalizeLogout()
                }
                DeleteAccountResult.AuthProviderUnavailable -> {
                    _events.send(
                        ProfileEvent.ShowNotification(
                            messageRes = Res.string.profile_delete_account_error_partial,
                            type = CelvoNotificationType.Warning
                        )
                    )
                    finalizeLogout()
                }
                DeleteAccountResult.Retryable,
                DeleteAccountResult.Generic -> {
                    _state.update { it.copy(deletionStatus = DeletionStatus.RetryableError) }
                }
            }
        }
    }

    private suspend fun finalizeLogout() {
        // Reset the local VM state before the auth observer pops Profile off
        // the back-stack, so a brief Profile recomposition (if any) doesn't
        // flash the dialog after deletion succeeds.
        _state.update { ProfileState() }
        sessionController.logout()
    }
}
