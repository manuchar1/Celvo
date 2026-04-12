package com.mtislab.celvo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mtislab.celvo.ui.MainScreenRoot
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationHost
import com.mtislab.core.designsystem.components.notifications.LocalCelvoNotification
import com.mtislab.core.designsystem.components.notifications.rememberCelvoNotificationState
import com.mtislab.core.designsystem.theme.CelvoTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    CelvoTheme {
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