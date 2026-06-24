package com.mtislab.core.data.session

import com.mtislab.core.domain.model.AppTheme
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemePreferences(
    private val dataStore: DataStore<Preferences>
) {

    private companion object {
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
    }

    val themeFlow: Flow<AppTheme> = dataStore.data.map { preferences ->
        when (preferences[KEY_APP_THEME]) {
            "LIGHT" -> AppTheme.LIGHT
            "DARK" -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[KEY_APP_THEME] = theme.name
        }
    }
}
