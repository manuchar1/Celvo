package com.mtislab.celvo.feature.myesim.presentation.details

/**
 * Actions that can be performed on the eSim Details Screen.
 */
sealed interface EsimDetailsAction {

    data object BackClick : EsimDetailsAction
    data object Refresh : EsimDetailsAction
    data object ShowQrCode : EsimDetailsAction
    data object DismissQrCode : EsimDetailsAction
    data object ShowOperators : EsimDetailsAction
    data object DismissOperators : EsimDetailsAction
    data object StartEditLabel : EsimDetailsAction
    data class UpdateLabelText(val text: String) : EsimDetailsAction
    data object SaveLabel : EsimDetailsAction
    data object CancelEditLabel : EsimDetailsAction
    data object TopUpClick : EsimDetailsAction
    data object DeleteClick : EsimDetailsAction
    data object ConfirmDelete : EsimDetailsAction
    data object CancelDelete : EsimDetailsAction
}