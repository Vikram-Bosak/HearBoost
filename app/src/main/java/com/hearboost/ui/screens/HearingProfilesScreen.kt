package com.hearboost.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hearboost.ui.theme.*

data class HearingProfile(
    val name: String,
    val emoji: String,
    val description: String,
    val isActive: Boolean = false,
    val volume: Int = 70,
    val noiseLevel: String = "Low"
)

@Composable
fun HearingProfilesScreen(
    onBack: () -> Unit
) {
    val profiles = listOf(
        HearingProfile("Dad's Settings", "👴", "Optimized for conversation", true, 80, "Hi"),
        HearingProfile("Conversation", "🎯", "Optimized for voice clarity", volume = 70, noiseLevel = "Med"),
        HearingProfile("TV Watching", "📺", "Boosted mid-range frequencies", volume = 85, noiseLevel = "Hi"),
        HearingProfile("Outdoors", "🌳", "Wind noise reduced significantly", volume = 60, noiseLevel = "Max")
    )

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
                Text("Hearing Profiles", style = HeadlineSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryContainer)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerHigh
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(PrimaryContainer.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text("Current Focus", style = LabelMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Normal
                            ), color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Optimization Active", style = DisplayLarge.copy(fontSize = MaterialTheme.typography.headlineLarge.fontSize), color = PrimaryContainer)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section label
                Text(
                    "QUICK PRESETS",
                    style = LabelMedium.copy(letterSpacing = 2.sp),
                    color = OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active profile (larger card)
                val activeProfile = profiles.first { it.isActive }
                ActiveProfileCard(profile = activeProfile)

                Spacer(modifier = Modifier.height(16.dp))

                // Other profiles
                profiles.filter { !it.isActive }.forEach { profile ->
                    ProfileListCard(profile = profile)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Create custom profile
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerHigh,
                    border = BorderStroke(2.dp, OutlineVariant.copy(alpha = 0.5f)),
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.AddCircle, null, tint = Primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Create Custom Profile", style = ButtonText, color = OnSurface)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ActiveProfileCard(profile: HearingProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerHigh,
        border = BorderStroke(2.dp, PrimaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${profile.emoji} ${profile.name}", style = HeadlineMedium.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize))
                Surface(
                    shape = CircleShape,
                    color = PrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(OnPrimaryContainer, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active", style = LabelMedium.copy(fontWeight = FontWeight.Bold), color = OnPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.VolumeUp, null, tint = PrimaryContainer, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vol ${profile.volume}%", style = BodyMedium, color = OnSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BlurOn, null, tint = PrimaryContainer, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Noise: ${profile.noiseLevel}", style = BodyMedium, color = OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProfileListCard(profile: HearingProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariant),
        onClick = { }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceContainerHighest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = HeadlineMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize))
                Text(profile.description, style = BodyMedium, color = OnSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = OnSurfaceVariant)
        }
    }
}


