package com.mtislab.celvo.feature.myesim.presentation.list

import com.mtislab.celvo.feature.myesim.domain.model.UserEsim

sealed interface MyEsimListAction {
    data object LoadEsims : MyEsimListAction
    data object RetryClick : MyEsimListAction
    data object Refresh : MyEsimListAction

    data class EsimClick(val esim: UserEsim) : MyEsimListAction
    data class TopUpClick(val esim: UserEsim) : MyEsimListAction
    data class DetailsClick(val esim: UserEsim) : MyEsimListAction
    data object AddEsimClick : MyEsimListAction

    data class ActivateClick(val esim: UserEsim) : MyEsimListAction

    data object DismissError : MyEsimListAction
}