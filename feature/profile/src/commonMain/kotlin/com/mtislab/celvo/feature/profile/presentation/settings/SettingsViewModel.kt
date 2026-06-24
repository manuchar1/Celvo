package com.mtislab.celvo.feature.profile.presentation.settings

import com.mtislab.core.domain.model.AppTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtislab.core.data.session.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    val availableLanguages = listOf("en", "ka")

    init {
        viewModelScope.launch {
            themePreferences.themeFlow.collect { theme ->
                _state.update { it.copy(appTheme = theme) }
            }
        }
    }

    fun onThemeSelect(theme: AppTheme) {
        _state.update { it.copy(appTheme = theme) }
        viewModelScope.launch {
            themePreferences.saveTheme(theme)
        }
    }

    fun onLanguageSelect(code: String) {
        _state.update { it.copy(currentLanguage = code) }
        // TODO: Save to DataStore here & update Locale
    }
}
