package com.mtislab.celvo

import com.mtislab.core.domain.model.AppTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.mtislab.celvo.ui.MainScreenRoot
import com.mtislab.core.data.session.ThemePreferences
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationHost
import com.mtislab.core.designsystem.components.notifications.LocalCelvoNotification
import com.mtislab.core.designsystem.components.notifications.rememberCelvoNotificationState
import com.mtislab.core.designsystem.theme.CelvoTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    // Configure Coil's singleton ImageLoader with an explicit Ktor engine.
    // Coil's auto-registered Ktor fetcher builds a no-arg HttpClient(), which
    // works on Android/JVM (the engine is auto-discovered via ServiceLoader)
    // but FAILS on iOS/Kotlin-Native where engines can't be auto-discovered —
    // so network images (e.g. marketing-banner mascots) silently fail to load.
    // Supplying the platform engine from Koin (Darwin on iOS, OkHttp on
    // Android) fixes network image loading on iOS.
    val httpClientEngine = koinInject<HttpClientEngine>()
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = { HttpClient(httpClientEngine) }))
            }
            .build()
    }

    val themePreferences = koinInject<ThemePreferences>()
    val appTheme by themePreferences.themeFlow.collectAsState(initial = AppTheme.SYSTEM)
    val isSystemDark = isSystemInDarkTheme()

    val darkTheme = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemDark
    }

    CelvoTheme(darkTheme = darkTheme) {
        val notificationState = rememberCelvoNotificationState()

        CompositionLocalProvider(LocalCelvoNotification provides notificationState) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainScreenRoot()
                CelvoNotificationHost(
                    state = notificationState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
