package com.mtislab.celvo.infrastructure.esim.esimgo.persistence

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "esim_bundles_cache_v3")
data class EsimBundleEntity(
    @Id
    @Column(length = 500)
    val id: String,

    @Column(columnDefinition = "TEXT")
    val description: String?,

    val dataAmountMb: Long,
    val durationDays: Int,

    @Column(precision = 10, scale = 2)
    val price: BigDecimal,

    val currency: String,

    val imageUrl: String?,

    @Column(columnDefinition = "TEXT")
    val supportedCountriesIso: String,
    val has5g: Boolean = false,
    @Column(columnDefinition = "TEXT")
    val roamingDataJson: String? = null
) {
    constructor() : this("", null, 0, 0, BigDecimal.ZERO, "USD", null, "")
}