package com.mtislab.celvo.feature.profile.presentation


import com.mtislab.core.domain.model.AppTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.profile.generated.resources.Res
import celvo.feature.profile.generated.resources.ic_avatars_user
import celvo.feature.profile.generated.resources.ic_card
import celvo.feature.profile.generated.resources.ic_doc
import celvo.feature.profile.generated.resources.ic_log_out_circle
import celvo.feature.profile.generated.resources.ic_moon
import celvo.feature.profile.generated.resources.ic_users
import celvo.feature.profile.generated.resources.ic_world
import celvo.feature.profile.generated.resources.profile_dark_mode
import celvo.feature.profile.generated.resources.profile_delete_account_dialog_cancel
import celvo.feature.profile.generated.resources.profile_delete_account_dialog_confirm
import celvo.feature.profile.generated.resources.profile_delete_account_dialog_consequence
import celvo.feature.profile.generated.resources.profile_delete_account_dialog_title
import celvo.feature.profile.generated.resources.profile_delete_account_dialog_warning
import celvo.feature.profile.generated.resources.profile_delete_account_error_generic
import celvo.feature.profile.generated.resources.profile_delete_account_trigger
import celvo.feature.profile.generated.resources.profile_log_out
import celvo.feature.profile.generated.resources.profile_logout_confirm
import celvo.feature.profile.generated.resources.profile_logout_confirmation
import celvo.feature.profile.generated.resources.profile_logout_dismiss
import celvo.feature.profile.generated.resources.theme_off
import celvo.feature.profile.generated.resources.theme_on
import celvo.feature.profile.generated.resources.theme_system_default
import com.celvo.core.designsystem.resources.Res as CoreRes
import com.celvo.core.designsystem.resources.legal_privacy_policy
import com.celvo.core.designsystem.resources.legal_terms_of_service
import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import com.mtislab.celvo.feature.profile.presentation.components.DeleteAccountDialog
import com.mtislab.celvo.feature.profile.presentation.components.ProfileHeader
import com.mtislab.celvo.feature.profile.presentation.components.ProfileMenuItem
import com.mtislab.celvo.feature.profile.presentation.settings.SettingsState
import com.mtislab.celvo.feature.profile.presentation.settings.SettingsViewModel
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.dialogs.CelvoDialog
import com.mtislab.core.designsystem.components.notifications.CelvoNotificationData
import com.mtislab.core.designsystem.components.notifications.LocalCelvoNotification
import com.mtislab.core.designsystem.legal.LegalLinks
import com.mtislab.core.designsystem.legal.rememberLegalOpener
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    viewModel: ProfileViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateToTheme: () -> Unit,
    onNavigateToLanguage: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val notificationState = LocalCelvoNotification.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowNotification -> {
                    notificationState.show(
                        CelvoNotificationData(
                            message = getString(event.messageRes),
                            type = event.type
                        )
                    )
                }
            }
        }
    }

    ProfileScreen(
        state = state,
        settingsState = settingsState,
        onAction = viewModel::onAction,
        onNavigateToTheme = onNavigateToTheme,
        onNavigateToLanguage = onNavigateToLanguage
    )
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    settingsState: SettingsState,
    onAction: (ProfileAction) -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToLanguage: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val openLegal = rememberLegalOpener()


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ProfileHeader(
            onSupportClick = { /* TODO */ }
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Content Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- User Info Card ---
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }

                    state.userProfile != null -> {
                        UserProfileCard(userProfile = state.userProfile)
                    }
                }

                // --- Menu Card (Settings) ---
                CelvoCard(
                    contentPadding = PaddingValues(0.dp)
                ) {
//                    ProfileMenuItem(
//                        icon = painterResource(Res.drawable.ic_card),
//                        title = "გადახდის მეთოდები",
//                        onClick = { /* TODO */ }
//                    )
//                    ProfileMenuItem(
//                        icon = painterResource(Res.drawable.ic_users),
//                        title = "მოიწვიე მეგობარი",
//                        onClick = { /* TODO */ }
//                    )

                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_moon),
                        title = stringResource(Res.string.profile_dark_mode),
                        isSwitch = false,
                        trailingText = when(settingsState.appTheme) {
                            AppTheme.DARK -> stringResource(Res.string.theme_on)
                            AppTheme.LIGHT -> stringResource(Res.string.theme_off)
                            AppTheme.SYSTEM -> stringResource(Res.string.theme_system_default)
                        },
                        showDivider = false,
                        onClick = onNavigateToTheme
                    )

