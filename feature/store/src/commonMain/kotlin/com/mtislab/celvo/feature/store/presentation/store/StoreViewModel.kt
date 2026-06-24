package com.mtislab.celvo.feature.store.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.store_install_link_failed
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.connectivity.ConnectivityObserver
import com.mtislab.core.domain.connectivity.onBackOnline
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.logging.CelvoLogger
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.domain.utils.isConnectivityError
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

// ── UI Event ──────────────────────────────────────────────────────────────────

sealed interface StoreUiEvent {
    data class OpenUrl(val url: String) : StoreUiEvent
}

// ── Banner Placement Constants ────────────────────────────────────────────────

private object Placements {
    const val STORE = "STORE"
    const val POST_PURCHASE = "POST_PURCHASE"
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class StoreViewModel(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager,
    private val linkGenerator: EsimLinkGenerator,
    private val logger: CelvoLogger,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    // ── One-shot events ──────────────────────────────────────────────────

    private val _uiEvent = Channel<StoreUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    // ── Page-level loading / error ───────────────────────────────────────
    private val _isLoading = MutableStateFlow(true)
    private val _pageError = MutableStateFlow<DataError?>(null)

    // ── Store catalog ────────────────────────────────────────────────────
    private val _allCountries = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _topPicks = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _regions = MutableStateFlow<List<StoreItem>>(emptyList())
    private val _marketingBanners = MutableStateFlow<List<MarketingBanner>>(emptyList())

    // ── eSIM home ────────────────────────────────────────────────────────
    private val _activeEsimHome = MutableStateFlow<ActiveEsimHome?>(null)
    private val _selectedEsimIndex = MutableStateFlow(0)
    private val _isRefreshing = MutableStateFlow(false)
    private val _isDataStale = MutableStateFlow(false)
    private val _showEsimSwitcher = MutableStateFlow(false)

    // ── Package lazy-loading ────────────────────────────────────────────
    private val _isLoadingPackages = MutableStateFlow(false)
    private val _packagesError = MutableStateFlow<String?>(null)

    // ── Installation ─────────────────────────────────────────────────────
    private val _isInstalling = MutableStateFlow(false)
    private val _installingEsimId = MutableStateFlow<String?>(null)
    private val _installationError = MutableStateFlow<String?>(null)

    // ── Auth ─────────────────────────────────────────────────────────────
    private val _isLoggedIn = sessionManager.state.map { it is AuthState.Authenticated }

    // ── Banner placement tracking ────────────────────────────────────────
    private var _lastLoadedPlacement: String? = null

    // ── Intermediate groupings (keeps top-level combine at ≤5 flows) ─────

    private data class DataPayload(
        val allCountries: List<StoreItem>,
        val topPicks: List<StoreItem>,
        val regions: List<StoreItem>,
        val banners: List<MarketingBanner>,
        val activeEsimHome: ActiveEsimHome?,
    )

    private val _dataPayload = combine(
        _allCountries, _topPicks, _regions, _marketingBanners, _activeEsimHome
    ) { countries, topPicks, regions, banners, esimHome ->
        DataPayload(countries, topPicks, regions, banners, esimHome)
    }

    private data class UiFlags(
        val selectedIndex: Int,
        val isRefreshing: Boolean,
        val isDataStale: Boolean,
        val showSwitcher: Boolean,
        val isLoadingPackages: Boolean,
        val packagesError: String?,
        val isInstalling: Boolean,
        val installingEsimId: String?,
        val installationError: String?,
    )

    private val _esimFlags = combine(
        _selectedEsimIndex, _isRefreshing, _isDataStale, _showEsimSwitcher
    ) { idx, refreshing, stale, switcher ->
        idx to Triple(refreshing, stale, switcher)
    }

    private val _installFlags = combine(
        _isInstalling, _installingEsimId, _installationError
    ) { installing, esimId, error ->
        Triple(installing, esimId, error)
    }

    private val _packageFlags = combine(
        _isLoadingPackages, _packagesError
    ) { loading, error ->
        loading to error
    }

    private val _uiFlags = combine(_esimFlags, _installFlags, _packageFlags) { esim, install, pkg ->
        val (idx, esimTriple) = esim
        val (refreshing, stale, switcher) = esimTriple
        val (installing, esimId, error) = install
        val (loadingPkg, pkgError) = pkg
        UiFlags(idx, refreshing, stale, switcher, loadingPkg, pkgError, installing, esimId, error)
    }

    // ── Public state ─────────────────────────────────────────────────────

    val state = combine(
        combine(_isLoading, _pageError) { loading, error -> loading to error },
        _isLoggedIn,
        _dataPayload,
        _uiFlags,
    ) { (loading, error), loggedIn, data, flags ->
        when {
            loading -> StoreState.Loading
            error != null -> StoreState.Error(error = error)
            else -> StoreState.Content(
                isLoggedIn = loggedIn,
                activeEsimHome = if (loggedIn) data.activeEsimHome else null,
                selectedEsimIndex = flags.selectedIndex,
                isRefreshing = flags.isRefreshing,
                isDataStale = flags.isDataStale,
                showEsimSwitcher = flags.showSwitcher,
                marketingBanners = data.banners,
                regions = data.regions,
                topPicks = data.topPicks,
                allCountries = data.allCountries,
                isLoadingPackages = flags.isLoadingPackages,
                packagesError = flags.packagesError,
                isInstalling = flags.isInstalling,
                installingEsimId = flags.installingEsimId,
                installationError = flags.installationError,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StoreState.Loading,
    )

    // ── Init ─────────────────────────────────────────────────────────────

    init {
        loadStoreData()
        observeAuthState()
        observeConnectivity()
    }

    // ── Action Handler ───────────────────────────────────────────────────

    fun onAction(action: StoreAction) {
        when (action) {
            // Delegated to StoreScreenRoot navigation callbacks — no VM logic needed.
            is StoreAction.OnItemClick,
            is StoreAction.OnRegionClick,
            is StoreAction.OnBannerClick,
            is StoreAction.OnSearchClick,
            is StoreAction.OnTopUpClick,
            is StoreAction.OnDetailsClick -> Unit

            // eSIM switcher
            StoreAction.OnEsimSwitcherClick -> _showEsimSwitcher.value = true
            StoreAction.OnEsimSwitcherDismiss -> _showEsimSwitcher.value = false
            is StoreAction.OnEsimSelected -> {
                _selectedEsimIndex.value = action.index
                _showEsimSwitcher.value = false
                _packagesError.value = null
                loadPackagesIfNeeded(action.index)
            }

            // Active eSIM quick-actions
            StoreAction.OnRetryLoadPackages -> loadPackagesIfNeeded(_selectedEsimIndex.value)
            StoreAction.OnInstallClick -> handleInstallClick()
            // Opens the platform-specific install guide; navigation handled in StoreScreenRoot.
            StoreAction.OnSupportClick -> Unit

            // Page-level
            StoreAction.OnRetry -> reload()

            StoreAction.OnRefresh -> refreshEsimHome()
        }
    }

    fun onScreenResumed() {
        refreshEsimHome()
    }

    // ── Auth ─────────────────────────────────────────────────────────────

    private fun observeAuthState() {
        sessionManager.state
            .map { it is AuthState.Authenticated }
            .distinctUntilChanged()
            .onEach { isLoggedIn ->
                if (isLoggedIn) {
                    loadEsimHome()
                } else {
                    _activeEsimHome.value = null
                    _selectedEsimIndex.value = 0
                    _isDataStale.value = false
                    reloadBanners(force = false)
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Store catalog ────────────────────────────────────────────────────

    /**
     * Re-attempts every page-level load. Shared by the manual retry button and
     * by [observeConnectivity] when the device comes back online.
     */
    private fun reload() {
        loadStoreData()
        reloadBanners(force = true)
        loadEsimHome()
    }

    /**
     * Auto-recovers from a no-internet failure: when connectivity returns while
     * the page still shows a connectivity error, the catalog is reloaded so the
     * placeholder gives way to content. A server error is left untouched — it
     * carries its own manual retry.
     */
    private fun observeConnectivity() {
        connectivityObserver.onBackOnline(viewModelScope) {
            if (_pageError.value?.isConnectivityError == true) {
                reload()
            }
        }
    }

    private fun loadStoreData() {
        if (_allCountries.value.isNotEmpty() || _regions.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _pageError.value = null
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
                _pageError.value = countriesRes.error
            }
            _isLoading.value = false
        }
    }

    // ── Marketing banners ────────────────────────────────────────────────

    /**
     * Fetches informational banners for the given [placement] and publishes
     * them to [_marketingBanners].
     */
    private fun loadBanners(placement: String) {
        viewModelScope.launch {
            when (val result = repository.getBanners(placement)) {
                is Resource.Success -> {
                    _marketingBanners.value = result.data
                    _lastLoadedPlacement = placement
                }

                is Resource.Failure -> {
                    logger.warn("[Store] Failed to load banners for placement=$placement")
                }
            }
        }
    }

    private fun reloadBanners(force: Boolean) {
        val placement = resolvePlacement()
        if (!force && placement == _lastLoadedPlacement) return
        loadBanners(placement)
    }

    private fun resolvePlacement(): String {
        val isLoggedIn = sessionManager.state.value is AuthState.Authenticated
        val esimHome = _activeEsimHome.value
        val selectedEsim = esimHome?.esims?.getOrNull(_selectedEsimIndex.value)
        val isGaugeVisible = isLoggedIn
                && selectedEsim != null
                && selectedEsim.packages.isNotEmpty()

        return if (isGaugeVisible) Placements.POST_PURCHASE else Placements.STORE
    }

    // ── eSIM home ────────────────────────────────────────────────────────

    private fun loadEsimHome() {
        if (sessionManager.state.value !is AuthState.Authenticated) return
        viewModelScope.launch {
            when (val result = repository.getEsimHome()) {
                is Resource.Success -> {
                    _activeEsimHome.value = result.data
                    _isDataStale.value = result.data?.esims?.any { !it.dataLive } == true
                    clampSelection(result.data)
                    reloadBanners(force = false)
                    loadPackagesIfNeeded(_selectedEsimIndex.value)
                }

                is Resource.Failure -> {
                    _activeEsimHome.value = null
                    _isDataStale.value = false
                    reloadBanners(force = false)
                }
            }
        }
    }

    private fun refreshEsimHome() {
        if (sessionManager.state.value !is AuthState.Authenticated) return
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = repository.getEsimHome()) {
                is Resource.Success -> {
                    _activeEsimHome.value = result.data
                    _isDataStale.value = result.data?.esims?.any { !it.dataLive } == true
                    clampSelection(result.data)
                    reloadBanners(force = false)
                    loadPackagesIfNeeded(_selectedEsimIndex.value)
                }

                is Resource.Failure -> {
                    if (_activeEsimHome.value != null) _isDataStale.value = true
                }
            }
            _isRefreshing.value = false
        }
    }

    private fun clampSelection(data: ActiveEsimHome?) {
        val maxIndex = (data?.esims?.size ?: 1) - 1
        if (_selectedEsimIndex.value > maxIndex) _selectedEsimIndex.value = 0
    }

    /**
     * Loads packages on demand when user selects an eSIM whose [packagesLoaded] is false.
     * Caches the result into [_activeEsimHome] so switching back is instant.
     */
    private fun loadPackagesIfNeeded(index: Int) {
        val esimHome = _activeEsimHome.value ?: return
        val esim = esimHome.esims.getOrNull(index) ?: return

        // Already loaded — nothing to do
        if (esim.packagesLoaded) return

        viewModelScope.launch {
            _isLoadingPackages.value = true
            _packagesError.value = null

            when (val result = repository.getEsimPackages(esim.iccid)) {
                is Resource.Success -> {
                    // Update the esim entry in-place with loaded packages
                    val updatedEsim = esim.copy(
                        packages = result.data,
                        packagesLoaded = true,
                        hasActivePackage = result.data.any { it.isActive },
                        totalPackageCount = result.data.size,
                    )
                    val updatedEsims = esimHome.esims.toMutableList()
                    updatedEsims[index] = updatedEsim
                    _activeEsimHome.value = esimHome.copy(esims = updatedEsims)
                    reloadBanners(force = false)
                }

                is Resource.Failure -> {
                    _packagesError.value = when (result.error) {
                        DataError.Remote.UNAUTHORIZED -> "AUTH_EXPIRED"
                        else -> "LOAD_FAILED"
                    }
                }
            }
            _isLoadingPackages.value = false
        }
    }

    // ── eSIM installation ────────────────────────────────────────────────

    private fun handleInstallClick() {
        val esimHome = _activeEsimHome.value
        if (esimHome == null) {
            logger.warn("[Store] InstallClick ignored — activeEsimHome is null")
            return
        }

        val esim = esimHome.esims.getOrNull(_selectedEsimIndex.value)
        if (esim == null) {
            logger.warn("[Store] InstallClick ignored — no eSIM at index ${_selectedEsimIndex.value}")
            return
        }

        val smdpAddress = esim.smdpAddress
        val activationCode = esim.activationCode

        if (smdpAddress.isNullOrBlank() || activationCode.isNullOrBlank()) {
            logger.warn(
                "[Store] InstallClick aborted — smdpAddress or activationCode is blank " +
                        "for eSIM ${esim.iccid}"
            )
            return
        }

        handleActivateClick(
            smdpAddress = smdpAddress,
            activationCode = activationCode,
            esimId = esim.iccid,
        )
    }

    private fun handleActivateClick(
        smdpAddress: String,
        activationCode: String,
        esimId: String,
    ) {
        viewModelScope.launch {
            _isInstalling.value = true
            _installingEsimId.value = esimId
            _installationError.value = null

            try {
                val url = linkGenerator.generateInstallLink(
                    smdpAddress = smdpAddress,
                    activationCode = activationCode,
                )
                logger.info("[Store] Opening install URL for eSIM $esimId: $url")
                _uiEvent.send(StoreUiEvent.OpenUrl(url))
            } catch (e: Exception) {
                logger.error("[Store] Failed to generate install link for eSIM $esimId", e)
                _installationError.value = getString(Res.string.store_install_link_failed)
            } finally {
                _isInstalling.value = false
                _installingEsimId.value = null
            }
        }
    }
}