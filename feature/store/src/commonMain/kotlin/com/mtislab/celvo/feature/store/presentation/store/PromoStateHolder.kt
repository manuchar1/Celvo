package com.mtislab.celvo.feature.store.presentation.store // Adjust package if needed

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A lightweight global state holder to keep track of promo codes
 * claimed by the user during this app session.
 */
object PromoStateHolder {
    private val _claimedPromoCode = MutableStateFlow<String?>(null)
    val claimedPromoCode: StateFlow<String?> = _claimedPromoCode.asStateFlow()

    fun claimCode(code: String) {
        _claimedPromoCode.value = code
    }

    fun clearCode() {
        _claimedPromoCode.value = null
    }
}