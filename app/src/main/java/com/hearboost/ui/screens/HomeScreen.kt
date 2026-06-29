package com.hearboost.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hearboost.ui.theme.*
import com.hearboost.viewmodel.HomeViewModel
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToHeadphones: () -> Unit,
    onNavigateToAudioSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            HomeTopAppBar(
                headphoneConnected = state.headphoneConnected,
                headphoneName = state.headphoneName,
                batteryPercent = state.batteryPercent,
                onSettingsClick = onNavigateToSettings
            )

            // Status Card
            HeadphoneStatusCard(
                isConnected = state.headphoneConnected,
                headphoneName = state.headphoneName,
                headphoneType = state.headphoneType,
                onClick = onNavigateToHeadphones
            )

            // Main content area
            if (state.isListening) {
                ActiveListeningContent(
                    isListening = state.isListening,
                    audioLevel = state.audioLevel,
                    volumePercent = state.volumePercent,
                    noiseReductionEnabled = state.noiseReductionEnabled,
                    onToggleListening = { viewModel.toggleListening() },
                    onVolumeChange = { viewModel.updateVolume(it) },
                    onToggleNoise = { viewModel.toggleNoiseReduction() },
                    onPause = { viewModel.toggleListening() }
                )
            } else {
                IdleContent(
                    audioLevel = state.audioLevel,
                    volumePercent = state.volumePercent,
                    noiseReductionEnabled = state.noiseReductionEnabled,
                    activeProfile = state.activeProfile,
                    onToggleListening = { viewModel.toggleListening() },
                    onVolumeChange = { viewModel.updateVolume(it) },
                    onToggleNoise = { viewModel.toggleNoiseReduction() },
                    onNavigateToProfiles = onNavigateToProfiles,
                    onNavigateToAudioSettings = onNavigateToAudioSettings
                )
            }
        }

        // Bottom Navigation
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onNavigateToProfiles = onNavigateToProfiles,
            onNavigateToAudioSettings = onNavigateToAudioSettings
        )
    }
}

@Composable
private fun HomeTopAppBar(
    headphoneConnected: Boolean,
    headphoneName: String,
    batteryPercent: Int,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Hearing, null, tint = PrimaryContainer, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("HearBoost", style = HeadlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = PrimaryContainer)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Battery pill
            Surface(
                shape = CircleShape,
                color = SurfaceContainer,
                border = BorderStroke(1.dp, OutlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.BatteryChargingFull, null, tint = SecondaryFixedDim, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$batteryPercent%", style = LabelMedium, color = OnSurface)
                }
            }

            // Settings button
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, "Settings", tint = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HeadphoneStatusCard(
    isConnected: Boolean,
    headphoneName: String,
    headphoneType: com.hearboost.bluetooth.HeadphoneManager.HeadphoneType,
    onClick: () -> Unit
) {
    val borderColor = if (isConnected) Primary else ActiveBorder

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F2035),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isConnected) Icons.Filled.Headphones else Icons.Filled.HeadsetOff,
                    null,
                    tint = if (isConnected) Primary else OnSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        if (isConnected) headphoneName else "Not connected",
                        style = LabelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = OnSurface
                    )
                    Text(
                        if (isConnected) "Connected" else "Connect headphones to begin",
                        style = LabelMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant)
        }
    }
}

