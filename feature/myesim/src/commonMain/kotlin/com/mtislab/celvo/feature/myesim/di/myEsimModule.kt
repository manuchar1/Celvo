package com.mtislab.celvo.feature.myesim.di

import com.mtislab.celvo.feature.myesim.data.remote.MyEsimRemoteService
import com.mtislab.celvo.feature.myesim.data.repository.MyEsimRepositoryImpl
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.celvo.feature.myesim.presentation.details.EsimDetailsViewModel
import com.mtislab.celvo.feature.myesim.presentation.list.MyEsimListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin dependency injection module for My eSim feature.
 * Following the same pattern as storeModule and profileModule.
 */
val myEsimModule = module {

    // Data Layer
    singleOf(::MyEsimRemoteService)
    singleOf(::MyEsimRepositoryImpl).bind<MyEsimRepository>()

    // Presentation Layer
    viewModelOf(::MyEsimListViewModel)
    viewModelOf(::EsimDetailsViewModel)
}