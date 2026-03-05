package com.mtislab.core.data.payment

import com.mtislab.core.domain.payment.NativePayManager

class NativePayManagerIos : NativePayManager {
    override suspend fun isAvailable(): Boolean = false // TODO: Apple Pay via PassKit
}