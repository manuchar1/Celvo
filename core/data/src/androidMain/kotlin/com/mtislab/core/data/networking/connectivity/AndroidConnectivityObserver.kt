package com.mtislab.core.data.networking.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.mtislab.core.domain.connectivity.ConnectivityObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * [ConnectivityObserver] backed by [ConnectivityManager].
 *
 * Tracks the app's default network and reports it as online only once the
 * system has VALIDATED that it actually reaches the internet — so a captive
 * portal or a connected-but-dead Wi-Fi network counts as offline.
 */
class AndroidConnectivityObserver(
    context: Context,
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities,
            ) {
                trySend(capabilities.hasValidatedInternet())
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        // Emit the current status up front so a subscriber is never left
        // waiting for the next change to learn whether it is online.
        trySend(currentlyOnline())

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
        .conflate()
        .distinctUntilChanged()

    private fun currentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasValidatedInternet()
    }

    private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
