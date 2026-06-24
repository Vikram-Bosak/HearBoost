package com.hearboost.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.hearboost.MainActivity
import com.hearboost.R
import com.hearboost.audio.AudioEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * ForegroundService — keeps audio processing alive in background.
 * Displays a persistent notification showing current status.
 */
@AndroidEntryPoint
class AudioForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "hearboost_audio_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.hearboost.START_AUDIO"
        const val ACTION_STOP = "com.hearboost.STOP_AUDIO"
        const val ACTION_UPDATE_GAIN = "com.hearboost.UPDATE_GAIN"
        const val EXTRA_GAIN = "extra_gain"
        const val EXTRA_NOISE_ENABLED = "extra_noise_enabled"
        const val EXTRA_NOISE_LEVEL = "extra_noise_level"
    }

    @Inject lateinit var audioEngine: AudioEngine

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundNotification()
                audioEngine.start()
            }
            ACTION_STOP -> {
                audioEngine.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE_GAIN -> {
                val gain = intent.getFloatExtra(EXTRA_GAIN, 3.0f)
                val noiseEnabled = intent.getBooleanExtra(EXTRA_NOISE_ENABLED, true)
                val noiseLevel = intent.getIntExtra(EXTRA_NOISE_LEVEL, 1)
                audioEngine.setGain(gain)
                audioEngine.setNoiseReduction(noiseEnabled, noiseLevel)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        audioEngine.stop()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "HearBoost Audio",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active audio amplification"
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun startForegroundNotification() {
        val notification = buildNotification("Listening... Amplification active")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, AudioForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HearBoost")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_hearing)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HearBoost::AudioWakeLock")
        wakeLock?.acquire(8 * 60 * 60 * 1000L) // 8 hours max
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}
