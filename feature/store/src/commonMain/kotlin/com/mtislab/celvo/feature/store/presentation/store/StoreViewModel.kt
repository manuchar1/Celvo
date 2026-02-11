package com.mtislab.celvo.feature.store.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // --- Internal Mutable Flows ---

    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Data Sources
    private val _allCountries = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _topPicks = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _regions = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _marketingBanners = MutableStateFlow<List<MarketingBanner>>(emptyList())

    // Auth state derived from SessionManager
    private val _isLoggedIn = sessionManager.state.map { it is AuthState.Authenticated }

    // TODO: Wire to /api/v1/esims/home GET when implemented
    private val _activeEsimHome = MutableStateFlow<ActiveEsimHome?>(null)

    // --- Combine into Sealed StoreState ---
    //
    // Strategy: Group data flows into a private wrapper to stay within
    // the 5-argument limit of kotlinx.coroutines.flow.combine.

    private data class DataPayload(
        val allCountries: List<StoreItem>,
        val topPicks: List<StoreItem>,
        val regions: List<StoreItem>,
        val banners: List<MarketingBanner>,
        val activeEsimHome: ActiveEsimHome?
    )

    private val dataPayload = combine(
        _allCountries,
        _topPicks,
        _regions,
        _marketingBanners,
        _activeEsimHome
    ) { countries, topPicks, regions, banners, esimHome ->
        DataPayload(countries, topPicks, regions, banners, esimHome)
    }

    val state = combine(
        _isLoading,
        _errorMessage,
        _isLoggedIn,
        dataPayload
    ) { loading, error, loggedIn, data ->

        when {
            loading -> StoreState.Loading

            error != null -> StoreState.Error(message = error)

            else -> StoreState.Content(
                isLoggedIn = loggedIn,
                activeEsimHome = if (loggedIn) data.activeEsimHome else null,
                marketingBanners = data.banners,
                regions = data.regions,
                topPicks = data.topPicks,
                allCountries = data.allCountries
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StoreState.Loading
    )

    // --- Init ---

    init {
        loadData()
        loadBanners()
    }

    // --- Action Handler ---

    fun onAction(action: StoreAction) {
        when (action) {
            is StoreAction.OnItemClick -> { /* handled at Screen level via callback */ }
            is StoreAction.OnRegionClick -> { /* handled at Screen level via callback */ }
            is StoreAction.OnBannerClick -> { /* handled at Screen level via callback */ }
            is StoreAction.OnSearchClick -> { /* handled at Screen level via callback */ }
            is StoreAction.OnTopUpClick -> { /* TODO: Navigate to top-up */ }
            is StoreAction.OnDetailsClick -> { /* TODO: Navigate to eSIM details */ }
            is StoreAction.OnSupportClick -> { /* TODO: Navigate to support */ }
            StoreAction.OnRetry -> {
                loadData()
                loadBanners()
            }
        }
    }

    // --- Data Loading ---

    private fun loadData() {
        if (_allCountries.value.isNotEmpty() || _regions.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val countriesDef = async { repository.getCountries() }
            val regionsDef = async { repository.getRegions() }

            val countriesRes = countriesDef.await()
            val regionsRes = regionsDef.await()

            if (countriesRes is Resource.Success) {
                _topPicks.value = countriesRes.data.topPicks
                _allCountries.value = countriesRes.data.allCountries
            }
            if (regionsRes is Resource.Success) {
                _regions.value = regionsRes.data
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
                is Resource.Success -> _marketingBanners.value = result.data
                is Resource.Failure -> { /* Banners are non-critical, fail silently */ }
            }
        }
    }
}