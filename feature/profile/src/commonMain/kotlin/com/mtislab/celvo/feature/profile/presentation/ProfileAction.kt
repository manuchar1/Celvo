package com.mtislab.celvo.feature.profile.presentation


sealed interface ProfileAction {
    data object OnLogoutClick : ProfileAction
    data object OnRetry : ProfileAction
}