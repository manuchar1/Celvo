package com.mtislab.core.data.di

import com.mtislab.core.data.BuildKonfig
import com.mtislab.core.data.logging.KermitLogger
import com.mtislab.core.data.networking.HttpClientFactory
import com.mtislab.core.data.session.DataStoreTokenStorage
import com.mtislab.core.data.session.SessionManager
import com.mtislab.core.data.session.TokenStorage
import com.mtislab.core.domain.auth.SessionController
import com.mtislab.core.domain.esim.EsimLinkGenerator
import com.mtislab.core.domain.esim.InstallEsimUseCase
import com.mtislab.core.domain.logging.CelvoLogger
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {

    includes(platformCoreDataModule)

    single {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY
        ) {
            install(Auth)
        }
    }

    single<CelvoLogger> { KermitLogger }

    single {
        HttpClientFactory(get(), get()).create(get())
    }

    singleOf(::DataStoreTokenStorage) bind TokenStorage::class

    single {
        SessionManager(
            tokenStorage = get(),
            supabase = get(),
            logger = get()
        )
    } bind SessionController::class


    factoryOf(::InstallEsimUseCase)

    singleOf(::EsimLinkGenerator)
}