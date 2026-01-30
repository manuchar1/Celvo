package com.mtislab.celvo.feature.profile.presentation.settings

import AppTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mtislab.celvo.feature.profile.presentation.settings.components.SettingsOptionItem
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.headers.CelvoDetailHeader
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
            CelvoDetailHeader(
                title = "მუქი რეჟიმი",
                onBackClick = onBackClick
            )
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
                // 1. ჩართულია
                SettingsOptionItem(
                    text = "ჩართულია",
                    isSelected = state.appTheme == AppTheme.DARK,
                    onClick = { viewModel.onThemeSelect(AppTheme.DARK) }
                )

                // 2. გამორთულია
                SettingsOptionItem(
                    text = "გამორთულია",
                    isSelected = state.appTheme == AppTheme.LIGHT,
                    onClick = { viewModel.onThemeSelect(AppTheme.LIGHT) }
                )

                // 3. ავტომატური
                SettingsOptionItem(
                    text = "ავტომატური",
                    isSelected = state.appTheme == AppTheme.SYSTEM,
                    onClick = { viewModel.onThemeSelect(AppTheme.SYSTEM) },
                    showDivider = false
                )
            }
        }
    }
}