package com.mtislab.core.data.di

import com.mtislab.core.data.BuildKonfig
import com.mtislab.core.data.auth.SupabaseDeepLinkHandler
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
            install(Auth) {
                scheme = "com.mtislab.celvo"
                host = "login-callback"
            }
        }
    }

    // --- Deep-link → Supabase Auth handler (needed for iOS OAuth) ---
    single {
        SupabaseDeepLinkHandler(supabase = get())
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

    // Eagerly start listening for auth deep links so the handler is
    // ready before any onOpenURL callback fires on iOS.
    single<Unit>(createdAtStart = true) {
        get<SupabaseDeepLinkHandler>().startListening()
    }
}