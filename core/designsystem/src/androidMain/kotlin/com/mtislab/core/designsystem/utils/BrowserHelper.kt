package com.mtislab.core.designsystem.utils

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

object BrowserHelper {
    fun openCustomTab(context: Context, url: String) {
        val primaryColor = "#9087CD".toColorInt()

        val params = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(primaryColor)
            .setNavigationBarColor(primaryColor)
            .build()

        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(params)
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()

        try {
            intent.launchUrl(context, url.toUri())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}