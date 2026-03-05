package com.mtislab.celvo.ui

data class MainScreenState(
    val isLoggedIn: Boolean = false,
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)