package com.mtislab.celvo.feature.myesim.domain.model

/**
 * Domain model representing a user's purchased eSIM.
 */
data class UserEsim(
    val id: String,
    val iccid: String,
    val status: EsimStatus, // profileStatus-დან
    val statusDisplayName: String, // statusLabel-დან
    val userLabel: String?, // displayName-დან
    val country: EsimCountry,

    // ⚠️ ესენი გახდა Nullable, რადგან ახალ API-ს არ მოაქვს
    val dataUsage: DataUsage?,
    val validity: EsimValidity?,

    val installation: InstallationInfo,
    val primaryAction: String?, // შესამოწმებლად ("INSTALL" ?)

    // დანარჩენი რაც ძველს ჰქონდა, დეფოლტებით შევავსებთ
    val purchaseDateFormatted: String = ""
) {

}

/**
 * eSIM status enumeration.
 * შეიცავს ყველა შესაძლო სტატუსს.
 */
enum class EsimStatus {
    ACTIVE,         // აქტიური
    INACTIVE,       // გამორთული
    EXPIRED,        // ვადაგასული
    PENDING,        // მუშავდება
    UNKNOWN,        // უცნობი


    PROVISIONED,    // გასააქტიურებელი (ნაყიდია, QR არ დასკანერებულა)
    INSTALLED,      // დაინსტალირებული (QR დასკანერდა, არ ჩართულა)
    DEPLETED,       // ამოწურული
    DELETED,        // წაშლილი
    FAILED;         // შეცდომა

    companion object {
        fun fromString(value: String): EsimStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

// ქვე-კლასები იგივე რჩება
data class EsimCountry(
    val code: String,
    val name: String,
    val flagUrl: String,
    val isRegion: Boolean
) {
    companion object {
        fun empty() = EsimCountry("", "უცნობი", "", false)
    }
}

data class DataUsage(
    val totalGB: Double,
    val usedGB: Double,
    val remainingGB: Double,
    val usagePercent: Float,
    val totalFormatted: String,
    val usedFormatted: String,
    val remainingFormatted: String,
    val isUnlimited: Boolean,
    val lastSyncedAt: String?
) {
    companion object {
        fun empty() = DataUsage(0.0, 0.0, 0.0, 0f, "0 GB", "0 GB", "0 GB", false, null)
    }
}

data class EsimValidity(
    val validityDays: Int,
    val remainingDays: Int,
    val activationDate: String?,
    val activationDateFormatted: String?,
    val expirationDate: String?,
    val expirationDateFormatted: String?,
    val isExpired: Boolean,
    val isActivated: Boolean
) {
    companion object {
        fun empty() = EsimValidity(0, 0, null, null, null, null, false, false)
    }
}

data class EsimOperator(
    val name: String,
    val country: String,
    val networkTypes: List<String>,
    val fullDisplayName: String,
    val networkTypesFormatted: String
) {
    companion object {
        fun empty() = EsimOperator("", "", emptyList(), "", "")
    }
}

data class InstallationInfo(
    val smdpAddress: String,
    val activationCode: String,
    val manualCode: String,
    val qrCodeUrl: String,
    val isReady: Boolean
) {
    companion object {
        fun empty() = InstallationInfo("", "", "", "", false)
    }
}