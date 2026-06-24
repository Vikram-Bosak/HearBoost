package com.hearboost.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hearboost_settings")

/**
 * SettingsManager — persists user preferences via DataStore.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys
    private object Keys {
        val VOLUME_PERCENT = intPreferencesKey("volume_percent")
        val NOISE_REDUCTION_ENABLED = booleanPreferencesKey("noise_reduction_enabled")
        val NOISE_REDUCTION_LEVEL = intPreferencesKey("noise_reduction_level")
        val SAFETY_LIMIT_ENABLED = booleanPreferencesKey("safety_limit_enabled")
        val MAX_VOLUME_LIMIT = intPreferencesKey("max_volume_limit")
        val LARGE_TEXT_MODE = booleanPreferencesKey("large_text_mode")
        val EXTRA_LARGE_BUTTONS = booleanPreferencesKey("extra_large_buttons")
        val HIGH_CONTRAST_MODE = booleanPreferencesKey("high_contrast_mode")
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        val LATENCY_MODE = intPreferencesKey("latency_mode") // 0=low delay, 1=high quality
        val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
        val LANGUAGE = stringPreferencesKey("language")
    }

    data class AppSettings(
        val volumePercent: Int = 70,
        val noiseReductionEnabled: Boolean = true,
        val noiseReductionLevel: Int = 1,
        val safetyLimitEnabled: Boolean = true,
        val maxVolumeLimit: Int = 85,
        val largeTextMode: Boolean = true,
        val extraLargeButtons: Boolean = false,
        val highContrastMode: Boolean = false,
        val activeProfile: String = "Default",
        val latencyMode: Int = 0,
        val firstLaunchDone: Boolean = false,
        val language: String = "en"
    )

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            volumePercent = prefs[Keys.VOLUME_PERCENT] ?: 70,
            noiseReductionEnabled = prefs[Keys.NOISE_REDUCTION_ENABLED] ?: true,
            noiseReductionLevel = prefs[Keys.NOISE_REDUCTION_LEVEL] ?: 1,
            safetyLimitEnabled = prefs[Keys.SAFETY_LIMIT_ENABLED] ?: true,
            maxVolumeLimit = prefs[Keys.MAX_VOLUME_LIMIT] ?: 85,
            largeTextMode = prefs[Keys.LARGE_TEXT_MODE] ?: true,
            extraLargeButtons = prefs[Keys.EXTRA_LARGE_BUTTONS] ?: false,
            highContrastMode = prefs[Keys.HIGH_CONTRAST_MODE] ?: false,
            activeProfile = prefs[Keys.ACTIVE_PROFILE] ?: "Default",
            latencyMode = prefs[Keys.LATENCY_MODE] ?: 0,
            firstLaunchDone = prefs[Keys.FIRST_LAUNCH_DONE] ?: false,
            language = prefs[Keys.LANGUAGE] ?: "en"
        )
    }

    suspend fun updateVolume(percent: Int) {
        context.dataStore.edit { it[Keys.VOLUME_PERCENT] = percent }
    }

    suspend fun updateNoiseReduction(enabled: Boolean, level: Int = 1) {
        context.dataStore.edit {
            it[Keys.NOISE_REDUCTION_ENABLED] = enabled
            it[Keys.NOISE_REDUCTION_LEVEL] = level
        }
    }

    suspend fun updateSafetyLimit(enabled: Boolean, maxVolume: Int = 85) {
        context.dataStore.edit {
            it[Keys.SAFETY_LIMIT_ENABLED] = enabled
            it[Keys.MAX_VOLUME_LIMIT] = maxVolume
        }
    }

    suspend fun updateAccessibility(largeText: Boolean, extraLarge: Boolean, highContrast: Boolean) {
        context.dataStore.edit {
            it[Keys.LARGE_TEXT_MODE] = largeText
            it[Keys.EXTRA_LARGE_BUTTONS] = extraLarge
            it[Keys.HIGH_CONTRAST_MODE] = highContrast
        }
    }

    suspend fun updateLatencyMode(mode: Int) {
        context.dataStore.edit { it[Keys.LATENCY_MODE] = mode }
    }

    suspend fun setFirstLaunchDone() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH_DONE] = true }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
