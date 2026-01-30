package com.mtislab.core.domain.model

import kotlinx.serialization.Serializable

sealed interface Route {
    //Auth Graph
    @Serializable
    data object AuthGraph : Route

    @Serializable
    data class Login(
        val redirectTo: String? = null
    ) : Route

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

    @Serializable
    data class Packages(
        val isoCode: String,
        val countryName: String
    ) : Route


    @Serializable
    data class CheckoutRoute(
        val packageId: String,
        val countryName: String
    ) : Route



    @Serializable
    data object Theme : Route

    @Serializable
    data object Language : Route


    @Serializable
    data class PaymentResult(
        val isSuccess: Boolean,
        val orderId: String? = null
    )


}