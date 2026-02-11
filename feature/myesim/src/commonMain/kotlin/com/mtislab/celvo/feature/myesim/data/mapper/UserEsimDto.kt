package com.mtislab.celvo.feature.myesim.data.mapper

import com.mtislab.celvo.feature.myesim.data.dto.DataUsageDto
import com.mtislab.celvo.feature.myesim.data.dto.EsimCountryDto
import com.mtislab.celvo.feature.myesim.data.dto.EsimOperatorDto
import com.mtislab.celvo.feature.myesim.data.dto.EsimValidityDto
import com.mtislab.celvo.feature.myesim.data.dto.InstallationInfoDto
import com.mtislab.celvo.feature.myesim.data.dto.UserEsimDto
import com.mtislab.celvo.feature.myesim.domain.model.DataUsage
import com.mtislab.celvo.feature.myesim.domain.model.EsimCountry
import com.mtislab.celvo.feature.myesim.domain.model.EsimOperator
import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus
import com.mtislab.celvo.feature.myesim.domain.model.EsimValidity
import com.mtislab.celvo.feature.myesim.domain.model.InstallationInfo
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim

/**
 * Extension functions to map DTOs to Domain models.
 * All mappings are null-safe with sensible defaults.
 */

/*fun UserEsimDto.toDomain(): UserEsim {
    return UserEsim(
        id = id,
        iccid = iccid.orEmpty(),
        status = EsimStatus.fromString(status.orEmpty()),
        statusDisplayName = statusDisplayName ?: getDefaultStatusDisplayName(status),
        packageName = packageName.orEmpty(),
        bundleSku = bundleSku.orEmpty(),
        userLabel = userLabel,
        country = country?.toDomain() ?: EsimCountry.empty(),
        dataUsage = dataUsage?.toDomain() ?: DataUsage.empty(),
        validity = validity?.toDomain() ?: EsimValidity.empty(),
        networkTypes = networkTypes.orEmpty(),
        networkSpeed = networkSpeed.orEmpty(),
        operators = operators?.map { it.toDomain() }.orEmpty(),
        operatorsSummary = operatorsSummary.orEmpty(),
        installation = installation?.toDomain() ?: InstallationInfo.empty(),
        autoRenewalEnabled = autoRenewalEnabled ?: false,
        canTopUp = canTopUp ?: false,
        canRenew = canRenew ?: false,
        purchaseDate = purchaseDate.orEmpty(),
        purchaseDateFormatted = purchaseDateFormatted.orEmpty()
    )
}*/

fun EsimCountryDto.toDomain(): EsimCountry {
    return EsimCountry(
        code = code.orEmpty(),
        name = name.orEmpty(),
        flagUrl = flagUrl.orEmpty(),
        isRegion = isRegion ?: false
    )
}

fun DataUsageDto.toDomain(): DataUsage {
    return DataUsage(
        totalGB = totalGB ?: 0.0,
        usedGB = usedGB ?: 0.0,
        remainingGB = remainingGB ?: 0.0,
        usagePercent = usagePercent ?: 0f,
        totalFormatted = totalFormatted ?: formatGB(totalGB ?: 0.0),
        usedFormatted = usedFormatted ?: formatGB(usedGB ?: 0.0),
        remainingFormatted = remainingFormatted ?: formatGB(remainingGB ?: 0.0),
        isUnlimited = isUnlimited ?: false,
        lastSyncedAt = lastSyncedAt
    )
}

fun EsimValidityDto.toDomain(): EsimValidity {
    return EsimValidity(
        validityDays = validityDays ?: 0,
        remainingDays = remainingDays ?: 0,
        activationDate = activationDate,
        activationDateFormatted = activationDateFormatted,
        expirationDate = expirationDate,
        expirationDateFormatted = expirationDateFormatted,
        isExpired = isExpired ?: false,
        isActivated = isActivated ?: false
    )
}

fun EsimOperatorDto.toDomain(): EsimOperator {
    return EsimOperator(
        name = name.orEmpty(),
        country = country.orEmpty(),
        networkTypes = networkTypes.orEmpty(),
        fullDisplayName = fullDisplayName.orEmpty(),
        networkTypesFormatted = networkTypesFormatted.orEmpty()
    )
}

fun InstallationInfoDto.toDomain(): InstallationInfo {
    return InstallationInfo(
        smdpAddress = smdpAddress.orEmpty(),
        activationCode = activationCode.orEmpty(),
        manualCode = manualCode.orEmpty(),
        qrCodeUrl = qrCodeUrl.orEmpty(),
        isReady = isReady ?: false
    )
}

// Helper functions

private fun getDefaultStatusDisplayName(status: String?): String {
    return when (EsimStatus.fromString(status.orEmpty())) {
        EsimStatus.ACTIVE -> "აქტიური"
        EsimStatus.INACTIVE -> "არააქტიური"
        EsimStatus.EXPIRED -> "ვადაგასული"
        EsimStatus.PENDING -> "მოლოდინში"
        EsimStatus.UNKNOWN -> "უცნობი"
        EsimStatus.PROVISIONED -> "გასააქტიურებელი"
        else -> {"KLYUN"}
    }
}

private fun formatGB(gb: Double): String {
    return if (gb >= 1.0) {
        "${((gb * 10).toInt() / 10.0)} GB"
    } else {
        "${(gb * 1024).toInt()} MB"
    }
}