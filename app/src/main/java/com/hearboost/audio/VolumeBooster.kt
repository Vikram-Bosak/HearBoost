package com.hearboost.audio

import javax.inject.Inject
import javax.inject.Singleton

/**
 * VolumeBooster — manages gain levels with safety limits.
 * WHO recommends max 85dB for extended use.
 */
@Singleton
class VolumeBooster @Inject constructor() {

    companion object {
        const val MIN_GAIN = 0.1f
        const val MAX_GAIN = 20.0f
        const val DEFAULT_GAIN = 3.0f
        const val SAFE_GAIN_LIMIT = 8.0f
        const val MAX_DB_SAFE = 85
    }

    private var _gain: Float = DEFAULT_GAIN
    val gain: Float get() = _gain

    private var _volumePercent: Int = 70
    val volumePercent: Int get() = _volumePercent

    private var _safetyLimitEnabled: Boolean = true
    val safetyLimitEnabled: Boolean get() = _safetyLimitEnabled

    private var _maxVolumeLimit: Int = 85
    val maxVolumeLimit: Int get() = _maxVolumeLimit

    /**
     * Set volume as percentage (0-100).
     * Maps to gain factor with logarithmic curve for natural feel.
     */
    fun setVolumePercent(percent: Int) {
        _volumePercent = percent.coerceIn(0, 100)
        _gain = percentToGain(_volumePercent)
    }

    /**
     * Direct gain setting.
     */
    fun setGain(factor: Float) {
        val effectiveGain = if (_safetyLimitEnabled) {
            factor.coerceAtMost(SAFE_GAIN_LIMIT)
        } else {
            factor.coerceIn(MIN_GAIN, MAX_GAIN)
        }
        _gain = effectiveGain
        _volumePercent = gainToPercent(effectiveGain)
    }

    fun setSafetyLimit(enabled: Boolean) {
        _safetyLimitEnabled = enabled
        if (enabled && _gain > SAFE_GAIN_LIMIT) {
            _gain = SAFE_GAIN_LIMIT
            _volumePercent = gainToPercent(_gain)
        }
    }

    fun setMaxVolumeLimit(percent: Int) {
        _maxVolumeLimit = percent.coerceIn(0, 100)
        val maxGain = percentToGain(_maxVolumeLimit)
        if (_gain > maxGain) {
            _gain = maxGain
            _volumePercent = _maxVolumeLimit
        }
    }

    fun resetToDefaults() {
        _gain = DEFAULT_GAIN
        _volumePercent = 70
        _safetyLimitEnabled = true
        _maxVolumeLimit = 85
    }

    /**
     * Convert percentage to gain using logarithmic curve.
     * This gives more fine-grained control at lower volumes.
     */
    private fun percentToGain(percent: Int): Float {
        if (percent <= 0) return MIN_GAIN
        val normalized = percent / 100.0f
        return MIN_GAIN + (MAX_GAIN - MIN_GAIN) * normalized * normalized
    }

    /**
     * Convert gain back to percentage.
     */
    private fun gainToPercent(gain: Float): Int {
        val normalized = (gain - MIN_GAIN) / (MAX_GAIN - MIN_GAIN)
        return (kotlin.math.sqrt(normalized.toDouble()) * 100).toInt().coerceIn(0, 100)
    }
}
