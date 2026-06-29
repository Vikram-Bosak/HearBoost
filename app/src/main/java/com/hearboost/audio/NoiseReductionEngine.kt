package com.hearboost.audio

import javax.inject.Inject
import javax.inject.Singleton

/**
 * NoiseReductionEngine — multi-level noise reduction for speech clarity.
 * 
 * Level 0: Off
 * Level 1 (Low): Basic noise gate — cuts silence/below-threshold noise
 * Level 2 (High): Aggressive noise gate + spectral tilt to suppress background noise
 */
@Singleton
class NoiseReductionEngine @Inject constructor() {

    enum class Level(val value: Int) {
        OFF(0), LOW(1), HIGH(2);

        companion object {
            fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: LOW
        }
    }

    private var _level: Level = Level.LOW
    val level: Level get() = _level

    // Noise gate thresholds (in 16-bit PCM amplitude)
    private val gateThresholds = mapOf(
        Level.OFF to 0,
        Level.LOW to 350,
        Level.HIGH to 700
    )

    // Attenuation factor below threshold
    private val attenuationMap = mapOf(
        Level.OFF to 1.0f,
        Level.LOW to 0.08f,
        Level.HIGH to 0.02f
    )

    fun setLevel(level: Level) {
        _level = level
    }

    fun setLevel(value: Int) {
        _level = Level.fromValue(value)
    }

    fun isEnabled(): Boolean = _level != Level.OFF

    /**
     * Get the current noise gate threshold for the active level.
     */
    fun getGateThreshold(): Int = gateThresholds[_level] ?: 0

    /**
     * Get the attenuation factor for samples below the gate.
     */
    fun getAttenuation(): Float = attenuationMap[_level] ?: 1.0f

    /**
     * Apply noise reduction to a PCM buffer in-place.
     */
    fun apply(buffer: ShortArray, length: Int) {
        if (_level == Level.OFF) return

        val threshold = getGateThreshold()
        val attenuation = getAttenuation()

        when (_level) {
            Level.LOW -> applyLowReduction(buffer, length, threshold, attenuation)
            Level.HIGH -> applyHighReduction(buffer, length, threshold, attenuation)
            Level.OFF -> return
        }
    }

    private fun applyLowReduction(
        buffer: ShortArray, length: Int,
        threshold: Int, attenuation: Float
    ) {
        for (i in 0 until length) {
            val absSample = kotlin.math.abs(buffer[i].toInt())
            if (absSample < threshold) {
                // Smooth attenuation instead of hard cut
                buffer[i] = (buffer[i] * 0.4f).toInt().toShort()
            }
        }
    }

    private fun applyHighReduction(
        buffer: ShortArray, length: Int,
        threshold: Int, attenuation: Float
    ) {
        // Aggressive gate with smooth transition
        val hysteresis = threshold / 2

        var wasBelow = false
        for (i in 0 until length) {
            val absSample = kotlin.math.abs(buffer[i].toInt())
            val isBelow = absSample < threshold

            val factor = when {
                isBelow -> attenuation
                wasBelow -> {
                    // Smooth transition: ramp up when crossing threshold
                    val ratio = ((absSample - hysteresis).toFloat() / (threshold - hysteresis))
                        .coerceIn(attenuation, 1.0f)
                    ratio
                }
                else -> 1.0f
            }

            buffer[i] = (buffer[i] * factor).toInt().toShort()
            wasBelow = isBelow
        }
    }
}
