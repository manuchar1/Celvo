package com.mtislab.celvo.feature.store.presentation.verification

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.store.generated.resources.Res
import celvo.feature.store.generated.resources.mascot_fox_success
import celvo.feature.store.generated.resources.verification_back_to_home
import celvo.feature.store.generated.resources.verification_error_subtitle
import celvo.feature.store.generated.resources.verification_install_esim
import celvo.feature.store.generated.resources.verification_install_failed
import celvo.feature.store.generated.resources.verification_loading_subtitle
import celvo.feature.store.generated.resources.verification_loading_title
import celvo.feature.store.generated.resources.verification_my_esims
import celvo.feature.store.generated.resources.verification_retry
import celvo.feature.store.generated.resources.verification_success_new_esim
import celvo.feature.store.generated.resources.verification_success_topup
import celvo.feature.store.generated.resources.verification_topup_no_install
import com.celvo.core.designsystem.resources.Res as CoreRes
import com.celvo.core.designsystem.resources.mascot_fox_error
import com.mtislab.core.designsystem.components.buttons.CelvoButton
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationData
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationType
import com.mtislab.core.designsystem.components.notifications.LocalCelvoNotification
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentVerificationScreenRoot(
    viewModel: PaymentVerificationViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToMyEsims: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val notificationState = LocalCelvoNotification.current
    val installFailedMessage = stringResource(Res.string.verification_install_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentVerificationEvent.NavigateToHome -> onNavigateToHome()
                is PaymentVerificationEvent.NavigateToMyEsims -> onNavigateToMyEsims()
                is PaymentVerificationEvent.OpenEsimInstallUrl -> {
                    try {
                        uriHandler.openUri(event.url)
                    } catch (_: Exception) {
                        notificationState.show(
                            CelvoNotificationData(
                                message = installFailedMessage,
                                type = CelvoNotificationType.Error
                            )
                        )
                    }
                }
            }
        }
    }

    PaymentVerificationScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun PaymentVerificationScreen(
    state: PaymentVerificationState,
    onAction: (PaymentVerificationAction) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(
                            initialScale = 0.92f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )) togetherWith
                        (fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                                scaleOut(targetScale = 0.92f))
            },
            contentKey = { it::class }
        ) { currentState ->
            when (currentState) {
                is PaymentVerificationState.Loading -> {
                    LoadingContent()
                }

                is PaymentVerificationState.SuccessNewEsim -> {
                    SuccessNewEsimContent(
                        state = currentState,
                        onInstallClick = { onAction(PaymentVerificationAction.InstallEsimClicked) },
                        onMyEsimsClick = { onAction(PaymentVerificationAction.GoToMyEsimsClicked) }
                    )
                }

                is PaymentVerificationState.SuccessTopUp -> {
                    SuccessTopUpContent(
                        state = currentState,
                        onHomeClick = { onAction(PaymentVerificationAction.GoToDashboardClicked) }
                    )
                }

                is PaymentVerificationState.Error -> {
                    ErrorContent(
                        state = currentState,
                        onRetryClick = { onAction(PaymentVerificationAction.RetryClicked) },
                        onHomeClick = { onAction(PaymentVerificationAction.GoToDashboardClicked) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.verification_loading_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.verification_loading_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success — New eSIM
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessNewEsimContent(
    state: PaymentVerificationState.SuccessNewEsim,
    onInstallClick: () -> Unit,
    onMyEsimsClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.mascot_fox_success),
            contentDescription = null,
            modifier = Modifier.size(260.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            Text(
                text = stringResource(Res.string.verification_success_new_esim),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.bundleName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        CelvoButton(
            text = stringResource(Res.string.verification_install_esim),
            onClick = onInstallClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        CelvoButton(
            text = stringResource(Res.string.verification_my_esims),
            onClick = onMyEsimsClick,
            modifier = Modifier.fillMaxWidth(),
            outlined = true,
            contentColor = MaterialTheme.colorScheme.extended.textSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success — Top-Up
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessTopUpContent(
    state: PaymentVerificationState.SuccessTopUp,
    onHomeClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(Res.drawable.mascot_fox_success),
            contentDescription = null,
            modifier = Modifier.size(260.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            Text(
                text = stringResource(Res.string.verification_success_topup),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.bundleName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.verification_topup_no_install),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        CelvoButton(
            text = stringResource(Res.string.verification_back_to_home),
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(
    state: PaymentVerificationState.Error,
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(CoreRes.drawable.mascot_fox_error),
            contentDescription = null,
            modifier = Modifier.size(260.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            Text(
                text = state.message.asString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.verification_error_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        CelvoButton(
            text = stringResource(Res.string.verification_retry),
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        CelvoButton(
            text = stringResource(Res.string.verification_back_to_home),
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(),
            outlined = true,
            contentColor = MaterialTheme.colorScheme.extended.textSecondary
        )
    }
}
