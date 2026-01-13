package com.mtislab.celvo.feature.store.presentation.checkout

import com.mtislab.celvo.feature.store.domain.model.EsimPackage

data class CheckoutState(
    val packageDetails: EsimPackage? = null,
    // Auto Top-up State
    val isAutoTopupEnabled: Boolean = false,
    val selectedTopupOption: TopupOption = TopupOptions.first()
)

// დროებითი დატა კლასი ოფციებისთვის
data class TopupOption(
    val id: String,
    val label: String,
    val price: Double,
    val currency: String = "₾"
)

// Hardcoded ოფციები
val TopupOptions = listOf(
    TopupOption("1", "20 GB", 55.00),
    TopupOption("2", "10 GB", 30.00),
    TopupOption("3", "5 GB", 15.00),
    TopupOption("4", "სხვა", 0.0)
)