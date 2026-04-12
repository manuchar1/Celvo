package com.mtislab.core.domain.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkHandler {
    private val _events = MutableSharedFlow<String>(replay = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var lastEmittedUrl: String? = null

    fun handleDeepLink(url: String) {
        // Deduplicate: iOS onOpenURL can fire multiple times for the same URL
        if (url == lastEmittedUrl) {
            println("🚀 DeepLink Duplicate (ignored): $url")
            return
        }
        lastEmittedUrl = url
        println("🚀 DeepLink Received: $url")
        _events.tryEmit(url)
    }

    /**
     * Call after processing a deep link to allow the same URL to be handled
     * again in the future (e.g., a second payment).
     */
    fun clearLastEmitted() {
        lastEmittedUrl = null
        _events.resetReplayCache()
    }
}
