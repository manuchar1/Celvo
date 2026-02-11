package com.mtislab.celvo.feature.myesim.presentation.list

import com.mtislab.celvo.feature.myesim.domain.model.UserEsim

/**
 * Actions that can be performed on the My eSIM List Screen.
 */
sealed interface MyEsimListAction {
    // Data loading
    data object LoadEsims : MyEsimListAction
    data object RetryClick : MyEsimListAction

    // Navigation triggers (handled by UI layer)
    data class EsimClick(val esim: UserEsim) : MyEsimListAction
    data class TopUpClick(val esim: UserEsim) : MyEsimListAction
    data class DetailsClick(val esim: UserEsim) : MyEsimListAction
    data object AddEsimClick : MyEsimListAction

    // eSIM Installation
    data class ActivateClick(val esim: UserEsim) : MyEsimListAction

    // Resolution handling (simplified)
    /**
     * Signals that the resolution PendingIntent was launched by the UI.
     * Clears [MyEsimListState.resolutionRequired] to prevent re-launch.
     *
     * NOTE: We do NOT need a ResolutionResult action anymore.
     * Samsung's consent dialog always returns RESULT_CANCELED via Activity Result.
     * The real result arrives via a second broadcast to AndroidEsimInstaller's
     * BroadcastReceiver, which emits Success/Error through the install flow.
     */
    data object ResolutionLaunched : MyEsimListAction

    // Error dismissal
    data object DismissError : MyEsimListAction
}