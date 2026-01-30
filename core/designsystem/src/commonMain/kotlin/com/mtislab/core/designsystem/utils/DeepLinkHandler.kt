package com.mtislab.core.designsystem.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkHandler {
    private val _events = MutableSharedFlow<String>(replay = 1) // replay=1 რადგან შეიძლება UI ჯერ არ იყოს მზად
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun handleDeepLink(url: String) {
        println("🚀 DeepLink Received: $url")
        _events.tryEmit(url)
    }
}