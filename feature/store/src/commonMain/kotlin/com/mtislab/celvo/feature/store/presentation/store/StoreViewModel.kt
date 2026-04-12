package com.mtislab.celvo.feature.store.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.PromoClaimRepository
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.logging.CelvoLogger
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val promoClaimRepository: PromoClaimRepository,
    private val logger: CelvoLogger,
) : ViewModel() {

    // ── One-shot events ──────────────────────────────────────────────────

    private val _events = Channel<StoreEvent>()
    val events = _events.receiveAsFlow()

    private val _uiEvent = Channel<StoreUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    // ── Raw banners from API (before claim-state merge) ──────────────────
    private var rawBanners: List<MarketingBanner> = emptyList()

    // ── Page-level loading / error ───────────────────────────────────────
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

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
        combine(_isLoading, _errorMessage) { loading, error -> loading to error },
        _isLoggedIn,
        _dataPayload,
        _uiFlags,
    ) { (loading, error), loggedIn, data, flags ->
        when {
            loading -> StoreState.Loading
            error != null -> StoreState.Error(message = error)
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
        observeClaimedState()
    }

    // ── Action Handler ───────────────────────────────────────────────────

    fun onAction(action: StoreAction) {
        when (action) {
            // Delegated to StoreScreenRoot navigation callbacks — no VM logic needed.
            is StoreAction.OnItemClick,
            is StoreAction.OnRegionClick,
            is StoreAction.OnBannerClick,
            is StoreAction.OnSearchClick -> Unit

            // eSIM switcher
            StoreAction.OnEsimSwitcherClick -> _showEsimSwitcher.value = true
            StoreAction.OnEsimSwitcherDismiss -> _showEsimSwitcher.value = false
            is StoreAction.OnEsimSelected -> {
                _selectedEsimIndex.value = action.index
                _showEsimSwitcher.value = false
                _packagesError.value = null
                loadPackagesIfNeeded(action.index)
            }

            // ── Promo claim (from interactive banner CTA) ────────────────
            is StoreAction.ClaimBannerPromo -> claimBannerPromo(action.banner)

            // DEPRECATED: Remove OnClaimPromoCode once PromoStateHolder is deleted.
            // Keeping it as a no-op to avoid breaking compilation during transition.
            is StoreAction.OnClaimPromoCode -> Unit

            // Active eSIM quick-actions
            StoreAction.OnRetryLoadPackages -> loadPackagesIfNeeded(_selectedEsimIndex.value)
            StoreAction.OnInstallClick -> handleInstallClick()
            StoreAction.OnTopUpClick -> { /* TODO: navigate to top-up */ }
            StoreAction.OnDetailsClick -> { /* TODO: navigate to details */ }
            StoreAction.OnSupportClick -> { /* TODO: navigate to support */ }

            // Page-level
            StoreAction.OnRetry -> {
                loadStoreData()
                reloadBanners(force = true)
                loadEsimHome()
            }

            StoreAction.OnRefresh -> refreshEsimHome()
        }
    }

    fun onScreenResumed() {
        refreshEsimHome()
    }

    // ── Promo claim (NEW — replaces PromoStateHolder) ────────────────────

    /**
     * Reactively observes locally claimed banner IDs from DataStore.
     * Whenever a claim changes, re-merges `isClaimed = true` onto
     * [rawBanners] and pushes the result into [_marketingBanners].
     *
     * This is the key piece that makes the banner UI update instantly
     * after claiming — no API re-fetch needed.
     */
    private fun observeClaimedState() {
        promoClaimRepository.observeClaimedBannerIds()
            .onEach { claimedIds ->
                if (rawBanners.isNotEmpty()) {
                    _marketingBanners.value = rawBanners.map { banner ->
                        banner.copy(isClaimed = banner.id in claimedIds)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Persists the claim in DataStore and emits a success notification event.
     * The banner UI updates reactively via [observeClaimedState] — no manual
     * state mutation needed here.
     */
    private fun claimBannerPromo(banner: MarketingBanner) {
        val code = banner.promoCode ?: return
        if (banner.isClaimed) return

        logger.info("[Store] claimBannerPromo called — code=$code, bannerId=${banner.id}")

        viewModelScope.launch {
            promoClaimRepository.claimPromo(
                bannerId = banner.id,
                code = code,
            )
            logger.info("[Store] claimPromo persisted — sending event")

            _events.send(
                StoreEvent.PromoClaimSuccess(
                    code = code,
                    message = banner.claimedTitle ?: "პრომოკოდი გააქტიურდა: $code",
                )
            )
        }
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

    private fun loadStoreData() {
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

    // ── Marketing banners ────────────────────────────────────────────────

    /**
     * Fetches banners for the given [placement] from the backend.
     * Stores raw results in [rawBanners], then lets [observeClaimedState]
     * merge the `isClaimed` flags before pushing to [_marketingBanners].
     */
    private fun loadBanners(placement: String) {
        viewModelScope.launch {
            when (val result = repository.getBanners(placement)) {
                is Resource.Success -> {
                    rawBanners = result.data
                    _lastLoadedPlacement = placement

                    // Trigger an immediate merge with current claimed state.
                    // After this, the observeClaimedState flow keeps it in sync.
                    mergeBannersWithClaimedState()
                }

                is Resource.Failure -> {
                    logger.warn("[Store] Failed to load banners for placement=$placement")
                }
            }
        }
    }

    /**
     * One-shot merge for when raw banners arrive from API.
     * Reads current claims and applies isClaimed flags.
     */
    private suspend fun mergeBannersWithClaimedState() {
        val activeCode = promoClaimRepository.getActivePromoCode()
        // Quick path: if nothing is claimed, just push raw banners as-is
        if (activeCode == null) {
            _marketingBanners.value = rawBanners
            return
        }

        // Full merge: take a single snapshot of claimed banner IDs
        val claimedIds = promoClaimRepository.observeClaimedBannerIds().first()

        _marketingBanners.value = rawBanners.map { banner ->
            banner.copy(isClaimed = banner.id in claimedIds)
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
                _installationError.value = "ინსტალაციის ბმული ვერ შეიქმნა"
            } finally {
                _isInstalling.value = false
                _installingEsimId.value = null
            }
        }
    }
}

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface StoreEvent {
    data class PromoClaimSuccess(
        val code: String,
        val message: String,
    ) : StoreEvent
}