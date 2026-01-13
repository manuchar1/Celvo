package com.mtislab.celvo.feature.store.presentation

import com.mtislab.celvo.feature.store.domain.model.StoreItem

sealed interface StoreAction {
    data class OnTabSelected(val tab: StoreTab) : StoreAction
    data class OnItemClick(val item: StoreItem) : StoreAction
    data class OnSearchQueryChange(val query: String) : StoreAction
    data object OnRetry : StoreAction

    data class OnBannerClick(val deepLink: String) : StoreAction
}