package com.mtislab.celvo.feature.myesim.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsimItemDto(
    @SerialName("iccid") val iccid: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("statusLabel") val statusLabel: String? = null,
    @SerialName("primaryAction") val primaryAction: String? = null,
    @SerialName("flagUrl") val flagUrl: String? = null,
    @SerialName("totalBundles") val totalBundles: Int? = null,
    @SerialName("profileStatus") val profileStatus: String? = null,
    @SerialName("firstInstalledAt") val firstInstalledAt: String? = null,
    @SerialName("smdpAddress") val smdpAddress: String? = null,
    @SerialName("activationCode") val activationCode: String? = null,
    @SerialName("manualInstallCode") val manualInstallCode: String? = null,
    @SerialName("lastOrderDate") val lastOrderDate: String? = null,
    @SerialName("primaryCountryCode") val primaryCountryCode: String? = null,
    @SerialName("countries") val countries: List<EsimCountryDto>? = null
)

@Serializable
data class EsimCountryDto(
    @SerialName("isoCode") val isoCode: String? = null,
    @SerialName("flagUrl") val flagUrl: String? = null
)