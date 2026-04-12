package com.mtislab.celvo.feature.store.data.mapper

import com.mtislab.celvo.feature.store.data.dto.EsimHomeItemDto
import com.mtislab.celvo.feature.store.data.dto.EsimHomePackageDto
import com.mtislab.celvo.feature.store.data.dto.EsimHomeResponseDto
import com.mtislab.core.domain.model.ActiveEsimHome
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
        countryCode = countryCode,
        countryName = countryName,
        flagUrl = flagUrl,
        isActive = isActivePackage,
        isUnlimited = isUnlimited
    )
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