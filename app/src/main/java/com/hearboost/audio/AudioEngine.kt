package com.hearboost.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core audio engine: manages the full pipeline from mic → processing → headphones.
 * Uses AudioRecord for capture and AudioTrack for playback with minimum latency settings.
 */
@Singleton
class AudioEngine @Inject constructor() {

    companion object {
        private const val TAG = "AudioEngine"
        const val SAMPLE_RATE = 48000
        const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val MAX_AMP_FACTOR = 20.0f // Maximum amplification factor
    }

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var processingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isRunning = false
        private set

    // Processing parameters (can be adjusted at runtime)
    var gainFactor: Float = 3.0f
    var noiseReductionEnabled: Boolean = true
    var noiseReductionLevel: Int = 1 // 0=off, 1=low, 2=high

    // Callback for audio level metering (UI waveform visualization)
    var onAudioLevel: ((Float) -> Unit)? = null

    // Calculate optimal buffer size
    private fun calculateBufferSize(): Int {
        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_FORMAT)
        return maxOf(minBuf, SAMPLE_RATE / 25) // ~40ms buffer for low latency
    }

    fun start(): Boolean {
        if (isRunning) return true

        return try {
            val bufferSize = calculateBufferSize()
            Log.d(TAG, "Buffer size: $bufferSize bytes")

            // Initialize AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_IN,
                AUDIO_FORMAT,
                bufferSize * 2 // Double buffer for safety
            ).also {
                if (it.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord failed to initialize")
                    return false
                }
            }

            // Initialize AudioTrack for direct output
            val trackBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE, CHANNEL_OUT, AUDIO_FORMAT
            )
            audioTrack = AudioTrack.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_OUT)
                        .setEncoding(AUDIO_FORMAT)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(trackBufferSize, bufferSize))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            audioRecord?.startRecording()
            audioTrack?.play()
            isRunning = true

            // Start the processing loop
            processingJob = scope.launch { audioProcessingLoop(bufferSize) }
            Log.d(TAG, "AudioEngine started successfully")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing RECORD_AUDIO permission", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AudioEngine", e)
            false
        }
    }

    fun stop() {
        isRunning = false
        processingJob?.cancel()
        processingJob = null

        try {
            audioRecord?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }

        try {
            audioTrack?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }

        audioRecord = null
        audioTrack = null
        Log.d(TAG, "AudioEngine stopped")
    }

    /**
     * Main audio processing loop — reads PCM from mic, applies DSP, writes to output.
     */
    private suspend fun audioProcessingLoop(bufferSize: Int) = withContext(Dispatchers.Default) {
        val buffer = ShortArray(bufferSize / 2) // 16-bit PCM = 2 bytes per sample
        val noiseGateThreshold = if (noiseReductionEnabled) {
            when (noiseReductionLevel) {
                2 -> 800  // High: aggressive gate
                1 -> 400  // Low: moderate gate
                else -> 0 // Off
            }
        } else 0

        while (isActive && isRunning) {
            val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (readCount <= 0) continue

            // === DSP Pipeline ===
            processAudioBuffer(
                buffer,
                readCount,
                gainFactor,
                noiseReductionEnabled,
                noiseGateThreshold,
                noiseReductionLevel
            )

            // Write processed audio to output
            audioTrack?.write(buffer, 0, readCount)

            // Calculate audio level for UI visualization
            val level = calculateAudioLevel(buffer, readCount)
            onAudioLevel?.invoke(level)
        }
    }

    /**
     * Process a single buffer: noise gate → amplification → soft clip.
     */
    private fun processAudioBuffer(
        buffer: ShortArray,
        length: Int,
        gain: Float,
        reduceNoise: Boolean,
        gateThreshold: Int,
        level: Int
    ) {
        for (i in 0 until length) {
            var sample = buffer[i].toFloat()

            // Step 1: Smooth Noise Gate (prevents clicking)
            if (reduceNoise && gateThreshold > 0) {
                val absSample = kotlin.math.abs(sample.toInt())
                if (absSample < gateThreshold) {
                    // Smooth fade out instead of hard cut
                    sample *= 0.3f
                } else {
                    // Smooth transition above gate
                    val ratio = ((absSample - gateThreshold).toFloat() / (Short.MAX_VALUE - gateThreshold))
                        .coerceIn(0.3f, 1.0f)
                    sample *= ratio
                }
            }

            // Step 2: Gain Amplification (reduced default)
            sample *= gain * 0.7f

            // Step 3: Soft Clipping (prevents harsh distortion)
            sample = softClip(sample)

            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    /**
     * Soft clipping function — smooth saturation curve instead of hard digital clip.
     * Preserves natural sound at high gains.
     */
    private fun softClip(sample: Float): Float {
        val maxVal = Short.MAX_VALUE.toFloat()
        val normalized = sample / maxVal
        val clipped = kotlin.math.tanh(normalized.toDouble()).toFloat()
        return clipped * maxVal
    }

    /**
     * Calculate RMS audio level normalized to 0.0..1.0 for UI metering.
     */
    private fun calculateAudioLevel(buffer: ShortArray, length: Int): Float {
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i].toDouble() * buffer[i].toDouble()
        }
        val rms = kotlin.math.sqrt(sum / length)
        return (rms / Short.MAX_VALUE).toFloat().coerceIn(0f, 1f)
    }

    fun setGain(factor: Float) {
        gainFactor = factor.coerceIn(0.1f, MAX_AMP_FACTOR)
    }

    fun setNoiseReduction(enabled: Boolean, level: Int = 1) {
        noiseReductionEnabled = enabled
        noiseReductionLevel = level.coerceIn(0, 2)
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
