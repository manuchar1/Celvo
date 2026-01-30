package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountriesResponseDto(
    @SerialName("topPicks") val topPicks: List<DestinationDto>,
    @SerialName("allDestinations") val allDestinations: List<DestinationDto>
)

@Serializable
data class RegionsResponseDto(
    @SerialName("regions") val regions: List<DestinationDto>
)

@Serializable
data class DestinationDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("flagUrl") val flagUrl: String,
    @SerialName("minPrice") val minPrice: Double,
    @SerialName("type") val type: String,


    @SerialName("coverageCount") val coverageCount: Int? = null,
    @SerialName("supportedCountries") val supportedCountries: List<SupportedCountryDto>? = null
)

@Serializable
data class SupportedCountryDto(
    @SerialName("name") val name: String,
    @SerialName("flagUrl") val flagUrl: String
)

@Serializable
data class PaymentInitiateRequestDto(
    @SerialName("amount") val amount: Double,
    @SerialName("sku") val sku: String,
    @SerialName("bundleName") val bundleName: String,
    @SerialName("currency") val currency: String,
    @SerialName("language") val language: String,
    @SerialName("theme") val theme: String
)

@Serializable
data class PaymentInitiateResponseDto(
    @SerialName("redirectUrl") val redirectUrl: String
)