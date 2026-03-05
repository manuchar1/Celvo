package com.mtislab.core.domain.auth


sealed interface SessionEvent {


    data object LoggedOut : SessionEvent

    data class UserChanged(val userId: String) : SessionEvent
}