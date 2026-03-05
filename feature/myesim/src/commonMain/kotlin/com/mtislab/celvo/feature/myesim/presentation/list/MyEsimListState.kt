package com.mtislab.celvo.feature.myesim.presentation.list

import com.mtislab.celvo.feature.myesim.domain.model.UserEsim
import com.mtislab.core.domain.utils.DataError

data class MyEsimListState(
    val isLoading: Boolean = true,
    val esims: List<UserEsim> = emptyList(),
    val error: DataError? = null,
    val isInstalling: Boolean = false,
    val installingEsimId: String? = null,
    val installationError: String? = null
) {
    val showEmptyState: Boolean
        get() = !isLoading && esims.isEmpty() && error == null

    val showError: Boolean
        get() = !isLoading && error != null
}