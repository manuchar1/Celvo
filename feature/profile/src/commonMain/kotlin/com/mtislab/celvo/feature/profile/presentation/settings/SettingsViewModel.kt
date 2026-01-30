package com.mtislab.celvo.feature.profile.presentation.settings

import AppTheme
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {


    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    val availableLanguages = listOf(
        "en" to "English",
        "ka" to "ქართული",
    )

    fun onThemeSelect(theme: AppTheme) {
        _state.update { it.copy(appTheme = theme) }
        // TODO: Save to DataStore here
    }

    fun onLanguageSelect(code: String) {
        _state.update { it.copy(currentLanguage = code) }
        // TODO: Save to DataStore here & update Locale
    }
}