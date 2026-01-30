package com.mtislab.celvo.feature.store.presentation

import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem

enum class StoreTab {
    COUNTRIES, REGIONS
}

data class StoreState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: StoreTab = StoreTab.COUNTRIES,
    val marketingBanners: List<MarketingBanner> = emptyList(),

    val searchQuery: String = "",
    val rawCountries: List<StoreItem> = emptyList(),
    val rawRegions: List<StoreItem> = emptyList(),
    val rawTopPicks: List<StoreItem> = emptyList(),
    val displayedItems: List<StoreItem> = emptyList(),
    val displayedTopPicks: List<StoreItem> = emptyList()
)