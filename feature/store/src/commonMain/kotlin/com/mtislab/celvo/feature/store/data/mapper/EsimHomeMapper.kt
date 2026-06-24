package com.mtislab.celvo.feature.store.data.mapper

import com.mtislab.celvo.feature.store.data.dto.EsimHomeItemDto
import com.mtislab.celvo.feature.store.data.dto.EsimHomePackageDto
import com.mtislab.celvo.feature.store.data.dto.EsimHomeResponseDto
import com.mtislab.core.domain.model.ActiveEsimHome
import com.mtislab.core.domain.model.AssignmentId
import com.mtislab.core.domain.model.BundleDisplay
import com.mtislab.core.domain.model.BundleDisplayBuilders
import com.mtislab.core.domain.model.BundleStatus
import com.mtislab.core.domain.model.EsimHomePackage
import com.mtislab.core.domain.model.PackageStatus
import com.mtislab.core.domain.model.ProfileStatus
import com.mtislab.core.domain.model.UserEsim

/**
 * Maps the full /api/v1/esims/home response to domain.
 * Returns null if the eSIM list is empty (user has no eSIMs).
 */
fun EsimHomeResponseDto.toDomain(): ActiveEsimHome? {
    val primaryDto = esims.firstOrNull() ?: return null

    return ActiveEsimHome(
        esims = esims.map { it.toDomain() },
        totalEsimCount = totalEsimCount
    )
}

fun EsimHomeItemDto.toDomain(): UserEsim {
    return UserEsim(
        iccid = iccid,
        esimNumber = esimNumber,
        displayName = displayName,
        profileStatus = profileStatus.toProfileStatus(),
        profileStatusDisplay = profileStatusDisplay,
        installed = isInstalled,
        wasEverInstalled = wasEverInstalled,
        primaryCountryCode = primaryCountryCode,
        primaryFlagUrl = primaryFlagUrl,
        smdpAddress = smdpAddress,
        activationCode = activationCode,
        manualInstallCode = manualInstallCode,
        packages = packages.map { it.toDomain() },
        hasActivePackage = hasActivePackage,
        totalPackageCount = totalPackageCount,
        packagesLoaded = packagesLoaded,
        dataLive = isDataLive
    )
}

fun EsimHomePackageDto.toDomain(): EsimHomePackage {
    return EsimHomePackage(
        assignmentId = resolveAssignmentId(
            raw = assignmentId,
            bundleName = bundleName,
            startTime = startTime,
            endTime = endTime
        ),
        bundleName = bundleName,
        displayName = displayName,
        packageStatus = packageStatus.toPackageStatus(),
        packageStatusDisplay = packageStatusDisplay,
        initialBytes = initialBytes,
        remainingBytes = remainingBytes,
        usedBytes = usedBytes,
        usagePercent = usagePercent,
        initialFormatted = initialFormatted,
        remainingFormatted = remainingFormatted,
        usedFormatted = usedFormatted,
        startTime = startTime,
        endTime = endTime,
        remainingDays = remainingDays,
        duration = duration,
        countryCode = countryCode,
        countryName = countryName,
        flagUrl = flagUrl,
        isActive = isActivePackage,
        isUnlimited = isUnlimited,
        queuePosition = queuePosition,
        description = description,
        throttleSpeedKbps = throttleSpeedKbps,
        throttleAfterMb = throttleAfterMb,
        bundleGroups = bundleGroups,
        networkTypes = networkTypes,
        roamingCountries = roamingCountries
    )
}

/**
 * Falls back to a deterministic synthetic id when the backend hasn't yet
 * shipped the per-assignment model. Legacy responses can only produce one
 * row per (bundleName, startTime, endTime), so this remains unique there.
 */
private fun resolveAssignmentId(
    raw: String?,
    bundleName: String,
    startTime: String?,
    endTime: String?
): AssignmentId {
    val trimmed = raw?.takeIf { it.isNotBlank() }
    return AssignmentId(trimmed ?: "legacy::${bundleName}::${startTime.orEmpty()}::${endTime.orEmpty()}")
}

private fun String.toProfileStatus(): ProfileStatus {
    return when (this.uppercase()) {
        "RELEASED" -> ProfileStatus.RELEASED
        "INSTALLED" -> ProfileStatus.INSTALLED
        "ENABLED" -> ProfileStatus.ENABLED
        "DISABLED" -> ProfileStatus.DISABLED
        else -> ProfileStatus.UNKNOWN
    }
}

private fun String.toPackageStatus(): PackageStatus {
    return when (this.uppercase()) {
        "ACTIVE" -> PackageStatus.ACTIVE
        "QUEUED" -> PackageStatus.QUEUED
        "EXPIRED" -> PackageStatus.EXPIRED
        "DEPLETED" -> PackageStatus.DEPLETED
        else -> PackageStatus.UNKNOWN
    }
}

/**
 * Single conversion point from home package → UI projection. The unlimited
 * rules (gauge fill, infinity glyph, throttle disclosure) live entirely in
 * [BundleDisplayBuilders], so widgets never branch on `isUnlimited` directly.
 */
fun EsimHomePackage.toBundleDisplay(): BundleDisplay = BundleDisplay(
    assignmentId = assignmentId,
    bundleName = bundleName,
    displayName = BundleDisplayBuilders.unlimitedDisplayName(isUnlimited, displayName),
    status = BundleDisplayBuilders.parseStatus(packageStatus.name),
    statusLabel = packageStatusDisplay,
    isUnlimited = isUnlimited,
    gaugeFillFraction = BundleDisplayBuilders.gaugeFraction(isUnlimited, usagePercent),
    primaryAmountLabel = BundleDisplayBuilders.primaryAmount(
        isUnlimited = isUnlimited,
        remainingFormatted = remainingFormatted,
        initialFormatted = initialFormatted
    ),
    secondaryAmountLabel = BundleDisplayBuilders.secondaryAmount(isUnlimited, initialFormatted),
    daysLeftLabel = BundleDisplayBuilders.formatDaysLeft(remainingDays),
    expiryIso = endTime,
    countryCode = countryCode,
    countryName = countryName,
    flagUrl = flagUrl,
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