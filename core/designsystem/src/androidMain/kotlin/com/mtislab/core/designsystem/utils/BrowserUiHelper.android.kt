package com.mtislab.core.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberBrowserOpener(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url ->
            BrowserHelper.openCustomTab(context, url)
        }
    }
}