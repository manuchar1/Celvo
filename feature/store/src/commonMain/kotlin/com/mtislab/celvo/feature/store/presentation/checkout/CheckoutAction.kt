package com.mtislab.celvo.feature.store.presentation.checkout

import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.core.domain.auth.GoogleAuthProvider

sealed interface CheckoutAction {
    data class Init(val pkg: EsimPackage) : CheckoutAction
    data class ToggleAutoTopup(val enabled: Boolean) : CheckoutAction
    data class SelectTopupOption(val option: TopupOption) : CheckoutAction
    data class Pay(val option: TopupOption) : CheckoutAction
    data object PayClicked : CheckoutAction
    data object DismissLoginSheet : CheckoutAction
    data class LoginWithGoogle(val provider: GoogleAuthProvider? = null) : CheckoutAction
    data object LoginWithApple : CheckoutAction

}