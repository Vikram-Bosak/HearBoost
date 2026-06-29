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
 * Core audio engine: manages mic → processing → headphones pipeline.
 * Uses VOICE_COMMUNICATION source when headphones are connected for echo-free audio.
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

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var processingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isRunning = false
        private set

    var gainFactor: Float = 3.0f
    var noiseReductionEnabled: Boolean = true
    var noiseReductionLevel: Int = 1
    var onAudioLevel: ((Float) -> Unit)? = null

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
            Log.d(TAG, "Buffer size: $bufferSize bytes")

            val headphoneConnected = context?.let { isHeadphoneConnected(it) } ?: false
            Log.d(TAG, "Headphone connected: $headphoneConnected")

            // KEY FIX: When headphones connected, use VOICE_COMMUNICATION
            // This routes to headphone mic AND enables Android's built-in echo cancellation
            // When no headphones, use MIC for best phone mic quality
            val audioSource = if (headphoneConnected) {
                MediaRecorder.AudioSource.VOICE_COMMUNICATION
            } else {
                MediaRecorder.AudioSource.MIC
            }

            Log.d(TAG, "Audio source: $audioSource (headphone=$headphoneConnected)")

            audioRecord = AudioRecord(
                audioSource,
                SAMPLE_RATE,
                CHANNEL_IN,
                AUDIO_FORMAT,
                bufferSize * 2
            ).also {
                if (it.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord failed to initialize")
                    return false
                }
            }

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

            // Prime with silence to prevent click
            val silenceBuffer = ShortArray(512)
            audioTrack?.write(silenceBuffer, 0, silenceBuffer.size)

            isRunning = true
            Thread.sleep(30)

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

        try { audioRecord?.apply { stop(); release() } } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        }
        try { audioTrack?.apply { stop(); release() } } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }

        audioRecord = null
        audioTrack = null
        Log.d(TAG, "AudioEngine stopped")
    }

    private suspend fun audioProcessingLoop(bufferSize: Int) = withContext(Dispatchers.Default) {
        val buffer = ShortArray(bufferSize / 2)
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
            val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (readCount <= 0) continue

            // Fade-in
            if (fadeSamples < fadeLength) {
                for (i in 0 until readCount) {
                    if (fadeSamples >= fadeLength) break
                    val fadeFactor = fadeSamples.toFloat() / fadeLength
                    buffer[i] = (buffer[i] * fadeFactor).toInt().toShort()
                    fadeSamples++
                }
            }

            processAudioBuffer(
                buffer, readCount, gainFactor,
                noiseReductionEnabled, noiseGateThreshold
            )

            audioTrack?.write(buffer, 0, readCount)

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

            // Smooth noise gate
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

            // Gain — reduced to prevent feedback
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
