package com.mtislab.celvo.feature.store.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: StoreRepository,
    // ✅ არგუმენტებს ვიღებთ პირდაპირ კონსტრუქტორში Koin-ისგან
    initialTab: Route.SearchTab,
    initialFocus: Boolean
) : ViewModel() {

    private val _query = MutableStateFlow("")
    // ✅ ინიციალიზაცია ხდება გადმოწოდებული არგუმენტით
    private val _selectedTab = MutableStateFlow(initialTab)
    private val _shouldFocusSearch = MutableStateFlow(initialFocus)

    private val _allCountries = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _allRegions = MutableStateFlow<List<StoreItem>>(emptyList())

    val state = combine(
        _query,
        _selectedTab,
        _allCountries,
        _allRegions,
        _shouldFocusSearch
    ) { query, tab, countries, regions, focus ->

        val sourceList = if (tab == Route.SearchTab.COUNTRY) countries else regions


        val cleanQuery = query.trim()

        val filteredList = if (cleanQuery.isBlank()) {
            sourceList
        } else {
            sourceList.filter { item ->
                if (tab == Route.SearchTab.REGION) {
                    item.name.contains(cleanQuery, ignoreCase = true) ||
                            item.supportedCountries.any { subCountry ->
                                subCountry.name.contains(cleanQuery, ignoreCase = true)
                            }
                } else {
                    item.name.contains(cleanQuery, ignoreCase = true)
                }
            }
        }

        SearchState(
            query = query,
            selectedTab = tab,
            searchResults = filteredList,
            requestFocus = focus,
            allCountries = countries,
            allRegions = regions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchState(
            selectedTab = initialTab,
            requestFocus = initialFocus
        )
    )

    init {
        loadData()
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryChange -> _query.update { action.query }
            is SearchAction.OnTabSelect -> _selectedTab.update { action.tab }
            SearchAction.OnClearQuery -> _query.update { "" }
            is SearchAction.OnItemClick -> { /* Handled by UI Event */ }
            SearchAction.OnBackClick -> { /* Handled by UI Event */ }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val countriesRes = repository.getCountries()
            val regionsRes = repository.getRegions()

            if (countriesRes is Resource.Success) {
                val mergedList = (countriesRes.data.topPicks + countriesRes.data.allCountries)
                    .distinctBy { it.id }

                _allCountries.value = mergedList
            }
            if (regionsRes is Resource.Success) {
                _allRegions.value = regionsRes.data
            }
        }
    }
}