@Composable
private fun IdleContent(
    audioLevel: Float,
    volumePercent: Int,
    noiseReductionEnabled: Boolean,
    activeProfile: String,
    onToggleListening: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onToggleNoise: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToAudioSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Central Start Listening Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Radial glow
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryContainer.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Main button
                Button(
                    onClick = onToggleListening,
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F2035)
                    ),
                    border = BorderStroke(2.dp, ActiveBorder),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Start Listening",
                            modifier = Modifier.size(48.dp),
                            tint = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Start Listening", style = LabelLarge, color = OnSurface)
                    }
                }

                // Waveform bars (inactive state)
                Spacer(modifier = Modifier.height(48.dp))
                WaveformBars(audioLevel = audioLevel, isActive = false)
            }
        }

        // Volume Slider
        VolumeSlider(
            volumePercent = volumePercent,
            onVolumeChange = onVolumeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Noise Reduction Toggle
        NoiseReductionCard(
            enabled = noiseReductionEnabled,
            onToggle = onToggleNoise
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Profile Card
        ProfileQuickCard(
            activeProfile = activeProfile,
            onSwitch = onNavigateToProfiles
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ActiveListeningContent(
    isListening: Boolean,
    audioLevel: Float,
    volumePercent: Int,
    noiseReductionEnabled: Boolean,
    onToggleListening: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onToggleNoise: () -> Unit,
    onPause: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Waveform Visualization
        WaveformBars(audioLevel = audioLevel, isActive = true)

        Spacer(modifier = Modifier.height(24.dp))

        // Listening indicator
        val infiniteTransition = rememberInfiniteTransition(label = "listening")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                tween(2000), RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        // Active circular button
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse rings
            for (i in 1..3) {
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.8f,
                    animationSpec = infiniteRepeatable(
                        tween(3000, delayMillis = i * 1000),
                        RepeatMode.Restart
                    ),
                    label = "ring$i"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .alpha((1f - scale / 1.8f) * 0.3f)
                        .border(2.dp, PrimaryContainer.copy(alpha = 0.3f), CircleShape)
                )
            }

            Button(
                onClick = onToggleListening,
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer
                ),
                border = null,
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.GraphicEq,
                        contentDescription = "Stop",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Text("Listening...", style = ButtonText, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Volume Slider
        VolumeSlider(
            volumePercent = volumePercent,
            onVolumeChange = onVolumeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Noise Reduction Toggle
        NoiseReductionCard(
            enabled = noiseReductionEnabled,
            onToggle = onToggleNoise
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onPause,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, OutlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerHigh)
            ) {
                Icon(Icons.Filled.PauseCircle, null, tint = Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pause", style = ButtonText, color = OnSurface)
            }

            OutlinedButton(
                onClick = onToggleListening,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, OutlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainerHigh)
            ) {
                Icon(Icons.Filled.MicOff, null, tint = Error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop", style = ButtonText, color = OnSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun VolumeSlider(
    volumePercent: Int,
    onVolumeChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainer,
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VolumeUp, null, tint = PrimaryContainer, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volume", style = HeadlineMedium.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize), color = OnSurface)
                }
                Text("$volumePercent%", style = HeadlineMedium.copy(color = PrimaryContainer))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = volumePercent.toFloat(),
                onValueChange = { onVolumeChange(it.toInt()) },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = VolumeThumb,
                    activeTrackColor = PrimaryContainer,
                    inactiveTrackColor = VolumeTrack
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("MIN", style = LabelMedium, color = OnSurfaceVariant)
                Text("MAX", style = LabelMedium, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NoiseReductionCard(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerHigh,
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.BlurOn, null, tint = Tertiary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Noise Reduction", style = BodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = OnSurface)
                    Text("Clarity in busy places", style = LabelMedium, color = OnSurfaceVariant)
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryContainer,
                    uncheckedThumbColor = OnSurfaceVariant,
                    uncheckedTrackColor = SurfaceVariant
                ),
                modifier = Modifier.size(56.dp, 32.dp)
            )
        }
    }
}

@Composable
private fun ProfileQuickCard(
    activeProfile: String,
    onSwitch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainer,
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Restaurant, null, tint = Secondary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(activeProfile, style = BodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = OnSurface)
                    Text("Optimized for conversation", style = LabelMedium, color = OnSurfaceVariant)
                }
            }

            Surface(
                onClick = onSwitch,
                shape = CircleShape,
                color = PrimaryContainer.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, PrimaryContainer.copy(alpha = 0.2f))
            ) {
                Text(
                    "Switch",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = LabelLarge,
                    color = PrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun WaveformBars(
    audioLevel: Float,
    isActive: Boolean
) {
    val barCount = 32
    val bars = remember { (0 until barCount).map { Random.nextFloat() } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { index, seed ->
            val heightFraction = if (isActive && audioLevel > 0.01f) {
                (audioLevel * (0.3f + seed * 0.7f) * 10f).coerceIn(0.1f, 1f)
            } else {
                0.1f + 0.1f * kotlin.math.sin(index * 0.5).toFloat()
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightFraction.coerceAtLeast(0.08f))
                    .background(
                        if (isActive && audioLevel > 0.01f) PrimaryContainer else ActiveBorder,
                        RoundedCornerShape(2.dp)
                    )
            )
            if (index < barCount - 1) {
                Spacer(modifier = Modifier.width(3.dp))
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    modifier: Modifier = Modifier,
    onNavigateToProfiles: () -> Unit = {},
    onNavigateToAudioSettings: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceContainer,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Listen (active)
            Column(
                modifier = Modifier
                    .background(PrimaryContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.GraphicEq, null, tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                Text("Listen", style = LabelMedium, color = OnPrimaryContainer)
            }
            // Profiles
            Column(
                modifier = Modifier
                    .clickable { onNavigateToProfiles() }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.AccountCircle, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                Text("Profiles", style = LabelMedium, color = OnSurfaceVariant)
            }
            // History
            Column(
                modifier = Modifier
                    .clickable { onNavigateToAudioSettings() }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.History, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                Text("History", style = LabelMedium, color = OnSurfaceVariant)
            }
        }
    }
}
