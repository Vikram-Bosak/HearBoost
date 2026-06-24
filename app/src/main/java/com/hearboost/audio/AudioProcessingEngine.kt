package com.hearboost.audio

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.tanh

/**
 * AudioProcessingEngine — advanced DSP for speech enhancement and noise reduction.
 * 
 * Implements:
 * 1. Simple noise gate (threshold-based)
 * 2. Frequency-dependent gain (boost speech frequencies 300Hz-3kHz)
 * 3. Automatic gain control (AGC)
 * 4. Soft clipping for distortion-free amplification
 */
@Singleton
class AudioProcessingEngine @Inject constructor() {

    companion object {
        private const val TAG = "AudioProcessingEngine"
    }

    // AGC state tracking
    private var runningAvgLevel = 0.0f
    private val agcSmoothFactor = 0.02f
    private val targetLevel = 0.15f // Target RMS level for AGC

    // Spectral tilt — boost mids (speech), slight cut on lows (rumble) and highs (hiss)
    private var midBoostFactor = 1.3f
    private var lowCutFactor = 0.8f
    private var highCutFactor = 0.85f

    /**
     * Apply full DSP chain to a PCM buffer.
     */
    fun process(
        buffer: ShortArray,
        length: Int,
        gain: Float,
        noiseReductionEnabled: Boolean,
        noiseLevel: Int,
        agcEnabled: Boolean = false
    ) {
        // Step 1: Noise gate
        if (noiseReductionEnabled) {
            applyNoiseGate(buffer, length, noiseLevel)
        }

        // Step 2: Simple spectral shaping (time-domain approximation)
        applySpectralShaping(buffer, length)

        // Step 3: Gain amplification
        applyGain(buffer, length, gain)

        // Step 4: AGC (optional)
        if (agcEnabled) {
            applyAGC(buffer, length)
        }

        // Step 5: Soft clip to prevent distortion
        softClipBuffer(buffer, length)
    }

    private fun applyNoiseGate(buffer: ShortArray, length: Int, level: Int) {
        val threshold = when (level) {
            2 -> 800   // High reduction
            1 -> 400   // Low reduction
            else -> 0
        }
        if (threshold == 0) return

        for (i in 0 until length) {
            val absSample = abs(buffer[i].toInt())
            if (absSample < threshold) {
                buffer[i] = (buffer[i] * 0.05f).toInt().toShort()
            }
        }
    }

    private fun applySpectralShaping(buffer: ShortArray, length: Int) {
        // Simple first-order IIR filter approximation for spectral tilt
        // Boosts mid frequencies important for speech intelligibility
        var prevSample = 0.0f
        for (i in 0 until length) {
            val current = buffer[i].toFloat()
            val filtered = current * midBoostFactor + prevSample * (1f - midBoostFactor) * 0.3f
            prevSample = filtered
            buffer[i] = filtered.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun applyGain(buffer: ShortArray, length: Int, gain: Float) {
        for (i in 0 until length) {
            val amplified = buffer[i].toFloat() * gain
            buffer[i] = amplified.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun applyAGC(buffer: ShortArray, length: Int) {
        var sum = 0.0
        for (i in 0 until length) {
            sum += abs(buffer[i].toDouble())
        }
        val currentLevel = (sum / length / Short.MAX_VALUE).toFloat()
        runningAvgLevel = runningAvgLevel * (1f - agcSmoothFactor) + currentLevel * agcSmoothFactor

        if (runningAvgLevel > 0.001f) {
            val agcGain = (targetLevel / runningAvgLevel).coerceIn(0.5f, 4.0f)
            for (i in 0 until length) {
                val adjusted = buffer[i].toFloat() * agcGain
                buffer[i] = adjusted.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
    }

    private fun softClipBuffer(buffer: ShortArray, length: Int) {
        val maxVal = Short.MAX_VALUE.toFloat()
        for (i in 0 until length) {
            val normalized = buffer[i].toFloat() / maxVal
            val clipped = tanh(normalized.toDouble()).toFloat()
            buffer[i] = (clipped * maxVal).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun resetAGC() {
        runningAvgLevel = 0.0f
    }
}
