package com.mtislab.celvo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.mtislab.celvo.ui.LocalCelvoNavBarFloating

fun MainViewController() = ComposeUIViewController {
    // iOS opts into the floating "Liquid Glass" navigation layer.
    CompositionLocalProvider(LocalCelvoNavBarFloating provides true) {
        App()
    }
}
