package com.mtislab.core.domain.esim

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic eSIM installer interface.
 *
 * Implementations:
 * - Android: [AndroidEsimInstaller] using EuiccManager API
 * - iOS: [IosEsimInstaller] using CTCellularPlanProvisioning
 *
 * The [install] method returns a Flow that emits [EsimInstallStatus] states.
 * Callers MUST collect the flow until a terminal state (Success/Error) is received.
 * Cancelling the flow prematurely will unregister platform receivers.
 */
interface EsimInstaller {
    fun install(data: EsimInstallationData): Flow<EsimInstallStatus>
    fun isEsimSupported(): Boolean
}