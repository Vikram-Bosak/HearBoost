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

@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var largeTextMode by remember { mutableStateOf(true) }
    var extraLargeButtons by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }
    var safetyLimit by remember { mutableStateOf(true) }
    var maxVolume by remember { mutableIntStateOf(85) }

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
                Text("Settings", style = HeadlineSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryContainer)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Accessibility
                SectionHeader("Accessibility")

                SettingsToggleCard(
                    icon = Icons.Filled.FormatSize,
                    label = "Large Text Mode",
                    description = "",
                    checked = largeTextMode,
                    onCheckedChange = { largeTextMode = it }
                )

                SettingsToggleCard(
                    icon = Icons.Filled.AdsClick,
                    label = "Extra Large Buttons",
                    description = "",
                    checked = extraLargeButtons,
                    onCheckedChange = { extraLargeButtons = it }
                )

                SettingsToggleCard(
                    icon = Icons.Filled.Contrast,
                    label = "High Contrast Mode",
                    description = "",
                    checked = highContrast,
                    onCheckedChange = { highContrast = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Audio Safety
                SectionHeader("Audio Safety")

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainer,
                    border = BorderStroke(1.dp, ActiveBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Shield, null, tint = Secondary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Max Volume Limit", style = BodyLarge)
                            }
                            Switch(
                                checked = safetyLimit,
                                onCheckedChange = { safetyLimit = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryContainer,
                                    uncheckedThumbColor = OnSurfaceVariant,
                                    uncheckedTrackColor = SurfaceVariant
                                )
                            )
                        }

                        if (safetyLimit) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Volume Level", style = LabelLarge, color = OnSurfaceVariant)
                                Text("$maxVolume%", style = HeadlineMedium.copy(color = if (maxVolume > 85) Error else Secondary))
                            }
                            Slider(
                                value = maxVolume.toFloat(),
                                onValueChange = { maxVolume = it.toInt() },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = VolumeThumb,
                                    activeTrackColor = if (maxVolume > 85) Error else Secondary,
                                    inactiveTrackColor = VolumeTrack
                                )
                            )

                            // WHO warning
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SecondaryContainer.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, SecondaryContainer.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Warning, null, tint = Secondary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "WHO recommends max 85dB for extended use.",
                                        style = LabelMedium,
                                        color = SecondaryFixed
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // General
                SectionHeader("General")

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainer,
                    border = BorderStroke(1.dp, ActiveBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Language, null, tint = Primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Language", style = BodyLarge)
                            Text("English / हिंदी", style = LabelMedium, color = OnSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Send Feedback", style = BodyLarge, color = OnSurface)
                }
                TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("About HearBoost", style = BodyLarge, color = OnSurface)
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Version 1.0.0", style = LabelMedium, color = OnSurfaceVariant.copy(alpha = 0.4f))
                    Text("Made for clear hearing.", style = LabelMedium, color = OnSurfaceVariant.copy(alpha = 0.4f))
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = LabelMedium.copy(letterSpacing = 2.sp),
        color = OnSurfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainer,
        border = BorderStroke(1.dp, ActiveBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, style = BodyLarge)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryContainer,
                    uncheckedThumbColor = OnSurfaceVariant,
                    uncheckedTrackColor = SurfaceVariant
                )
            )
        }
    }
}

private val Float.Companion.sp get() = this
