package com.mtislab.celvo.feature.store.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageDto(
    val id: String,
    val name: String,
    @SerialName("dataAmount") val dataAmount: String,
    val validity: String,
    val price: Double,
    val currency: String,
    val isoCode: String,
    val bestValue: Boolean,
    val originalPrice: Double? = null,
    val discountPercent: Int? = 0,
    val operators: List<OperatorDto> = emptyList(),
    val description: String? = null,
    val networkTypes: List<String> = emptyList(),
    val coverageCountries: List<String> = emptyList(),
    val coverageCount: Int = 0,
    val planTier: String? = null,
    val badgeText: String? = null,
    val badgeColor: String? = null
)
