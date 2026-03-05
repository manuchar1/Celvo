package com.mtislab.celvo.feature.myesim.domain.model

import kotlin.time.Instant

data class UserEsim(
    val id: String,
    val iccid: String,
    val status: EsimStatus,
    val statusDisplayName: String,
    val userLabel: String,
    val totalBundles: Int,
    val primaryCountryCode: String,
    val flagUrl: String,
    val supportedCountries: List<EsimCountry>,
    val installation: InstallationInfo,
    val primaryAction: String?,
    val firstInstalledAt: Instant?,
    val lastOrderDate: Instant?
)

data class EsimCountry(
    val isoCode: String,
    val flagUrl: String
)

data class InstallationInfo(
    val smdpAddress: String,
    val activationCode: String,
    val manualCode: String,
    val isReady: Boolean
)

enum class EsimStatus {
    RELEASED,
    DOWNLOADED,
    INSTALLED,
    ENABLED,
    DELETED,
    ACTIVE,
    EXPIRED,
    PENDING,
    UNKNOWN,
    FAILED;

    companion object {
        fun fromString(status: String): EsimStatus {
            return entries.find { it.name.equals(status, ignoreCase = true) } ?: UNKNOWN
        }
    }
}