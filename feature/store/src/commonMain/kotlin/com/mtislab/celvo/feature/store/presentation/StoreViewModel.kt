package com.mtislab.celvo.feature.store.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Internal mutable states
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _selectedTab = MutableStateFlow(StoreTab.COUNTRIES)
    private val _searchQuery = MutableStateFlow("")
    // ✅ ახალი Flow სპეციალურად ავტორიზაციის სტატუსისთვის
    private val _isLoggedIn = MutableStateFlow(false)

    // Data Sources
    private val _rawCountries = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _rawRegions = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _rawTopPicks = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _marketingBanners = MutableStateFlow<List<MarketingBanner>>(emptyList())

    // UI State Construction
    val state = combine(
        _isLoading,
        _errorMessage,
        _selectedTab,
        _searchQuery,
        _isLoggedIn, // ✅ დავამატეთ მე-5 არგუმენტად
        _rawCountries,
        _rawRegions,
        _rawTopPicks,
        _marketingBanners
    ) { loading, error, tab, query, loggedIn, countries, regions, topPicks, banners ->

        val isSearching = query.isNotBlank()

        // 1. Source List Logic
        val sourceList = if (tab == StoreTab.COUNTRIES) {
            if (isSearching) {
                (topPicks + countries).distinctBy { it.id }
            } else {
                countries
            }
        } else {
            regions
        }

        // 2. Filter Logic
        val filteredList = if (isSearching) {
            sourceList.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            sourceList
        }

        // 3. Top Picks Logic
        val currentTopPicks = if (tab == StoreTab.COUNTRIES && !isSearching) {
            topPicks
        } else {
            emptyList()
        }

        StoreState(
            isLoading = loading,
            isLoggedIn = loggedIn, // ✅ აქ გადავცემთ რეალურ სტატუსს!
            errorMessage = error,
            selectedTab = tab,
            searchQuery = query,
            rawCountries = countries,
            rawRegions = regions,
            rawTopPicks = topPicks,
            marketingBanners = banners,
            displayedItems = filteredList,
            displayedTopPicks = currentTopPicks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreState())

    init {
        loadData()
        loadBanners()

        // სესიის მოსმენა და _isLoggedIn-ის განახლება
        viewModelScope.launch {
            sessionManager.state.collect { authState ->
                _isLoggedIn.value = authState is AuthState.Authenticated
            }
        }
    }

    fun onAction(action: StoreAction) {
        when (action) {
            is StoreAction.OnTabSelected -> _selectedTab.value = action.tab
            is StoreAction.OnSearchQueryChange -> _searchQuery.value = action.query
            is StoreAction.OnItemClick -> println("Clicked Item: ${action.item.name}")
            is StoreAction.OnBannerClick -> println("Clicked Banner DeepLink: ${action.deepLink}")
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
            when (val result = repository.getBanners()) {
                is Resource.Success -> {
                    _marketingBanners.value = result.data
                }
                is Resource.Failure -> {
                    println("Banner Error: ${result.error}")
                }
            }
        }
    }

    // ✅ განახლებული Helper Function (9 არგუმენტით)
    @Suppress("UNCHECKED_CAST")
    fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
        flow1: kotlinx.coroutines.flow.Flow<T1>,
        flow2: kotlinx.coroutines.flow.Flow<T2>,
        flow3: kotlinx.coroutines.flow.Flow<T3>,
        flow4: kotlinx.coroutines.flow.Flow<T4>,
        flow5: kotlinx.coroutines.flow.Flow<T5>,
        flow6: kotlinx.coroutines.flow.Flow<T6>,
        flow7: kotlinx.coroutines.flow.Flow<T7>,
        flow8: kotlinx.coroutines.flow.Flow<T8>,
        flow9: kotlinx.coroutines.flow.Flow<T9>,
        transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
    ): kotlinx.coroutines.flow.Flow<R> = kotlinx.coroutines.flow.combine(
        listOf(flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9)
    ) { args ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
            args[7] as T8,
            args[8] as T9
        )
    }
}