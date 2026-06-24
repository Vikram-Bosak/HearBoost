package com.hearboost.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hearboost.ui.theme.*
import com.hearboost.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var volumeLevel by remember { mutableIntStateOf(state.volumePercent) }
    var bassBoost by remember { mutableIntStateOf(4) }
    var clarityBoost by remember { mutableIntStateOf(8) }
    var noiseMode by remember { mutableIntStateOf(1) } // 0=off, 1=low, 2=high
    var latencyMode by remember { mutableIntStateOf(0) } // 0=low delay, 1=high quality

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Primary)
                }
                Text("Audio Settings", style = HeadlineSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryContainer)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Main Volume
                SectionLabel("Main Volume")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Main Volume", style = LabelLarge, color = OnSurfaceVariant)
                    Text("$volumeLevel%", style = HeadlineMedium.copy(color = PrimaryFixed))
                }
                Slider(
                    value = volumeLevel.toFloat(),
                    onValueChange = { volumeLevel = it.toInt() },
                    onValueChangeFinished = { viewModel.updateVolume(volumeLevel) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = VolumeThumb,
                        activeTrackColor = PrimaryContainer,
                        inactiveTrackColor = VolumeTrack
                    )
                )

                DottedDivider()

                // Sound Enhancement
                SectionLabel("Sound Enhancement")

                // Bass Boost
                SettingsSliderCard(
                    icon = Icons.Filled.Equalizer,
                    iconColor = Tertiary,
                    label = "Bass Boost",
                    level = bassBoost,
                    maxLevel = 10,
                    onValueChange = { bassBoost = it },
                    trackColor = Tertiary
                )

                // Clarity Boost
                SettingsSliderCard(
                    icon = Icons.Filled.Hearing,
                    iconColor = Primary,
                    label = "Clarity Boost",
                    level = clarityBoost,
                    maxLevel = 10,
                    onValueChange = { clarityBoost = it },
                    trackColor = Primary
                )

                DottedDivider()

                // Noise Reduction Mode
                SectionLabel("Noise Reduction")
                SegmentedControl(
                    options = listOf("Off", "Low", "High"),
                    selectedIndex = noiseMode,
                    onSelect = { noiseMode = it; viewModel.setNoiseReductionLevel(it) }
                )

                // Latency Mode
                SectionLabel("Latency Mode")
                SegmentedControl(
                    options = listOf("Low Delay", "High Quality"),
                    selectedIndex = latencyMode,
                    onSelect = { latencyMode = it }
                )
                Text(
                    "Wired: ~15ms, Bluetooth: ~200ms",
                    style = LabelLarge,
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Reset button
                OutlinedButton(
                    onClick = {
                        volumeLevel = 70
                        bassBoost = 4
                        clarityBoost = 8
                        noiseMode = 1
                        latencyMode = 0
                        viewModel.updateVolume(70)
                        viewModel.setNoiseReductionLevel(1)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.full,
                    border = BorderStroke(2.dp, Outline)
                ) {
                    Text("Reset to Defaults", style = ButtonText, color = OnSurface)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = LabelLarge.copy(letterSpacing = 2.sp),
        color = Color(0xFF7A9AB5)
    )
}

@Composable
private fun DottedDivider() {
    Divider(
        color = ActiveBorder,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsSliderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    level: Int,
    maxLevel: Int,
    onValueChange: (Int) -> Unit,
    trackColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, style = BodyLarge)
                }
                Text("Level $level", style = LabelLarge.copy(color = iconColor))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = level.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..maxLevel.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = trackColor,
                    activeTrackColor = trackColor,
                    inactiveTrackColor = VolumeTrack
                )
            )
        }
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(4.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            Surface(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                color = if (index == selectedIndex) PrimaryContainer else Color.Transparent,
                shadowElevation = if (index == selectedIndex) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        style = ButtonText,
                        color = if (index == selectedIndex) OnPrimaryContainer else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

private val Float.Companion.sp get() = this
