package com.mtislab.celvo.feature.myesim.presentation.list

import com.mtislab.celvo.feature.myesim.domain.model.EsimStatus
import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.domain.utils.DataError

/**
 * UI State for the My eSIM List screen.
 *
 * Manages three concerns:
 * 1. eSIM list loading state
 * 2. eSIM installation state
 * 3. Resolution dialog state
 */
data class MyEsimListState(
    // List loading state
    val isLoading: Boolean = false,
    val esims: List<UserEsim> = emptyList(),
    val error: DataError? = null,

    // eSIM Installation state
    val isInstalling: Boolean = false,
    val installingEsimId: String? = null,
    val installationError: InstallationError? = null,

    /**
     * Platform-specific resolution data.
     *
     * On Android: This is a PendingIntent from EuiccManager
     * On iOS: Always null (iOS handles consent natively)
     *
     * When non-null, the UI should launch the resolution intent.
     * After launching, dispatch [MyEsimListAction.ResolutionLaunched] to clear this.
     *
     * IMPORTANT: After launching, do NOT interpret the Activity Result.
     * Samsung always returns RESULT_CANCELED. The real result arrives via
     * a second broadcast to the installer's BroadcastReceiver, which
     * emits Success/Error through the installation flow.
     */
    val resolutionRequired: Any? = null
) {
    /**
     * Filtered and sorted eSIM list for display.
     *
     * Sort by actionability priority:
     *   PROVISIONED (needs activation) → INSTALLED (ready) → ACTIVE → Others
     */
    val filteredEsims: List<UserEsim>
        get() = esims
            .sortedBy { esim ->
                when (esim.status) {
                    EsimStatus.PROVISIONED -> 0
                    EsimStatus.INSTALLED -> 1
                    EsimStatus.ACTIVE -> 2
                    else -> 3
                }
            }

    val showEmptyState: Boolean
        get() = !isLoading && filteredEsims.isEmpty() && error == null

    val showContent: Boolean
        get() = !isLoading && filteredEsims.isNotEmpty() && error == null

    val showError: Boolean
        get() = !isLoading && error != null

    fun isEsimInstalling(esimId: String): Boolean =
        isInstalling && installingEsimId == esimId
}

/**
 * Installation error with user-friendly Georgian message and error type.
 */
data class InstallationError(
    val message: String,
    val type: Type
) {
    enum class Type {
        USER_CANCELLED,
        DEVICE_ERROR,
        NETWORK_ERROR,
        INVALID_CODE,
        CARRIER_ERROR,
        SYSTEM_ERROR
    }

    companion object {
        fun userCancelled() = InstallationError(
            message = "ინსტალაცია გაუქმებულია",
            type = Type.USER_CANCELLED
        )

        fun deviceNotSupported() = InstallationError(
            message = "თქვენი მოწყობილობა არ უჭერს მხარს eSIM-ს",
            type = Type.DEVICE_ERROR
        )

        fun networkError() = InstallationError(
            message = "ქსელის შეცდომა. შეამოწმეთ ინტერნეტ კავშირი",
            type = Type.NETWORK_ERROR
        )

        fun invalidCode() = InstallationError(
            message = "არასწორი აქტივაციის კოდი",
            type = Type.INVALID_CODE
        )

        fun carrierError() = InstallationError(
            message = "ოპერატორის შეცდომა. სცადეთ მოგვიანებით",
            type = Type.CARRIER_ERROR
        )

        fun systemError() = InstallationError(
            message = "სისტემური შეცდომა. სცადეთ თავიდან",
            type = Type.SYSTEM_ERROR
        )
    }
}