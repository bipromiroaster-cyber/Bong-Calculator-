package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.AngleUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bong_calc_prefs")

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val isScientificMode: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val hapticFeedbackEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IS_SCIENTIFIC_MODE = booleanPreferencesKey("is_scientific_mode")
        val ANGLE_UNIT = stringPreferencesKey("angle_unit")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val angleStr = prefs[Keys.ANGLE_UNIT] ?: AngleUnit.DEG.name
        val angleUnit = runCatching { AngleUnit.valueOf(angleStr) }.getOrDefault(AngleUnit.DEG)
        UserPreferences(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            isScientificMode = prefs[Keys.IS_SCIENTIFIC_MODE] ?: false,
            angleUnit = angleUnit,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true
        )
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_THEME] = isDark }
    }

    suspend fun setScientificMode(isScientific: Boolean) {
        context.dataStore.edit { it[Keys.IS_SCIENTIFIC_MODE] = isScientific }
    }

    suspend fun setAngleUnit(angleUnit: AngleUnit) {
        context.dataStore.edit { it[Keys.ANGLE_UNIT] = angleUnit.name }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }
}
