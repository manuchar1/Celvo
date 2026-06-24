package com.mtislab.celvo.feature.store.presentation.store

import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.model.UserEsim
import com.mtislab.core.domain.utils.DataError

sealed interface StoreState {

    data object Loading : StoreState

    /**
     * The whole catalog failed to load. [error] decides which placeholder the
     * screen shows: a connectivity error auto-recovers on reconnect, anything
     * else is a server fault the user must retry manually.
     */
    data class Error(val error: DataError) : StoreState

    data class Content(
        val isLoggedIn: Boolean = false,
        val activeEsimHome: ActiveEsimHome? = null,
        val selectedEsimIndex: Int = 0,
        val isRefreshing: Boolean = false,
        val isDataStale: Boolean = false,
        val showEsimSwitcher: Boolean = false,
        val marketingBanners: List<MarketingBanner> = emptyList(),
        val regions: List<StoreItem> = emptyList(),
        val topPicks: List<StoreItem> = emptyList(),
        val allCountries: List<StoreItem> = emptyList(),
        val isLoadingPackages: Boolean = false,
        val packagesError: String? = null,
        val isInstalling: Boolean = false,
        val installingEsimId: String? = null,
        val installationError: String? = null,
    ) : StoreState {

        /** The currently displayed eSIM based on [selectedEsimIndex]. */
        val selectedEsim: UserEsim?
            get() = activeEsimHome?.esims?.getOrNull(selectedEsimIndex)
    }
}