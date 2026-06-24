package com.mtislab.core.designsystem.components.placeholders

import CelvoPlaceholder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.celvo.core.designsystem.resources.Res
import com.celvo.core.designsystem.resources.mascot_fox_error
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.isConnectivityError

/**
 * Full-screen error placeholder that picks its presentation from the error type.
 *
 * A connectivity failure renders [CelvoNoInternetPlaceholder] — the caller's
 * ViewModel retries the load once the device is back online, so it disappears
 * on its own. Every other failure (server / API / serialization) renders the
 * generic [CelvoPlaceholder] with the fox-error mascot and a manual retry.
 *
 * Routing the no-internet vs. server distinction through this single component
 * keeps it consistent across every screen.
 */
@Composable
fun CelvoErrorState(
    error: DataError,
    onRetry: () -> Unit,
    serverErrorTitle: String,
    serverErrorMessage: String,
    serverErrorActionLabel: String,
    modifier: Modifier = Modifier,
    onViewInstructionsClick: (() -> Unit)? = null,
) {
    if (error.isConnectivityError) {
        CelvoNoInternetPlaceholder(
            onRetryClick = onRetry,
            modifier = modifier,
            onViewInstructionsClick = onViewInstructionsClick,
        )
    } else {
        CelvoPlaceholder(
            icon = Res.drawable.mascot_fox_error,
            title = serverErrorTitle,
            message = serverErrorMessage,
            actionLabel = serverErrorActionLabel,
            onActionClick = onRetry,
            modifier = modifier,
        )
    }
}
