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

    /*    @Serializable
        data object Search : Route*/

    @Serializable
    data object MyEsim : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class Packages(
        val isoCode: String,
        val countryName: String,
        val type: String
    ) : Route


    @Serializable
    data class CheckoutRoute(
        val packageId: String,
        val countryName: String,
        val type: String,
        val region: String,

        ) : Route


    @Serializable
    data object Theme : Route

    @Serializable
    data object Language : Route


    @Serializable
    data class PaymentVerification(
        val orderId: String
    ) : Route


    @Serializable
    data object OfflineInstructions : Route

    @Serializable
    data class EsimDetailsRoute(
        val esimId: String
    ) : Route


    @Serializable
    data class Search(
        val initialTab: SearchTab = SearchTab.COUNTRY,
        val focusSearch: Boolean = false
    ) : Route

    @Serializable
    enum class SearchTab {
        COUNTRY, REGION
    }


}