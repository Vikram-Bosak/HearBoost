package com.hearboost.audio

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio engine with dual mic support.
 * Strategy:
 * 1. Use MIC source (captures from both phone + headphone on many devices)
 * 2. If headphone detected, also try secondary mic capture
 * 3. Mix both streams for complete audio coverage
 */
@Singleton
class AudioEngine @Inject constructor() {

    companion object {
        private const val TAG = "AudioEngine"
        const val SAMPLE_RATE = 44100
        const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val MAX_AMP_FACTOR = 20.0f
    }

    // Primary audio capture
    private var audioRecord: AudioRecord? = null
    // Secondary mic (phone mic when headphone is connected)
    private var secondaryAudioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var processingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isRunning = false
        private set

    var gainFactor: Float = 3.0f
    var noiseReductionEnabled: Boolean = true
    var noiseReductionLevel: Int = 1
    var onAudioLevel: ((Float) -> Unit)? = null

    private var headphoneConnected = false

    private fun calculateBufferSize(): Int {
        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, AUDIO_FORMAT)
        return maxOf(minBuf, SAMPLE_RATE / 25)
    }

    private fun isHeadphoneConnected(context: android.content.Context): Boolean {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
    }

    fun start(context: android.content.Context? = null): Boolean {
        if (isRunning) return true

        return try {
            val bufferSize = calculateBufferSize()
            headphoneConnected = context?.let { isHeadphoneConnected(it) } ?: false
            Log.d(TAG, "Headphone connected: $headphoneConnected")

            // PRIMARY: Always use MIC source
            // On many devices, MIC source captures from BOTH phone + headphone mics
            Log.d(TAG, "Using MIC source (dual mic strategy)")
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_IN,
                AUDIO_FORMAT,
                bufferSize * 2
            ).also {
                if (it.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "Primary AudioRecord failed")
                    return false
                }
            }

            // SECONDARY: If headphone connected, try to open phone mic separately
            // This captures ambient sound that headphone mic misses
            if (headphoneConnected) {
                try {
                    secondaryAudioRecord = AudioRecord(
                        MediaRecorder.AudioSource.CAMCORDER,
                        SAMPLE_RATE,
                        CHANNEL_IN,
                        AUDIO_FORMAT,
                        bufferSize * 2
                    )
                    if (secondaryAudioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                        Log.w(TAG, "Secondary AudioRecord failed - using primary only")
                        secondaryAudioRecord?.release()
                        secondaryAudioRecord = null
                    } else {
                        Log.d(TAG, "Secondary mic (phone) opened successfully")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not open secondary mic: ${e.message}")
                    secondaryAudioRecord = null
                }
            }

            // AudioTrack for output
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

            // Start all audio streams
            audioRecord?.startRecording()
            secondaryAudioRecord?.startRecording()
            audioTrack?.play()

            // Prime with silence
            val silenceBuffer = ShortArray(512)
            audioTrack?.write(silenceBuffer, 0, silenceBuffer.size)

            isRunning = true
            Thread.sleep(30)

            processingJob = scope.launch { audioProcessingLoop(bufferSize) }
            Log.d(TAG, "AudioEngine started - dual mic: ${secondaryAudioRecord != null}")
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

        try { audioRecord?.apply { stop(); release() } } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
        try { secondaryAudioRecord?.apply { stop(); release() } } catch (e: Exception) {
            Log.e(TAG, "Error stopping secondary AudioRecord", e)
        }
        try { audioTrack?.apply { stop(); release() } } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }

        audioRecord = null
        secondaryAudioRecord = null
        audioTrack = null
        Log.d(TAG, "AudioEngine stopped")
    }

    private suspend fun audioProcessingLoop(bufferSize: Int) = withContext(Dispatchers.Default) {
        val buffer = ShortArray(bufferSize / 2)
        val secondaryBuffer = ShortArray(bufferSize / 2)
        val mixedBuffer = ShortArray(bufferSize / 2)

        val noiseGateThreshold = if (noiseReductionEnabled) {
            when (noiseReductionLevel) {
                2 -> 800
                1 -> 400
                else -> 0
            }
        } else 0

        var fadeSamples = 0
        val fadeLength = SAMPLE_RATE / 20

        while (isActive && isRunning) {
            // Read from primary mic
            val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (readCount <= 0) continue

            // Read from secondary mic (phone mic) if available
            val hasSecondary = secondaryAudioRecord != null
            if (hasSecondary) {
                val secondaryRead = secondaryAudioRecord?.read(secondaryBuffer, 0, secondaryBuffer.size) ?: 0

                // Mix both audio streams
                // Primary (headphone) gets 60%, Secondary (phone) gets 40%
                for (i in 0 until readCount) {
                    val primary = buffer[i].toFloat() * 0.6f
                    val secondary = if (i < secondaryRead) secondaryBuffer[i].toFloat() * 0.4f else 0f
                    mixedBuffer[i] = (primary + secondary).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                }
                // Copy mixed back to buffer
                System.arraycopy(mixedBuffer, 0, buffer, 0, readCount)
            }

            // Fade-in
            if (fadeSamples < fadeLength) {
                for (i in 0 until readCount) {
                    if (fadeSamples >= fadeLength) break
                    val fadeFactor = fadeSamples.toFloat() / fadeLength
                    buffer[i] = (buffer[i] * fadeFactor).toInt().toShort()
                    fadeSamples++
                }
            }

            // Process audio
            processAudioBuffer(
                buffer, readCount, gainFactor,
                noiseReductionEnabled, noiseGateThreshold
            )

            // Output
            audioTrack?.write(buffer, 0, readCount)

            // UI level
            val level = calculateAudioLevel(buffer, readCount)
            onAudioLevel?.invoke(level)
        }
    }

    private fun processAudioBuffer(
        buffer: ShortArray, length: Int, gain: Float,
        reduceNoise: Boolean, gateThreshold: Int
    ) {
        for (i in 0 until length) {
            var sample = buffer[i].toFloat()

            // Noise gate
            if (reduceNoise && gateThreshold > 0) {
                val absSample = kotlin.math.abs(sample.toInt())
                if (absSample < gateThreshold) {
                    sample *= 0.2f
                } else {
                    val ratio = ((absSample - gateThreshold).toFloat() / (Short.MAX_VALUE - gateThreshold))
                        .coerceIn(0.3f, 1.0f)
                    sample *= ratio
                }
            }

            // Gain
            sample *= gain * 0.6f

            // Soft clip
            sample = softClip(sample)

            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun softClip(sample: Float): Float {
        val maxVal = Short.MAX_VALUE.toFloat()
        val normalized = sample / maxVal
        val clipped = kotlin.math.tanh(normalized.toDouble()).toFloat()
        return clipped * maxVal
    }

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
