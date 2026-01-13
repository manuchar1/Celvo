package com.mtislab.celvo.feature.store.data.mapper

// ⚠️ ყურადღება მიაქციე ამ იმპორტებს!
import com.mtislab.celvo.feature.store.data.dto.OperatorDto
import com.mtislab.celvo.feature.store.data.dto.PackageDto
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.PackageOperator

fun PackageDto.toDomain(): EsimPackage {
    val isUnlimitedPkg = this.dataAmount.contains("Unlimited", ignoreCase = true) ||
            this.dataAmount.contains("-1")

    return EsimPackage(
        id = this.id,
        name = this.name,
        dataAmountDisplay = if (isUnlimitedPkg) "Unlimited Data" else this.dataAmount,
        validityDisplay = this.validity,
        price = this.price,
        currency = this.currency,
        isUnlimited = isUnlimitedPkg,
        isBestValue = this.bestValue,
        isoCode = this.isoCode,
        originalPrice = if (this.originalPrice != null && this.originalPrice > 0) this.originalPrice else null,
        discountPercent = if (this.discountPercent != null && this.discountPercent > 0) this.discountPercent else null,

        // --- Mapping Operators ---

        operators = this.operators.map { it.toDomain() }
    )
}


fun OperatorDto.toDomain(): PackageOperator {
    return PackageOperator(
        name = this.name,
        networks = this.networks
    )
}