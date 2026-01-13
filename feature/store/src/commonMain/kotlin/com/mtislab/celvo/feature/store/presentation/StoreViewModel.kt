package com.mtislab.celvo.feature.store.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: StoreRepository
) : ViewModel() {

    // Internal mutable states
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _selectedTab = MutableStateFlow(StoreTab.COUNTRIES)
    private val _searchQuery = MutableStateFlow("")

    // Data Sources
    private val _rawCountries = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _rawRegions = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _rawTopPicks = MutableStateFlow<List<StoreItem>>(emptyList())

    // ✅ NEW: Banners Flow
    private val _marketingBanners = MutableStateFlow<List<MarketingBanner>>(emptyList())

    // UI State Construction
// UI State Construction
    val state = combine(
        _isLoading,
        _errorMessage,
        _selectedTab,
        _searchQuery,
        _rawCountries,
        _rawRegions,
        _rawTopPicks,
        _marketingBanners
    ) { loading, error, tab, query, countries, regions, topPicks, banners ->

        val isSearching = query.isNotBlank()

        // 1. განვსაზღვროთ მთავარი სია (საიდანაც ვეძებთ ან ვაჩვენებთ)
        val sourceList = if (tab == StoreTab.COUNTRIES) {
            if (isSearching) {
                // 🔍 ძებნის დროს: ვაერთიანებთ TopPicks-ს და Countries-ს და ვშლით დუბლიკატებს.
                // ეს აგვარებს პრობლემას, რომ "Search only searches countries".
                (topPicks + countries).distinctBy { it.id }
            } else {
                // ჩვეულებრივ დროს: მხოლოდ All Countries სია
                countries
            }
        } else {
            regions
        }

        // 2. ფილტრაცია (Search Logic)
        val filteredList = if (isSearching) {
            sourceList.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            sourceList
        }

        // 3. Top Picks ლოგიკა
        // Top Picks უნდა გამოჩნდეს მხოლოდ მაშინ, როცა:
        // ა) ვართ ქვეყნების ტაბზე
        // ბ) და არ ვეძებთ (Search ცარიელია)
        val currentTopPicks = if (tab == StoreTab.COUNTRIES && !isSearching) {
            topPicks
        } else {
            emptyList()
        }

        StoreState(
            isLoading = loading,
            errorMessage = error,
            selectedTab = tab,
            searchQuery = query,
            rawCountries = countries,
            rawRegions = regions,
            rawTopPicks = topPicks,
            marketingBanners = banners,

            // UI-ს ვაწვდით გაფილტრულ სიას
            displayedItems = filteredList,

            // UI-ს ვაწვდით Top Picks-ს (ან ცარიელს, თუ ვეძებთ)
            displayedTopPicks = currentTopPicks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreState())

    init {
        loadData()
        loadBanners()
    }

    fun onAction(action: StoreAction) {
        when (action) {
            is StoreAction.OnTabSelected -> _selectedTab.value = action.tab
            is StoreAction.OnSearchQueryChange -> _searchQuery.value = action.query
            is StoreAction.OnItemClick -> println("Clicked Item: ${action.item.name}")
            is StoreAction.OnBannerClick -> println("Clicked Banner DeepLink: ${action.deepLink}") // ✅ Handle Click
            StoreAction.OnRetry -> {
                loadData()
                loadBanners()
            }
            else -> {}
        }
    }

    private fun loadData() {
        if (_rawCountries.value.isNotEmpty() || _rawRegions.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val countriesDef = async { repository.getCountries() }
            val regionsDef = async { repository.getRegions() }

            val countriesRes = countriesDef.await()
            val regionsRes = regionsDef.await()

            if (countriesRes is Resource.Success) {
                _rawTopPicks.value = countriesRes.data.topPicks
                _rawCountries.value = countriesRes.data.allCountries
            }
            if (regionsRes is Resource.Success) {
                _rawRegions.value = regionsRes.data
            }

            if (countriesRes is Resource.Failure && regionsRes is Resource.Failure) {
                _errorMessage.value = "მონაცემების ჩატვირთვა ვერ მოხერხდა"
            }

            _isLoading.value = false
        }
    }

    private fun loadBanners() {
        viewModelScope.launch {
            // ✅ Updates local flow, which triggers 'combine' to update the UI State
            when (val result = repository.getBanners()) {
                is Resource.Success -> {
                    _marketingBanners.value = result.data
                }
                is Resource.Failure -> {
                    // Silently fail for banners (optional: log error)
                    println("Banner Error: ${result.error}")
                }
            }
        }
    }

    // Helper function extended to support 8 arguments
    @Suppress("UNCHECKED_CAST")
    fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
        flow1: kotlinx.coroutines.flow.Flow<T1>,
        flow2: kotlinx.coroutines.flow.Flow<T2>,
        flow3: kotlinx.coroutines.flow.Flow<T3>,
        flow4: kotlinx.coroutines.flow.Flow<T4>,
        flow5: kotlinx.coroutines.flow.Flow<T5>,
        flow6: kotlinx.coroutines.flow.Flow<T6>,
        flow7: kotlinx.coroutines.flow.Flow<T7>,
        flow8: kotlinx.coroutines.flow.Flow<T8>,
        transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R
    ): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(
        listOf(flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8)
    ) { args ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
            args[7] as T8
        )
    }
}