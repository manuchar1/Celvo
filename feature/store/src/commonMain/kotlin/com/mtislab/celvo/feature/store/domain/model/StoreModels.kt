package com.mtislab.celvo.feature.store.domain.model


data class StoreItem(
    val id: String,
    val name: String,
    val imageUrl: String,
    val formattedPrice: String,
    val type: StoreItemType,
    val supportedCountriesCount: Int
)

enum class StoreItemType {
    COUNTRY, REGION
}

data class StoreCountriesData(
    val topPicks: List<StoreItem>,
    val allCountries: List<StoreItem>
)