package com.mtislab.core.domain.payment

/**
 * Checks native wallet payment readiness.
 * Injected into ViewModels via Koin.
 */
interface NativePayManager {
    suspend fun isAvailable(): Boolean
}