package com.mtislab.celvo.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    //Auth Graph
    @Serializable
    data object AuthGraph : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Register : Route

    //Main Tabs
    @Serializable
    data object Home : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object MyEsim : Route

    @Serializable
    data object Profile : Route
}