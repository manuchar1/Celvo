package com.mtislab.celvo.api.dto

import com.mtislab.celvo.domain.esim.models.DataUnit
import com.mtislab.celvo.domain.esim.models.EsimBundle
import java.math.RoundingMode

data class AppBundleDto(
    val id: String,
    val name: String,
    val dataAmount: String,
    val validity: String,
    val price: java.math.BigDecimal,
    val currency: String,
    val isoCode: String,
    val bestValue: Boolean,
    val is5G: Boolean,

    val originalPrice: java.math.BigDecimal? = null,
    val discountPercent: Int = 0,
    val operators: List<OperatorDto> = emptyList()
)

data class OperatorDto(
    val name: String,
    val networks: List<String>
)

fun EsimBundle.toDto(targetIso: String, isBestValueOverride: Boolean = false): AppBundleDto {


    val formattedData = if (this.dataAmount.value == -1L) {
        "Unlimited"
    } else if (this.dataAmount.unit == DataUnit.MB && this.dataAmount.value >= 1000) {
        "${this.dataAmount.value / 1000} GB"
    } else {
        "${this.dataAmount.value} ${this.dataAmount.unit}"
    }

    val has5GCapability = this.networkTypes.contains("5G")

    val targetCountryIso = this.coverage.countries.find {
        it.isoCode.equals(targetIso, ignoreCase = true)
    }?.isoCode ?: targetIso

    return AppBundleDto(
        id = this.id,
        name = "$formattedData - ${this.validity.value} Days",
        dataAmount = formattedData,
        validity = "${this.validity.value} Days",
        price = this.price.amount.setScale(2, RoundingMode.HALF_UP),
        currency = "$",
        isoCode = targetCountryIso,
        bestValue = isBestValueOverride,
        is5G = has5GCapability,

        originalPrice = null,
        discountPercent = 0,
        operators = emptyList()
    )
}