//                    ProfileMenuItem(
//                        icon = painterResource(Res.drawable.ic_world),
//                        title = "ენა",
//                        trailingText = when(settingsState.currentLanguage) {
//                            "ka" -> "ქართული"
//                            "en" -> "English"
//                            else -> settingsState.currentLanguage
//                        },
//                        onClick = onNavigateToLanguage
//                    )
                }

                // --- Legal Section ---
                // Standard, discoverable place for legal docs. Both open in an in-app
                // browser (Chrome Custom Tabs / SFSafariViewController) via openLegal.
                CelvoCard(
                    contentPadding = PaddingValues(0.dp)
                ) {
                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_doc),
                        title = stringResource(CoreRes.string.legal_terms_of_service),
                        onClick = { openLegal(LegalLinks.TERMS_OF_SERVICE) }
                    )

                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_doc),
                        title = stringResource(CoreRes.string.legal_privacy_policy),
                        showDivider = false,
                        onClick = { openLegal(LegalLinks.PRIVACY_POLICY) }
                    )
                }

                // --- Logout Card ---
                CelvoCard(
                    onClick = { showLogoutDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_log_out_circle),
                        title = stringResource(Res.string.profile_log_out),
                        showDivider = false,
                        textColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            // --- Delete Account trigger ---
            // Industry-standard placement: subtle text button at the very bottom of
            // Profile. Discoverable but never inviting. Hit target is widened via
            // minimumInteractiveComponentSize to satisfy a11y while keeping the
            // visible text at caption size.
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onAction(ProfileAction.OnDeleteAccountClick) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = stringResource(Res.string.profile_delete_account_trigger),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        CelvoDialog(
            title = stringResource(Res.string.profile_log_out),
            description = stringResource(Res.string.profile_logout_confirmation),
            icon = painterResource(Res.drawable.ic_log_out_circle),
            confirmText = stringResource(Res.string.profile_logout_confirm),
            dismissText = stringResource(Res.string.profile_logout_dismiss),
            onConfirm = {
                showLogoutDialog = false
                onAction(ProfileAction.OnLogoutClick)
            },
            onDismiss = { showLogoutDialog = false },
            confirmContainerColor = Color(0xFFE59CA8),
            confirmContentColor = Color.Black
        )
    }

    if (state.deletionStatus != DeletionStatus.Idle) {
        val inlineError = if (state.deletionStatus == DeletionStatus.RetryableError) {
            stringResource(Res.string.profile_delete_account_error_generic)
        } else null

        DeleteAccountDialog(
            title = stringResource(Res.string.profile_delete_account_dialog_title),
            warning = stringResource(Res.string.profile_delete_account_dialog_warning),
            consequence = stringResource(Res.string.profile_delete_account_dialog_consequence),
            cancelText = stringResource(Res.string.profile_delete_account_dialog_cancel),
            confirmText = stringResource(Res.string.profile_delete_account_dialog_confirm),
            inlineErrorText = inlineError,
            isLoading = state.deletionStatus == DeletionStatus.Deleting,
            onCancel = { onAction(ProfileAction.OnCancelDelete) },
            onConfirm = { onAction(ProfileAction.OnConfirmDelete) }
        )
    }
}

@Composable
private fun UserProfileCard(
    userProfile: UserProfile
) {
    // ეს ნაწილი უცვლელია, როგორც შენს ფაილში იყო
    CelvoCard(
        onClick = null,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_avatars_user),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userProfile.fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.4.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 19.6.sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
