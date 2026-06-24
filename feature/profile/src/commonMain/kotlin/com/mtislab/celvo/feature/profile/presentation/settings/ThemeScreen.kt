package com.mtislab.celvo.feature.profile.presentation.settings

import com.mtislab.core.domain.model.AppTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.profile.generated.resources.Res
import celvo.feature.profile.generated.resources.profile_dark_mode
import celvo.feature.profile.generated.resources.theme_on
import celvo.feature.profile.generated.resources.theme_off
import celvo.feature.profile.generated.resources.theme_system_default
import com.mtislab.celvo.feature.profile.presentation.settings.components.SettingsOptionItem
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.headers.CelvoDetailHeader
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ThemeScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                Spacer(modifier = Modifier.statusBarsPadding())
                CelvoDetailHeader(
                    title = stringResource(Res.string.profile_dark_mode),
                    onBackClick = onBackClick
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            CelvoCard(
                contentPadding = PaddingValues(0.dp),
            ) {
                SettingsOptionItem(
                    text = stringResource(Res.string.theme_on),
                    isSelected = state.appTheme == AppTheme.DARK,
                    onClick = { viewModel.onThemeSelect(AppTheme.DARK) }
                )

                SettingsOptionItem(
                    text = stringResource(Res.string.theme_off),
                    isSelected = state.appTheme == AppTheme.LIGHT,
                    onClick = { viewModel.onThemeSelect(AppTheme.LIGHT) }
                )

                SettingsOptionItem(
                    text = stringResource(Res.string.theme_system_default),
                    isSelected = state.appTheme == AppTheme.SYSTEM,
                    onClick = { viewModel.onThemeSelect(AppTheme.SYSTEM) },
                    showDivider = false
                )
            }
        }
    }
}