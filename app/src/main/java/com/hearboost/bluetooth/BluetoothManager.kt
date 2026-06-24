package com.hearboost.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BluetoothManager — manages Bluetooth headphone detection, connection state,
 * and wired headphone detection.
 */
@Singleton
class HeadphoneManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "HeadphoneManager"
    }

    enum class HeadphoneType { NONE, WIRED, BLUETOOTH }

    data class HeadphoneInfo(
        val type: HeadphoneType = HeadphoneType.NONE,
        val name: String = "Not connected",
        val batteryPercent: Int = -1,
        val isConnected: Boolean = false
    )

    private val _headphoneState = MutableStateFlow(HeadphoneInfo())
    val headphoneState: StateFlow<HeadphoneInfo> = _headphoneState.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val bluetoothManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    } else null

    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val audioDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkAudioDevices()
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> checkBluetoothDevices()
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> checkBluetoothDevices()
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> checkBluetoothDevices()
            }
        }
    }

    fun startListening() {
        // Register for audio device changes
        val audioFilter = IntentFilter().apply {
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        context.registerReceiver(audioDeviceReceiver, audioFilter)

        // Register for Bluetooth changes
        val btFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(bluetoothReceiver, btFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(bluetoothReceiver, btFilter)
        }

        // Initial check
        checkAudioDevices()
    }

    fun stopListening() {
        try { context.unregisterReceiver(audioDeviceReceiver) } catch (_: Exception) {}
        try { context.unregisterReceiver(bluetoothReceiver) } catch (_: Exception) {}
    }

    fun isHeadphoneConnected(): Boolean {
        return checkAudioDevices()
    }

    @SuppressLint("MissingPermission")
    private fun checkAudioDevices(): Boolean {
        // Check wired headphones
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val hasWired = audioDevices.any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }

        // Check Bluetooth audio
        val hasBluetooth = audioDevices.any {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }

        return when {
            hasWired -> {
                _headphoneState.value = HeadphoneInfo(
                    type = HeadphoneType.WIRED,
                    name = "Wired Earphones",
                    isConnected = true
                )
                true
            }
            hasBluetooth -> {
                val btName = getBluetoothDeviceName()
                _headphoneState.value = HeadphoneInfo(
                    type = HeadphoneType.BLUETOOTH,
                    name = btName,
                    isConnected = true
                )
                true
            }
            else -> {
                _headphoneState.value = HeadphoneInfo()
                false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkBluetoothDevices() {
        checkAudioDevices()
    }

    @SuppressLint("MissingPermission")
    private fun getBluetoothDeviceName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermission = context.checkSelfPermission(
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return "Bluetooth Device"
        }

        val a2dp = bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(p0: Int, p1: BluetoothProfile) {}
            override fun onServiceDisconnected(p0: Int) {}
        }, BluetoothProfile.A2DP)
        Thread.sleep(100)
        return bluetoothAdapter?.name ?: "Bluetooth Device"
    }

    fun getLatencyInfo(): String {
        return when (_headphoneState.value.type) {
            HeadphoneType.WIRED -> "Wired: ~15ms latency"
            HeadphoneType.BLUETOOTH -> "Bluetooth: ~200ms latency"
            HeadphoneType.NONE -> "No headphones connected"
        }
    }
}
