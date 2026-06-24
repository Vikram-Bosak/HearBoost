package com.hearboost.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hearboost.audio.AudioEngine
import com.hearboost.audio.NoiseReductionEngine
import com.hearboost.audio.VolumeBooster
import com.hearboost.bluetooth.HeadphoneManager
import com.hearboost.service.AudioForegroundService
import com.hearboost.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val audioEngine: AudioEngine,
    private val volumeBooster: VolumeBooster,
    private val noiseEngine: NoiseReductionEngine,
    private val headphoneManager: HeadphoneManager,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    // UI State
    data class UiState(
        val isListening: Boolean = false,
        val volumePercent: Int = 70,
        val noiseReductionEnabled: Boolean = true,
        val noiseReductionLevel: Int = 1,
        val headphoneConnected: Boolean = false,
        val headphoneName: String = "Not connected",
        val headphoneType: HeadphoneManager.HeadphoneType = HeadphoneManager.HeadphoneType.NONE,
        val batteryPercent: Int = 80,
        val audioLevel: Float = 0f,
        val isFullScreenMode: Boolean = false,
        val activeProfile: String = "Default"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Start observing headphone changes
        headphoneManager.startListening()

        viewModelScope.launch {
            headphoneManager.headphoneState.collect { state ->
                _uiState.update {
                    it.copy(
                        headphoneConnected = state.isConnected,
                        headphoneName = state.name,
                        headphoneType = state.type
                    )
                }
            }
        }

        // Observe settings
        viewModelScope.launch {
            settingsManager.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        volumePercent = settings.volumePercent,
                        noiseReductionEnabled = settings.noiseReductionEnabled,
                        noiseReductionLevel = settings.noiseReductionLevel,
                        activeProfile = settings.activeProfile
                    )
                }
                volumeBooster.setVolumePercent(settings.volumePercent)
            }
        }

        // Audio level callback for waveform visualization
        audioEngine.onAudioLevel = { level ->
            _uiState.update { it.copy(audioLevel = level) }
        }
    }

    fun toggleListening() {
        val app = getApplication<Application>()
        if (_uiState.value.isListening) {
            // Stop
            val intent = Intent(app, AudioForegroundService::class.java).apply {
                action = AudioForegroundService.ACTION_STOP
            }
            app.startForegroundService(intent)
            _uiState.update { it.copy(isListening = false, audioLevel = 0f) }
        } else {
            // Start
            audioEngine.setGain(volumeBooster.gain)
            audioEngine.setNoiseReduction(
                noiseEngine.isEnabled(),
                noiseEngine.level.value
            )
            val intent = Intent(app, AudioForegroundService::class.java).apply {
                action = AudioForegroundService.ACTION_START
            }
            app.startForegroundService(intent)
            _uiState.update { it.copy(isListening = true) }
        }
    }

    fun updateVolume(percent: Int) {
        _uiState.update { it.copy(volumePercent = percent) }
        volumeBooster.setVolumePercent(percent)
        audioEngine.setGain(volumeBooster.gain)

        // Update service
        updateServiceSettings()

        // Persist
        viewModelScope.launch { settingsManager.updateVolume(percent) }
    }

    fun toggleNoiseReduction() {
        val newEnabled = !_uiState.value.noiseReductionEnabled
        _uiState.update { it.copy(noiseReductionEnabled = newEnabled) }
        noiseEngine.setLevel(if (newEnabled) _uiState.value.noiseReductionLevel else 0)
        audioEngine.setNoiseReduction(newEnabled, _uiState.value.noiseReductionLevel)
        updateServiceSettings()
        viewModelScope.launch { settingsManager.updateNoiseReduction(newEnabled, _uiState.value.noiseReductionLevel) }
    }

    fun setNoiseReductionLevel(level: Int) {
        _uiState.update { it.copy(noiseReductionLevel = level, noiseReductionEnabled = level > 0) }
        noiseEngine.setLevel(level)
        audioEngine.setNoiseReduction(level > 0, level)
        updateServiceSettings()
        viewModelScope.launch { settingsManager.updateNoiseReduction(level > 0, level) }
    }

    fun setFullScreenMode(enabled: Boolean) {
        _uiState.update { it.copy(isFullScreenMode = enabled) }
    }

    private fun updateServiceSettings() {
        val app = getApplication<Application>()
        val intent = Intent(app, AudioForegroundService::class.java).apply {
            action = AudioForegroundService.ACTION_UPDATE_GAIN
            putExtra(AudioForegroundService.EXTRA_GAIN, volumeBooster.gain)
            putExtra(AudioForegroundService.EXTRA_NOISE_ENABLED, noiseEngine.isEnabled())
            putExtra(AudioForegroundService.EXTRA_NOISE_LEVEL, noiseEngine.level.value)
        }
        app.startForegroundService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        headphoneManager.stopListening()
    }
}
