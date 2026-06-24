package com.mtislab.core.domain.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Observes device connectivity in real time.
 *
 * The platform implementations report whether there is a usable, validated
 * internet connection — not merely an attached network interface — so a Wi-Fi
 * network behind a captive portal is reported as offline.
 */
interface ConnectivityObserver {
    /**
     * Emits the current connectivity status on subscription, then every change.
     * `true` means a validated internet connection is available.
     */
    val isOnline: Flow<Boolean>
}

/**
 * Runs [action] every time connectivity is regained after having been lost.
 *
 * The initial status emitted on subscription is ignored, so [action] fires only
 * on a genuine offline → online transition. Callers still guard [action] with
 * their own "am I currently showing an offline error?" check, so that a
 * reconnect does not reload screens that never failed.
 */
fun ConnectivityObserver.onBackOnline(
    scope: CoroutineScope,
    action: () -> Unit,
): Job = isOnline
    .drop(1)
    .distinctUntilChanged()
    .filter { online -> online }
    .onEach { action() }
    .launchIn(scope)
