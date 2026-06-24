package com.mtislab.celvo.feature.profile.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import celvo.feature.profile.generated.resources.Res
import celvo.feature.profile.generated.resources.language_name_english
import celvo.feature.profile.generated.resources.language_name_georgian
import celvo.feature.profile.generated.resources.language_screen_title
import com.mtislab.celvo.feature.profile.presentation.settings.components.SettingsOptionItem
import com.mtislab.core.designsystem.components.cards.CelvoCard
import com.mtislab.core.designsystem.components.headers.CelvoDetailHeader
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguageScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CelvoDetailHeader(
                title = stringResource(Res.string.language_screen_title),
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            CelvoCard(contentPadding = PaddingValues(0.dp)) {
                val languages = viewModel.availableLanguages
                languages.forEachIndexed { index, code ->
                    SettingsOptionItem(
                        text = languageDisplayName(code),
                        isSelected = state.currentLanguage == code,
                        onClick = { viewModel.onLanguageSelect(code) },
                        showDivider = index != languages.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun languageDisplayName(code: String): String = when (code) {
    "en" -> stringResource(Res.string.language_name_english)
    "ka" -> stringResource(Res.string.language_name_georgian)
    else -> code
}