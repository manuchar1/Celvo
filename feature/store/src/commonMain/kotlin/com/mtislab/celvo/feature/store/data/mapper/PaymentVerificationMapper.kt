package com.mtislab.celvo.feature.store.data.mapper

import com.mtislab.celvo.feature.store.data.dto.PaymentVerificationResponseDto
import com.mtislab.core.domain.esim.EsimInstallationData
import com.mtislab.core.domain.payment.OrderType
import com.mtislab.core.domain.payment.PaymentVerificationResult
import com.mtislab.core.domain.payment.VerificationStatus

fun PaymentVerificationResponseDto.toDomain(): PaymentVerificationResult {
    val verificationStatus = when (status.uppercase()) {
        "COMPLETED" -> VerificationStatus.COMPLETED
        "FAILED" -> VerificationStatus.FAILED
        "PENDING" -> VerificationStatus.PENDING
        "REFUNDED" -> VerificationStatus.REFUNDED
        else -> VerificationStatus.FAILED
    }

    val type = when (orderType.uppercase()) {
        "NEW_ESIM" -> OrderType.NEW_ESIM
        "TOP_UP" -> OrderType.TOP_UP
        else -> OrderType.NEW_ESIM
    }

    val esimData = if (type == OrderType.NEW_ESIM &&
        smdpAddress != null && activationCode != null
    ) {
        EsimInstallationData(
            smdpAddress = smdpAddress,
            activationCode = activationCode,
            manualCode = "LPA:1\$$smdpAddress\$$activationCode"
        )
    } else null

    return PaymentVerificationResult(
        status = verificationStatus,
        orderType = type,
        esimData = esimData,
        iccid = iccid,
        bundleName = bundleName,
        countryCode = countryCode,
        totalDataInGB = totalDataInGB,
        durationDays = durationDays,
        provisioningStatus = provisioningStatus
    )
}
