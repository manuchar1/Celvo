package com.mtislab.celvo.feature.store.di

import com.mtislab.celvo.feature.store.data.remote.StoreRemoteService
import com.mtislab.celvo.feature.store.data.repository.StoreRepositoryImpl
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.celvo.feature.store.presentation.store.StoreViewModel
import com.mtislab.celvo.feature.store.presentation.checkout.CheckoutViewModel
import com.mtislab.celvo.feature.store.presentation.packages.PackagesScreenViewModel
import com.mtislab.celvo.feature.store.presentation.search.SearchViewModel
import com.mtislab.core.domain.model.Route
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val storeModule = module {
    singleOf(::StoreRemoteService)
    singleOf(::StoreRepositoryImpl).bind<StoreRepository>()
    viewModelOf(::StoreViewModel)
    viewModelOf(::PackagesScreenViewModel)
    viewModelOf(::CheckoutViewModel)
    viewModel { (initialTab: Route.SearchTab, focusSearch: Boolean) ->
        SearchViewModel(
            repository = get(),
            initialTab = initialTab,
            initialFocus = focusSearch
        )
    }
}