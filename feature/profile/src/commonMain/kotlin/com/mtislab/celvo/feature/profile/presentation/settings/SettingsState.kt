package com.mtislab.celvo.feature.profile.presentation.settings

import AppTheme

data class SettingsState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: String = "ka"
)