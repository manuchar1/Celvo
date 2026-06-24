package com.mtislab.core.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberBrowserOpener(): (String) -> Unit {
    return remember {
        { url ->
            try {
                if (Desktop.isDesktopSupported()) {
                    val desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(URI(url))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
