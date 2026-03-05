package com.mtislab.celvo.feature.myesim.presentation.details

import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.celvo.feature.myesim.domain.model.EsimBundleInfo
import com.mtislab.core.domain.utils.DataError

data class EsimDetailsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false, // Swipe-to-refresh-ისთვის
    val esim: UserEsim? = null,
    val bundleInfo: EsimBundleInfo? = null, // ბექიდან წამოსული პაკეტები
    val error: DataError? = null,
    val bundlesError: DataError? = null, // თუ პაკეტების წამოღებისას დაერორდა
    val showQrCodeSheet: Boolean = false,
    val showOperatorsSheet: Boolean = false,
    val isEditingLabel: Boolean = false,
    val editLabelText: String = "",
    val isUpdatingLabel: Boolean = false
) {
    val statusDisplay: String
        get() = esim?.statusDisplayName ?: ""

    val showContent: Boolean
        get() = !isLoading && esim != null && error == null

    val showError: Boolean
        get() = !isLoading && error != null


     enum class EsimDetailTab(val title: String) {
        CURRENT("Current"),
        HISTORY("Archived")
    }
}