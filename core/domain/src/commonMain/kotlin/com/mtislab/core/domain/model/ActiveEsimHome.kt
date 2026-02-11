package com.mtislab.core.domain.model

/**
 * Domain model for /api/v1/esims/home GET response.
 * Represents the user's active eSIM state on the Home screen.
 */
data class ActiveEsimHome(
    val hasActiveBundle: Boolean,
    val activeBundle: ActiveBundle?,
    val queuedBundleCount: Int
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
    val startTime: String,
    val endTime: String,
    val remainingDays: Int,
    val countryCode: String,
    val countryName: String,
    val flagUrl: String
)