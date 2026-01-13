package com.mtislab.celvo.feature.store.presentation.checkout

import com.mtislab.celvo.feature.store.domain.model.EsimPackage

sealed interface CheckoutAction {
    data class Init(val pkg: EsimPackage) : CheckoutAction
    data class ToggleAutoTopup(val enabled: Boolean) : CheckoutAction
    data class SelectTopupOption(val option: TopupOption) : CheckoutAction

}