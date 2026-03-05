package com.mtislab.celvo.feature.myesim.data.mapper

import com.mtislab.celvo.feature.myesim.data.dto.EsimItemDto
import com.mtislab.celvo.feature.myesim.domain.model.EsimCountry
import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus
import com.mtislab.celvo.feature.myesim.domain.model.InstallationInfo
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import kotlin.time.Instant

fun EsimItemDto.toDomain(): UserEsim {
    val safeIccid = this.iccid.orEmpty()
    val stableId = safeIccid.ifBlank {
        "${this.smdpAddress.orEmpty()}_${this.activationCode.orEmpty()}"
    }

    val mappedCountries = this.countries?.map {
        EsimCountry(
            isoCode = it.isoCode.orEmpty(),
            flagUrl = it.flagUrl.orEmpty()
        )
    }.orEmpty()

    return UserEsim(
        id = stableId,
        iccid = safeIccid,
        status = EsimStatus.fromString(this.profileStatus.orEmpty()),
        statusDisplayName = this.statusLabel.orEmpty(),
        userLabel = this.displayName ?: "eSIM",
        totalBundles = this.totalBundles ?: 0,
        primaryCountryCode = this.primaryCountryCode.orEmpty(),
        flagUrl = this.flagUrl.orEmpty(),
        supportedCountries = mappedCountries,
        installation = InstallationInfo(
            smdpAddress = this.smdpAddress.orEmpty(),
            activationCode = this.activationCode.orEmpty(),
            manualCode = this.manualInstallCode.orEmpty(),
            isReady = !this.activationCode.isNullOrEmpty()
        ),
        primaryAction = this.primaryAction,
        firstInstalledAt = this.firstInstalledAt?.let { parseIsoDate(it) },
        lastOrderDate = this.lastOrderDate?.let { parseIsoDate(it) }
    )
}

private fun parseIsoDate(dateString: String): Instant? {
    return try {
        Instant.parse(dateString)
    } catch (e: Exception) {
        null
    }
}