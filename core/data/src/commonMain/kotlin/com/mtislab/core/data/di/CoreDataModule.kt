package com.mtislab.core.data.di


import com.mtislab.core.data.BuildKonfig
import com.mtislab.core.data.logging.KermitLogger
import com.mtislab.core.data.networking.HttpClientFactory
import com.mtislab.core.domain.logging.ChirpLogger
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)

    single<ChirpLogger> { KermitLogger }

    single {
        HttpClientFactory(get()).create(get())
    }


    single {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY
        ) {
            install(Auth)
            //  install(Postgrest)
            // httpEngine = get()
        }
    }
}