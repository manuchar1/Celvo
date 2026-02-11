package com.mtislab.core.domain.esim

/**
 * eSIM installation status — commonMain (platform-agnostic).
 *
 * Flow contract:
 * ```
 * Idle → Installing → ResolutionRequired → Installing → Success
 *                   → Error
 *                   → ResolutionRequired → Error
 * ```
 *
 * IMPORTANT: After [ResolutionRequired] is emitted, the flow remains OPEN.
 * The platform installer continues listening for the final result.
 * Do NOT cancel the flow after receiving [ResolutionRequired].
 */
sealed interface EsimInstallStatus {
    data object Idle : EsimInstallStatus
    data object Installing : EsimInstallStatus
    data object Success : EsimInstallStatus
    data class Error(val error: InstallError) : EsimInstallStatus

    /**
     * System requires user confirmation (e.g., Samsung consent dialog).
     *
     * @param resolutionData Platform-specific data:
     *   - Android: [android.app.PendingIntent] to launch via Activity Result API
     *   - iOS: Not used (iOS handles consent natively)
     *
     * After the UI launches the resolution intent, the installer's internal
     * BroadcastReceiver will capture the final result and emit either
     * [Success] or [Error]. The UI does NOT need to interpret the Activity
     * Result code — Samsung always returns RESULT_CANCELED regardless of
     * user choice.
     */
    data class ResolutionRequired(val resolutionData: Any) : EsimInstallStatus
}

/**
 * Unified error type for eSIM installation failures.
 *
 * Maps platform-specific errors:
 * - Android: EuiccManager.ERROR_* codes
 * - iOS: CTCellularPlanProvisioningAddPlanResult
 */
sealed interface InstallError {
    data object DeviceNotSupported : InstallError
    data object NotAllowed : InstallError
    data object SystemError : InstallError
    data object Cancelled : InstallError
    data object InvalidActivationCode : InstallError
    data object InsufficientMemory : InstallError
    data object CarrierLocked : InstallError
    data object Timeout : InstallError
}