package com.mtislab.celvo.domain.esim.models

import java.math.BigDecimal

/**
 * Domain model representing an eSIM data bundle.
 * Updated to support rich country info and categories.
 */
data class EsimBundle(
    val id: String,
    val name: String,
    val description: String?,
    val dataAmount: DataAmount,
    val validity: ValidityPeriod,
    val coverage: Coverage,
    val category: BundleCategory,
    val price: Price,
    val type: BundleType,
    val imageUrl: String? = null,
    val networkTypes: List<String> = listOf("4G")
)

data class DataAmount(
    val value: Long,
    val unit: DataUnit = DataUnit.MB
) {
    fun toReadableString(): String = when {
        value >= 1024 -> "${value / 1024} GB"
        else -> "$value MB"
    }
}

enum class DataUnit {
    MB, GB
}

data class ValidityPeriod(
    val value: Int,
    val unit: TimeUnit
) {
    fun toReadableString(): String = "$value ${unit.name.lowercase()}"
}

enum class TimeUnit {
    DAYS, MONTHS, YEARS
}


data class Coverage(
    val countries: List<CountryInfo>,
    val regions: List<String> = emptyList()
) {
    fun isGlobal(): Boolean = regions.contains("GLOBAL")
}

data class CountryInfo(
    val isoCode: String,
    val name: String,
    val flagEmoji: String,
    val networks: List<NetworkInfo> = emptyList()
)

data class NetworkInfo(
    val name: String,
    val types: List<String>
)

enum class BundleCategory {
    SINGLE_COUNTRY,
    REGIONAL,
    GLOBAL
}
// -------------------------

data class Price(
    val amount: BigDecimal,
    val currency: String
)

enum class BundleType {
    NEW,
    TOPUP
}