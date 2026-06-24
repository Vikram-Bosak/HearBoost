package com.hearboost.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Microphone manager — handles permission checks and mic availability.
 */
@Singleton
class MicrophoneManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isMicAvailable(): Boolean {
        val pm = context.packageManager
        return pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_MICROPHONE)
    }

    fun hasPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
