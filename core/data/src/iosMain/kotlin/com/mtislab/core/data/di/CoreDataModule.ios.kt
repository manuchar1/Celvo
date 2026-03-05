package com.mtislab.core.data.di

import com.mtislab.core.data.esim.IosEsimInstaller
import com.mtislab.core.data.session.DATA_STORE_FILE_NAME
import com.mtislab.core.data.session.createDataStore
import com.mtislab.core.domain.esim.EsimInstaller
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.bind

@OptIn(ExperimentalForeignApi::class)
actual val platformCoreDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single {
        createDataStore {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/$DATA_STORE_FILE_NAME"
        }
    }

    single<EsimInstaller> { IosEsimInstaller(logger = get()) }
}