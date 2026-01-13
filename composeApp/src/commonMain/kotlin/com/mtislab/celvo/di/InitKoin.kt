package com.mtislab.celvo.di


import com.mtislab.auth.di.authPresentationModule
import com.mtislab.celvo.feature.store.di.storeModule
import com.mtislab.core.data.di.coreDataModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            authModule,
            appModule,
            storeModule
        )
    }
}