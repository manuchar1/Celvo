package com.mtislab.celvo.feature.profile.presentation


import AppTheme
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
import androidx.compose.runtime.Composable
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
import com.mtislab.celvo.feature.profile.domain.model.UserProfile
import com.mtislab.celvo.feature.profile.presentation.components.ProfileHeader
import com.mtislab.celvo.feature.profile.presentation.components.ProfileMenuItem
import com.mtislab.celvo.feature.profile.presentation.settings.SettingsState
import com.mtislab.celvo.feature.profile.presentation.settings.SettingsViewModel
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.dialogs.CelvoDialog
import com.mtislab.core.designsystem.theme.extended
import org.jetbrains.compose.resources.painterResource
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
                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_card),
                        title = "გადახდის მეთოდები",
                        onClick = { /* TODO */ }
                    )
                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_users),
                        title = "მოიწვიე მეგობარი",
                        onClick = { /* TODO */ }
                    )

                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_moon),
                        title = "მუქი რეჟიმი",
                        isSwitch = false,
                        // ვაჩვენებთ ტექსტს მიმდინარე თემის მიხედვით
                        trailingText = when(settingsState.appTheme) {
                            AppTheme.DARK -> "ჩართულია"
                            AppTheme.LIGHT -> "გამორთულია"
                            AppTheme.SYSTEM -> "ავტომატური"
                        },
                        onClick = onNavigateToTheme
                    )

                    // ✅ ენა (განახლებული - უკავშირდება ViewModel-ს)
                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_world),
                        title = "ენა",
                        trailingText = when(settingsState.currentLanguage) {
                            "ka" -> "ქართული"
                            "en" -> "English"
                            else -> settingsState.currentLanguage
                        },
                        onClick = onNavigateToLanguage // 🚀 ნავიგაცია
                    )

                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_doc),
                        title = "წესები და პირობები",
                        showDivider = false,
                        onClick = { /* TODO */ }
                    )
                }

                // --- Logout Card ---
                CelvoCard(
                    onClick = { showLogoutDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    ProfileMenuItem(
                        icon = painterResource(Res.drawable.ic_log_out_circle),
                        title = "გამოსვლა",
                        showDivider = false,
                        textColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        CelvoDialog(
            title = "გამოსვლა",
            description = "ნამდვილად გსურთ აპლიკაციიდან\nგამოსვლა?",
            icon = painterResource(Res.drawable.ic_log_out_circle),
            confirmText = "დიახ, მსურს",
            dismissText = "ახლა არა",
            onConfirm = {
                showLogoutDialog = false
                onAction(ProfileAction.OnLogoutClick)
            },
            onDismiss = { showLogoutDialog = false },
            confirmContainerColor = Color(0xFFE59CA8),
            confirmContentColor = Color.Black
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