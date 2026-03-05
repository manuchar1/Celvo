package com.mtislab.core.domain.auth

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SessionController {

    val state: StateFlow<AuthState>
    val sessionEvents: SharedFlow<SessionEvent>
    suspend fun onLoginSuccess(accessToken: String, refreshToken: String, userId: String)
    suspend fun logout()
    fun isAuthenticated(): Boolean
}