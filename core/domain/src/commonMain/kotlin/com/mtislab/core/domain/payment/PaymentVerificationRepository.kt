package com.mtislab.core.domain.payment

import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource

interface PaymentVerificationRepository {
    suspend fun verifyPayment(orderId: String): Resource<PaymentVerificationResult, DataError.Remote>
}
