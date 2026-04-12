package com.mtislab.celvo.feature.store.di

import com.mtislab.celvo.feature.store.data.remote.StoreRemoteService
import com.mtislab.celvo.feature.store.data.repository.PromoClaimRepositoryImpl
import com.mtislab.celvo.feature.store.data.repository.StoreRepositoryImpl
import com.mtislab.celvo.feature.store.domain.repository.PromoClaimRepository
import com.mtislab.celvo.feature.store.domain.repository.StoreRepository
import com.mtislab.celvo.feature.store.presentation.store.StoreViewModel
import com.mtislab.celvo.feature.store.presentation.checkout.CheckoutViewModel
import com.mtislab.celvo.feature.store.presentation.packages.PackagesScreenViewModel
import com.mtislab.celvo.feature.store.presentation.search.SearchViewModel
import com.mtislab.celvo.feature.store.presentation.verification.PaymentVerificationViewModel
import com.mtislab.core.domain.model.Route
import com.mtislab.core.domain.payment.PaymentVerificationRepository
import com.mtislab.core.domain.payment.VerifyPaymentUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val storeModule = module {
    singleOf(::StoreRemoteService)
    single { StoreRepositoryImpl(get()) } bind StoreRepository::class bind PaymentVerificationRepository::class
    singleOf(::PromoClaimRepositoryImpl).bind<PromoClaimRepository>()

    factoryOf(::VerifyPaymentUseCase)

    viewModelOf(::StoreViewModel)
    viewModelOf(::PackagesScreenViewModel)
    viewModelOf(::CheckoutViewModel)
    viewModelOf(::PaymentVerificationViewModel)
    viewModel { (initialTab: Route.SearchTab, focusSearch: Boolean) ->
        SearchViewModel(
            repository = get(),
            initialTab = initialTab,
            initialFocus = focusSearch,
        )
    }
}