package com.mtislab.core.domain.payment

import com.mtislab.core.domain.esim.EsimInstallationData

data class PaymentVerificationResult(
    val status: VerificationStatus,
    val orderType: OrderType,
    val esimData: EsimInstallationData?,
    val iccid: String?,
    val bundleName: String,
    val countryCode: String?,
    val totalDataInGB: Double?,
    val durationDays: Int?,
    val provisioningStatus: String?
)
