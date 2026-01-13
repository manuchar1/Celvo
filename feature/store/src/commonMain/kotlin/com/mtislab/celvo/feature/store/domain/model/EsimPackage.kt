package com.mtislab.celvo.feature.store.domain.model

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

    val operators: List<PackageOperator>
)

