package com.mtislab.celvo.feature.myesim.domain.model

data class EsimBundleInfo(
    val iccid: String,
    val activeBundle: EsimBundle?,
    val queuedBundles: List<EsimBundle>,
    val historyBundles: List<EsimBundle>,
    val totalBundles: Int
) {
    val isSingleBundle: Boolean
        get() = totalBundles == 1

    val singleBundle: EsimBundle?
        get() = when {
            totalBundles != 1 -> null
            activeBundle != null -> activeBundle
            queuedBundles.size == 1 -> queuedBundles.first()
            historyBundles.size == 1 -> historyBundles.first()
            else -> null
        }
}

data class EsimBundle(
    val bundleName: String,
    val displayName: String,
    val state: String,
    val stateDisplayName: String,
    val initialBytes: Long,
    val remainingBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val initialFormatted: String,
    val remainingFormatted: String,   // დაემატა
    val usedFormatted: String,        // დაემატა
    val isUnlimited: Boolean,         // სახელი შესწორდა სერვერის მიხედვით
    val startTime: String?,           // დაემატა
    val endTime: String?,
    val remainingDays: Int?,
    val duration: String?,
    val expiryDate: String?,
    val assignmentId: String?,
    val countryCode: String,          // დაემატა
    val countryName: String,          // დაემატა
    val flagUrl: String               // დაემატა
)