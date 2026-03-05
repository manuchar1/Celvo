package com.mtislab.celvo.feature.store.data.mapper


import com.mtislab.celvo.feature.store.data.dto.OperatorDto
import com.mtislab.celvo.feature.store.data.dto.PackageDto
import com.mtislab.celvo.feature.store.data.dto.PromoValidationResponseDto
import com.mtislab.celvo.feature.store.data.dto.WalletPaymentResponseDto
import com.mtislab.celvo.feature.store.domain.model.EsimPackage
import com.mtislab.celvo.feature.store.domain.model.PackageOperator
import com.mtislab.celvo.feature.store.domain.model.PromoValidationResult
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentResult
import com.mtislab.celvo.feature.store.domain.model.WalletPaymentStatus
import com.mtislab.core.domain.model.PackageInfoCardData

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
        operators = this.operators.map { it.toDomain() }
    )
}


fun OperatorDto.toDomain(): PackageOperator {
    return PackageOperator(
        name = this.name,
        networks = this.networks
    )
}


fun EsimPackage.toPackageInfoCardData(
    countryName: String,
    type: String,
    region: String
): PackageInfoCardData =
    PackageInfoCardData(
        dataAmountDisplay = dataAmountDisplay,
        validityDisplay = validityDisplay,
        countryName = countryName,
        isoCode = isoCode,
        badgeType = if (type == "REGION") {
            PackageInfoCardData.BadgeType.Region
        } else {
            PackageInfoCardData.BadgeType.Country
        },
        region = region,
        primaryOperator = operators.firstOrNull()?.name.orEmpty().ifEmpty { "Network" },
        additionalOperatorCount = (operators.size - 1).coerceAtLeast(0),
    )




fun PromoValidationResponseDto.toDomain(): PromoValidationResult {
    return PromoValidationResult(
        valid = valid,
        codeId = codeId,
        codeType = codeType,
        discountAmount = discountAmount ?: 0.0,
        discountDisplay = discountDisplay,
        originalPrice = originalPrice ?: 0.0,
        finalPrice = finalPrice ?: 0.0,
        walletBalance = walletBalance ?: 0.0,
        isReferral = isReferral ?: false,
        referrerUserId = referrerUserId,
        displayName = displayName,
        errorCode = errorCode,
        errorMessage = errorMessage
    )
}


fun WalletPaymentResponseDto.toDomain(): WalletPaymentResult {
    return WalletPaymentResult(
        orderId = orderId,
        status = when (status.uppercase()) {
            "COMPLETED" -> WalletPaymentStatus.COMPLETED
            "REQUIRES_3DS", "REDIRECT" -> WalletPaymentStatus.REQUIRES_3DS
            else -> WalletPaymentStatus.FAILED
        },
        redirectUrl = redirectUrl
    )
}