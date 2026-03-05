package com.mtislab.core.domain.model

/**
 * Domain model for GET /api/v1/esims/home.
 * [esims] is sorted by relevance — index 0 is the most relevant.
 */
data class ActiveEsimHome(
    val esims: List<UserEsim>,
    val totalEsimCount: Int
) {
    val primaryEsim: UserEsim? get() = esims.firstOrNull()
    val hasActiveBundle: Boolean get() = primaryEsim?.hasActivePackage == true
    val activeBundle: ActiveBundle? get() = primaryEsim?.activeBundle
    val isInstalled: Boolean get() = primaryEsim?.installed == true
}

data class UserEsim(
    val iccid: String,
    val esimNumber: Int,
    val displayName: String,
    val profileStatus: ProfileStatus,
    val profileStatusDisplay: String,
    val installed: Boolean,
    val wasEverInstalled: Boolean,
    val primaryCountryCode: String,
    val primaryFlagUrl: String,
    val smdpAddress: String,
    val activationCode: String,
    val manualInstallCode: String,
    val packages: List<EsimHomePackage>,
    val hasActivePackage: Boolean,
    val totalPackageCount: Int,
    val dataLive: Boolean
) {
    val activeBundle: ActiveBundle?
        get() {
            val pkg = packages.firstOrNull { it.isActive }
                ?: packages.firstOrNull()
                ?: return null
            return ActiveBundle(
                bundleName = pkg.bundleName,
                displayName = pkg.displayName,
                initialBytes = pkg.initialBytes,
                remainingBytes = pkg.remainingBytes,
                usedBytes = pkg.usedBytes,
                usagePercent = pkg.usagePercent,
                initialFormatted = pkg.initialFormatted,
                remainingFormatted = pkg.remainingFormatted,
                usedFormatted = pkg.usedFormatted,
                isUnlimited = pkg.isUnlimited,
                startTime = pkg.startTime,
                endTime = pkg.endTime,
                remainingDays = pkg.remainingDays,
                countryCode = pkg.countryCode,
                countryName = pkg.countryName,
                flagUrl = pkg.flagUrl
            )
        }

    val headerLabel: String
        get() {
            val status = profileStatusDisplay //if (hasActivePackage) "აქტიური" else "არააქტიური"
            return "eSIM #$esimNumber ($status)"
        }


}

data class EsimHomePackage(
    val bundleName: String,
    val displayName: String,
    val packageStatus: PackageStatus,
    val packageStatusDisplay: String,
    val initialBytes: Long,
    val remainingBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val initialFormatted: String,
    val remainingFormatted: String,
    val usedFormatted: String,
    val startTime: String?,
    val endTime: String?,
    val remainingDays: Int?,
    val countryCode: String,
    val countryName: String,
    val flagUrl: String,
    val isActive: Boolean,
    val isUnlimited: Boolean
)

data class ActiveBundle(
    val bundleName: String,
    val displayName: String,
    val initialBytes: Long,
    val remainingBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val initialFormatted: String,
    val remainingFormatted: String,
    val usedFormatted: String,
    val isUnlimited: Boolean,
    val startTime: String?,
    val endTime: String?,
    val remainingDays: Int?,
    val countryCode: String,
    val countryName: String,
    val flagUrl: String
)

enum class ProfileStatus { RELEASED, INSTALLED, ENABLED, DISABLED, UNKNOWN }
enum class PackageStatus { ACTIVE, QUEUED, EXPIRED, DEPLETED, UNKNOWN }