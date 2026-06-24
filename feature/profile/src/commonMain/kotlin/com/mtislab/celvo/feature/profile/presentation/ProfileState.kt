package com.mtislab.celvo.feature.profile.presentation

import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import com.mtislab.core.domain.utils.DataError

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: DataError? = null,
    val deletionStatus: DeletionStatus = DeletionStatus.Idle
)

sealed interface DeletionStatus {
    /** No dialog shown. */
    data object Idle : DeletionStatus

    /** Dialog open, waiting for user confirmation. */
    data object Confirming : DeletionStatus

    /** Dialog open, network request in flight. Buttons disabled, spinner inline. */
    data object Deleting : DeletionStatus

    /** Dialog open, recoverable error shown inline. Buttons re-enabled, user may retry. */
    data object RetryableError : DeletionStatus
}
