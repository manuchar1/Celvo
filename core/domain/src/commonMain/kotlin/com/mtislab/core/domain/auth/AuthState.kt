package com.mtislab.core.domain.auth

sealed interface   AuthState {
    data object Initial : AuthState
    data object Guest : AuthState
    data class Authenticated(val userId: String) : AuthState
}