package com.mtislab.celvo.feature.store.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class EsimPackage(
    val id: String,
    val name: String,
    val dataAmountDisplay: String,
    val validityDisplay: String,
    val price: Double,
    val currency: String,
    val isUnlimited: Boolean,
    val isBestValue: Boolean,
    val isoCode: String,
    val originalPrice: Double?,
    val discountPercent: Int?,
    val operators: List<PackageOperator>,
    val description: String? = null,
    val networkTypes: List<String> = emptyList(),
    val is5G: Boolean = false,
    val coverageCountries: List<String> = emptyList(),
    val coverageCount: Int = 0,
    val planTier: String? = null,
    val badgeText: String? = null,
    val badgeColor: String? = null
)
