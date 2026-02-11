package com.mtislab.celvo.di


import com.mtislab.celvo.feature.auth.di.authPresentationModule
import com.mtislab.celvo.feature.myesim.di.myEsimModule
import com.mtislab.celvo.feature.profile.di.profileModule
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
            storeModule,
            profileModule,
            myEsimModule
        )
    }
}