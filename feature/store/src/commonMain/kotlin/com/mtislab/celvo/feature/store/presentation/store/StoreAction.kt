package com.mtislab.celvo.feature.store.presentation.store

import com.mtislab.celvo.feature.store.domain.model.StoreItem

sealed interface StoreAction {
    data class OnItemClick(val item: StoreItem) : StoreAction
    data class OnRegionClick(val item: StoreItem) : StoreAction
    data class OnBannerClick(val deepLink: String) : StoreAction
    data object OnSearchClick : StoreAction
    data object OnRetry : StoreAction

    // Active eSIM actions (logged-in users)
    data object OnTopUpClick : StoreAction
    data object OnDetailsClick : StoreAction
    data object OnSupportClick : StoreAction
}