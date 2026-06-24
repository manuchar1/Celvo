package com.mtislab.celvo.feature.myesim.data.mapper

import com.mtislab.celvo.feature.myesim.data.dto.BundleDto
import com.mtislab.celvo.feature.myesim.data.dto.BundleSummaryDto
import com.mtislab.celvo.feature.myesim.data.dto.EsimBundlesResponseDto
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundle
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleInfo
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleSummary
import com.mtislab.core.domain.model.AssignmentId
import com.mtislab.core.domain.model.BundleDisplay
import com.mtislab.core.domain.model.BundleDisplayBuilders
import com.mtislab.core.domain.model.PackageInfoCardData

fun EsimBundlesResponseDto.toDomain(): EsimBundleInfo {
    return EsimBundleInfo(
        iccid = this.iccid,
        bundles = this.bundles.map { it.toDomain() },
        totalBundles = this.summary?.totalBundles ?: this.bundles.size,
        summary = this.summary?.toDomain()
    )
}

fun BundleDto.toDomain(): EsimBundle {
    return EsimBundle(
        assignmentId = resolveAssignmentId(
            raw = this.assignmentId,
            bundleName = this.bundleName,
            startTime = this.startTime,
            endTime = this.endTime,
            purchasedAt = this.purchasedAt
        ),
        bundleName = this.bundleName,
        displayName = this.displayName,
        state = this.state,
        stateDisplayName = this.stateDisplayName,
        initialBytes = this.initialBytes,
        remainingBytes = this.remainingBytes,
        usedBytes = this.usedBytes,
        usagePercent = this.usagePercent,
        initialFormatted = this.initialFormatted,
        remainingFormatted = this.remainingFormatted,
        usedFormatted = this.usedFormatted,
        isUnlimited = this.isUnlimited,
        startTime = this.startTime,
        endTime = this.endTime,
        remainingDays = this.remainingDays,
        duration = this.duration,
        expiryDate = this.expiryDate,
        purchasedAt = this.purchasedAt,
        queuePosition = this.queuePosition,
        countryCode = this.countryCode.orEmpty(),
        countryName = this.countryName.orEmpty(),
        flagUrl = this.flagUrl.orEmpty(),
        description = this.description,
        throttleSpeedKbps = this.throttleSpeedKbps,
        throttleAfterMb = this.throttleAfterMb,
        bundleGroups = this.bundleGroups,
        networkTypes = this.networkTypes,
        roamingCountries = this.roamingCountries
    )
}

private fun BundleSummaryDto.toDomain(): EsimBundleSummary = EsimBundleSummary(
    totalBundles = totalBundles,
    activeBundleCount = activeBundleCount,
    queuedBundleCount = queuedBundleCount,
    historyBundleCount = historyBundleCount,
    totalDataPurchasedBytes = totalDataPurchasedBytes,
    totalDataUsedBytes = totalDataUsedBytes,
    totalDataRemainingBytes = totalDataRemainingBytes,
    totalDataPurchasedFormatted = totalDataPurchasedFormatted,
    totalDataUsedFormatted = totalDataUsedFormatted,
    totalDataRemainingFormatted = totalDataRemainingFormatted,
    activeRemainingBytes = activeRemainingBytes,
    activeRemainingFormatted = activeRemainingFormatted,
    nextExpiryAt = nextExpiryAt
)

/**
 * Falls back to a deterministic synthetic id when the backend hasn't yet
 * shipped the per-assignment model. Legacy responses can only produce one
 * row per (bundleName, startTime, endTime), so collisions don't occur there.
 */
private fun resolveAssignmentId(
    raw: String?,
    bundleName: String,
    startTime: String?,
    endTime: String?,
    purchasedAt: String?
): AssignmentId {
    val trimmed = raw?.takeIf { it.isNotBlank() }
    return AssignmentId(
        trimmed
            ?: "legacy::$bundleName::${startTime.orEmpty()}::${endTime.orEmpty()}::${purchasedAt.orEmpty()}"
    )
}

fun EsimBundle.toPackageInfoCardData(): PackageInfoCardData =
    PackageInfoCardData(
        dataAmountDisplay = displayName,
        validityDisplay = duration,
        countryName = countryName,
        isoCode = countryCode,
        badgeType = PackageInfoCardData.BadgeType.Country,
        region = "",
        primaryOperator = "",
        additionalOperatorCount = 0,
    )

/**
 * Single conversion point from bundle → UI projection. The unlimited rules
 * (gauge fill, infinity glyph, throttle disclosure) live entirely in
 * [BundleDisplayBuilders], so widgets never branch on `isUnlimited` directly.
 */
fun EsimBundle.toBundleDisplay(): BundleDisplay = BundleDisplay(
    assignmentId = assignmentId,
    bundleName = bundleName,
    displayName = BundleDisplayBuilders.unlimitedDisplayName(isUnlimited, displayName),
    status = BundleDisplayBuilders.parseStatus(state),
    statusLabel = displayStatus,
    isUnlimited = isUnlimited,
    gaugeFillFraction = BundleDisplayBuilders.gaugeFraction(isUnlimited, usagePercent),
    primaryAmountLabel = BundleDisplayBuilders.primaryAmount(
        isUnlimited = isUnlimited,
        remainingFormatted = remainingFormatted,
        initialFormatted = initialFormatted
    ),
    secondaryAmountLabel = BundleDisplayBuilders.secondaryAmount(isUnlimited, initialFormatted),
    daysLeftLabel = BundleDisplayBuilders.formatDaysLeft(remainingDays),
    expiryIso = expiryDate ?: endTime,
    countryCode = countryCode.ifEmpty { null },
    countryName = countryName.ifEmpty { null },
    flagUrl = flagUrl.ifEmpty { null },
    throttle = BundleDisplayBuilders.throttle(
        isUnlimited = isUnlimited,
        capMb = throttleAfterMb,
        speedKbps = throttleSpeedKbps,
        duration = duration
    ),
    // Metered bundles still ship a bundleGroup (e.g. "Standard Fixed"), so the
    // tier lookup has to be gated on isUnlimited — otherwise every metered card
    // gets an "Unlimited" tier badge.
    tier = if (isUnlimited) BundleDisplayBuilders.tier(bundleGroups) else null,
    roamingCountries = roamingCountries,
    networkTypes = networkTypes,
    description = description
)
