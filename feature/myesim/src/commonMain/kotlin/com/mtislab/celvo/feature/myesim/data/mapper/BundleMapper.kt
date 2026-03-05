package com.mtislab.celvo.feature.myesim.data.mapper

import com.mtislab.celvo.feature.myesim.data.dto.BundleDto
import com.mtislab.celvo.feature.myesim.data.dto.EsimBundlesResponseDto
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundle
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleInfo
import com.mtislab.core.domain.model.PackageInfoCardData

fun EsimBundlesResponseDto.toDomain(): EsimBundleInfo {
    return EsimBundleInfo(
        iccid = this.iccid,
        activeBundle = this.activeBundle?.toDomain(),
        queuedBundles = this.queuedBundles.map { it.toDomain() },
        historyBundles = this.historyBundles.map { it.toDomain() },
        totalBundles = this.summary?.totalBundles ?: 0
    )
}

fun BundleDto.toDomain(): EsimBundle {
    return EsimBundle(
        bundleName = this.bundleName,
        displayName = this.displayName,
        state = this.state,
        stateDisplayName = this.stateDisplayName,
        initialBytes = this.initialBytes,
        remainingBytes = this.remainingBytes,
        usedBytes = this.usedBytes,
        usagePercent = this.usagePercent,
        remainingDays = this.remainingDays,
        endTime = this.endTime,
        unlimited = this.unlimited
    )
}


data class Operators(
    val name: String,
    val networks: List<String>
)
val operators = listOf<Operators>()


fun BundleDto.toPackageInfoCardData(): PackageInfoCardData =
    PackageInfoCardData(
        dataAmountDisplay = displayName,
        validityDisplay = "15",
        countryName = "Georgia",
        isoCode = displayName,
        badgeType = if (true) {
            PackageInfoCardData.BadgeType.Region
        } else {
            PackageInfoCardData.BadgeType.Country
        },
        region = "region",
        primaryOperator = operators.firstOrNull()?.name.orEmpty().ifEmpty { "Network" },
        additionalOperatorCount = (operators.size - 1).coerceAtLeast(0),
    )


