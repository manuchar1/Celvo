package com.mtislab.celvo.feature.myesim.di

import com.mtislab.celvo.feature.myesim.data.remote.MyEsimRemoteService
import com.mtislab.celvo.feature.myesim.data.repository.MyEsimRepositoryImpl
import com.mtislab.celvo.feature.myesim.domain.repository.MyEsimRepository
import com.mtislab.celvo.feature.myesim.presentation.details.EsimDetailsViewModel
import com.mtislab.celvo.feature.myesim.presentation.list.MyEsimListViewModel
import com.mtislab.core.domain.utils.CacheClearable
import org.koin.core.module.dsl.binds
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val myEsimModule = module {
    singleOf(::MyEsimRemoteService)
    single<MyEsimRepository> {
        MyEsimRepositoryImpl(
            remoteService = get(),
            sessionController = get()
        )
    }
    viewModelOf(::MyEsimListViewModel)
    viewModelOf(::EsimDetailsViewModel)
}