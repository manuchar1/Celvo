package com.mtislab.celvo.feature.store.domain.model

import kotlinx.serialization.Serializable


data class StoreItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val formattedPrice: String,
    val type: StoreItemType,
    val supportedCountriesCount: Int,
    val supportedCountries: List<SupportedCountry> = emptyList()
)

enum class StoreItemType {
    COUNTRY, REGION
}

data class StoreCountriesData(
    val topPicks: List<StoreItem>,
    val allCountries: List<StoreItem>
)

@Serializable
data class SupportedCountry(
    val name: String,
    val flagUrl: String
)