package com.mtislab.core.data.di

import com.mtislab.core.data.session.DATA_STORE_FILE_NAME
import com.mtislab.core.data.session.createDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module
import java.io.File
import org.koin.android.ext.koin.androidContext

actual val platformCoreDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single {
        createDataStore {
            val context = androidContext()
            val file = File(context.filesDir, "datastore/$DATA_STORE_FILE_NAME")
            file.absolutePath
        }
    }
}