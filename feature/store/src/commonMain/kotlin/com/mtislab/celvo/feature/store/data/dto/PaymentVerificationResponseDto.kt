package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentVerificationResponseDto(
    @SerialName("status") val status: String,
    @SerialName("orderType") val orderType: String,
    @SerialName("bundleName") val bundleName: String,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("smdpAddress") val smdpAddress: String? = null,
    @SerialName("activationCode") val activationCode: String? = null,
    @SerialName("iccid") val iccid: String? = null,
    @SerialName("provisioningStatus") val provisioningStatus: String? = null,
    @SerialName("totalDataInGB") val totalDataInGB: Double? = null,
    @SerialName("durationDays") val durationDays: Int? = null
)
