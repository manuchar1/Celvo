package com.mtislab.celvo.feature.store.presentation.store

import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.core.domain.model.ActiveEsimHome

sealed interface StoreState {

    data object Loading : StoreState

    data class Error(val message: String) : StoreState

    data class Content(
        val isLoggedIn: Boolean = false,

        // If non-null and hasActiveBundle == true → show Usage Gauge section (Figma: Purchased).
        // If null or hasActiveBundle == false → show "მიიღე +1GB საჩუქრად" banner (Figma: Guest).
        val activeEsimHome: ActiveEsimHome? = null,

        val marketingBanners: List<MarketingBanner> = emptyList(),
        val regions: List<StoreItem> = emptyList(),
        val topPicks: List<StoreItem> = emptyList(),
        val allCountries: List<StoreItem> = emptyList()
    ) : StoreState
}