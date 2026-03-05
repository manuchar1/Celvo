package com.mtislab.celvo.feature.store.presentation.store

import com.mtislab.celvo.feature.store.domain.model.MarketingBanner
import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.model.UserEsim

sealed interface StoreState {

    data object Loading : StoreState

    data class Error(val message: String) : StoreState

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
        val isInstalling: Boolean = false,
        val installingEsimId: String? = null,
        val installationError: String? = null,
        val claimedPromoCode: String? = null
    ) : StoreState {

        /** The currently displayed eSIM based on [selectedEsimIndex]. */
        val selectedEsim: UserEsim?
            get() = activeEsimHome?.esims?.getOrNull(selectedEsimIndex)



    }
}