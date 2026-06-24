package com.mtislab.celvo.feature.profile.presentation.settings

import com.mtislab.core.domain.model.AppTheme

data class SettingsState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: String = "ka"
)