package com.mtislab.celvo.feature.auth.presentation.register

import com.mtislab.core.domain.utils.DataError

data class RegisterState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: DataError? = null,
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)