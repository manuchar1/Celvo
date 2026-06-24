package com.mtislab.celvo.feature.profile.presentation


sealed interface ProfileAction {
    data object OnLogoutClick : ProfileAction
    data object OnRetry : ProfileAction
    data object OnDeleteAccountClick : ProfileAction
    data object OnConfirmDelete : ProfileAction
    data object OnCancelDelete : ProfileAction
}
