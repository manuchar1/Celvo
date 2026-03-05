package com.mtislab.celvo.feature.store.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.domain.auth.AuthState
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.logging.CelvoLogger
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.utils.Resource
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

// ── UI Event ──────────────────────────────────────────────────────────────────

sealed interface StoreUiEvent {
    data class OpenUrl(val url: String) : StoreUiEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class StoreViewModel(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager,
    private val linkGenerator: EsimLinkGenerator,
    private val logger: CelvoLogger
) : ViewModel() {

    // ── One-shot events ──────────────────────────────────────────────────
    private val _uiEvent = Channel<StoreUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

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

    // ── Installation ─────────────────────────────────────────────────────
    private val _isInstalling = MutableStateFlow(false)
    private val _installingEsimId = MutableStateFlow<String?>(null)
    private val _installationError = MutableStateFlow<String?>(null)

    // ── Auth ─────────────────────────────────────────────────────────────
    private val _isLoggedIn = sessionManager.state.map { it is AuthState.Authenticated }

    // ── Intermediate groupings (keeps top-level combine at ≤5 flows) ─────

    private data class DataPayload(
        val allCountries: List<StoreItem>,
        val topPicks: List<StoreItem>,
        val regions: List<StoreItem>,
        val banners: List<MarketingBanner>,
        val activeEsimHome: ActiveEsimHome?
    )

    private val _dataPayload = combine(
        _allCountries, _topPicks, _regions, _marketingBanners, _activeEsimHome
    ) { countries, topPicks, regions, banners, esimHome ->
        DataPayload(countries, topPicks, regions, banners, esimHome)
    }

    /**
     * Groups all transient UI flags so the outer [combine] stays within its
     * 5-argument overload limit, preserving type-safety and avoiding vararg
     * Array<*> casts.
     */
    private data class UiFlags(
        val selectedIndex: Int,
        val isRefreshing: Boolean,
        val isDataStale: Boolean,
        val showSwitcher: Boolean,
        val isInstalling: Boolean,
        val installingEsimId: String?,
        val installationError: String?
    )

    // Inner combine (eSIM navigation flags)
    private val _esimFlags = combine(
        _selectedEsimIndex, _isRefreshing, _isDataStale, _showEsimSwitcher
    ) { idx, refreshing, stale, switcher ->
        idx to Triple(refreshing, stale, switcher)
    }

    // Inner combine (install flags)
    private val _installFlags = combine(
        _isInstalling, _installingEsimId, _installationError
    ) { installing, esimId, error ->
        Triple(installing, esimId, error)
    }

    // Merged flags group
    private val _uiFlags = combine(_esimFlags, _installFlags) { esim, install ->
        val (idx, esimTriple) = esim
        val (refreshing, stale, switcher) = esimTriple
        val (installing, esimId, error) = install
        UiFlags(idx, refreshing, stale, switcher, installing, esimId, error)
    }

    // ── Public state ─────────────────────────────────────────────────────

    /**
     * Single source of truth consumed by the UI via [collectAsStateWithLifecycle].
     * All fields in [StoreState.Content] are now populated correctly.
     */
    val state = combine(
        combine(_isLoading, _errorMessage) { loading, error -> loading to error },
        _isLoggedIn,
        _dataPayload,
        _uiFlags,
        PromoStateHolder.claimedPromoCode
    ) { (loading, error), loggedIn, data, flags, claimedCode ->
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
                isInstalling = flags.isInstalling,
                installingEsimId = flags.installingEsimId,
                installationError = flags.installationError,
                claimedPromoCode = claimedCode
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = StoreState.Loading
    )

    // ── Init ─────────────────────────────────────────────────────────────

    init {
        loadStoreData()
        loadBanners()
        observeAuthState()
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
            }

            is StoreAction.OnClaimPromoCode -> {
                PromoStateHolder.claimCode(action.code)
            }

            // Active eSIM quick-actions
            StoreAction.OnInstallClick -> handleInstallClick()
            StoreAction.OnTopUpClick -> { /* TODO: navigate to top-up */ }
            StoreAction.OnDetailsClick -> { /* TODO: navigate to details */ }
            StoreAction.OnSupportClick -> { /* TODO: navigate to support */ }

            // Page-level
            StoreAction.OnRetry -> {
                loadStoreData()
                loadBanners()
                loadEsimHome()
            }
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

    private fun loadBanners() {
        viewModelScope.launch {
            when (val result = repository.getBanners()) {
                is Resource.Success -> _marketingBanners.value = result.data
                is Resource.Failure -> Unit
            }
        }
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
                }
                is Resource.Failure -> {
                    _activeEsimHome.value = null
                    _isDataStale.value = false
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
                }
                is Resource.Failure -> {
                    // Keep showing cached data but mark it as stale.
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

    // ── eSIM installation ────────────────────────────────────────────────

    /**
     * Entry-point called from [onAction] for [StoreAction.OnInstallClick].
     * Reads the currently selected eSIM from internal state — no parameters
     * needed from the UI side, keeping the Action sealed interface clean.
     */
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
            esimId = esim.iccid
        )
    }

    /**
     * Generates the platform install link and fires a [StoreUiEvent.OpenUrl]
     * one-shot event.  State is properly threaded through [_isInstalling] /
     * [_installingEsimId] / [_installationError] flows so the UI can reflect
     * the operation progress.
     */
    private fun handleActivateClick(
        smdpAddress: String,
        activationCode: String,
        esimId: String
    ) {
        viewModelScope.launch {
            _isInstalling.value = true
            _installingEsimId.value = esimId
            _installationError.value = null

            try {
                val url = linkGenerator.generateInstallLink(
                    smdpAddress = smdpAddress,
                    activationCode = activationCode
                )
                logger.info("[Store] Opening install URL for eSIM $esimId: $url")
                _uiEvent.send(StoreUiEvent.OpenUrl(url))
            } catch (e: Exception) {
                logger.error("[Store] Failed to generate install link for eSIM $esimId", e)
                _installationError.value = "ინსტალაციის ბმული ვერ შეიქმნა"
            } finally {
                // Always clear the loading state regardless of outcome.
                _isInstalling.value = false
                _installingEsimId.value = null
            }
        }
    }
}