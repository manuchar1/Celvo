package com.mtislab.celvo.feature.myesim.presentation.details

import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.domain.utils.DataError

/**
 * UI State for the eSim Details Screen.
 */
data class EsimDetailsState(
    val isLoading: Boolean = false,
    val esim: UserEsim? = null,
    val error: DataError? = null,
    val showQrCodeSheet: Boolean = false,
    val showOperatorsSheet: Boolean = false,
    val isEditingLabel: Boolean = false,
    val editLabelText: String = "",
    val isUpdatingLabel: Boolean = false
) {
    /**
     * Data usage progress for the gauge (0.0 to 1.0).
     */
    val usageProgress: Float
        get() = esim?.dataUsage?.let {
            if (it.isUnlimited) 0f
            else (it.usagePercent / 100f).coerceIn(0f, 1f)
        } ?: 0f

    /**
     * Display text for remaining data (e.g., "1.2 GB").
     */
    val remainingDataDisplay: String
        get() = esim?.dataUsage?.let {
            if (it.isUnlimited) "Unlimited" else it.remainingFormatted
        } ?: ""

    /**
     * Display text for total data (e.g., "2 GB" or "∞").
     */
    val totalDataDisplay: String
        get() = esim?.dataUsage?.let {
            if (it.isUnlimited) "∞" else it.totalFormatted
        } ?: ""

    /**
     * Display text for used data (e.g., "0.8 GB").
     */
    val usedDataDisplay: String
        get() = esim?.dataUsage?.usedFormatted ?: ""



    /**
     * Validity display text (e.g., "15 დღე (მთ 6, 2026)").
     */
    val validityDisplay: String
        get() = esim?.validity?.let {
            if (it.isActivated && it.expirationDateFormatted != null) {
                "${it.remainingDays} დღე (${it.expirationDateFormatted})"
            } else {
                "${it.validityDays} დღე"
            }
        } ?: ""

    /**
     * Status display text.
     */
    val statusDisplay: String
        get() = esim?.statusDisplayName ?: ""

    /**
     * Check if content should be shown.
     */
    val showContent: Boolean
        get() = !isLoading && esim != null && error == null

    /**
     * Check if error state should be shown.
     */
    val showError: Boolean
        get() = !isLoading && error != null


}