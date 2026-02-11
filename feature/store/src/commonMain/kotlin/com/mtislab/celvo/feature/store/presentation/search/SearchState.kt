package com.mtislab.celvo.feature.store.presentation.search

import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.core.domain.model.Route

data class SearchState(
    val query: String = "",
    val selectedTab: Route.SearchTab = Route.SearchTab.COUNTRY,
    val searchResults: List<StoreItem> = emptyList(),
    val requestFocus: Boolean = false,
    val allCountries: List<StoreItem> = emptyList(),
    val allRegions: List<StoreItem> = emptyList()
)

