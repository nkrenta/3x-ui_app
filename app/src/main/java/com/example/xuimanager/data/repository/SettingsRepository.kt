package com.example.xuimanager.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val ALERT_CPU_THRESHOLD = floatPreferencesKey("alert_cpu_threshold")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val SAVED_CONNECTIONS = stringPreferencesKey("saved_connections")
    }

    val fontSizeScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE_SCALE] ?: 1.0f
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE] ?: "Русский"
    }

    val alertCpuThreshold: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[ALERT_CPU_THRESHOLD] ?: 85f
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED] ?: false
    }

    val pinCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PIN_CODE] ?: ""
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PIN_ENABLED] ?: false
    }

    val savedConnectionsJson: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SAVED_CONNECTIONS] ?: ""
    }

    suspend fun setFontSizeScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE_SCALE] = scale
        }
    }

    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun setAlertCpuThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[ALERT_CPU_THRESHOLD] = threshold
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setPinCode(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_CODE] = pin
        }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PIN_ENABLED] = enabled
        }
    }

    suspend fun setSavedConnectionsJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[SAVED_CONNECTIONS] = json
        }
    }

    val APP_FONT_FAMILY = stringPreferencesKey("app_font_family")

    val appFontFamily: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_FONT_FAMILY] ?: "System"
    }

    suspend fun setAppFontFamily(font: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_FONT_FAMILY] = font
        }
    }
}