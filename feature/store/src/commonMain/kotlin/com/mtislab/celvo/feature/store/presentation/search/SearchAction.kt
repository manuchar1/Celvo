package com.mtislab.celvo.feature.store.presentation.search

import com.mtislab.celvo.feature.store.domain.model.StoreItem
import com.mtislab.core.domain.model.Route

sealed interface SearchAction {
    data class OnQueryChange(val query: String) : SearchAction
    data class OnTabSelect(val tab: Route.SearchTab) : SearchAction
    data class OnItemClick(val item: StoreItem) : SearchAction
    data object OnBackClick : SearchAction
    data object OnClearQuery : SearchAction
}