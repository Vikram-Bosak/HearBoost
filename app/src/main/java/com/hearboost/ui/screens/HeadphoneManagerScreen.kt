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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hearboost.ui.theme.*
import com.hearboost.viewmodel.HomeViewModel

@Composable
fun HeadphoneManagerScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Primary)
                }
                Text("Headphones", style = HeadlineSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryContainer)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Output Type Section
                Text(
                    "OUTPUT TYPE",
                    style = LabelMedium.copy(letterSpacing = 2.sp),
                    color = OnSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Wired option
                OutputTypeCard(
                    icon = Icons.Filled.SettingsInputComponent,
                    iconColor = Primary,
                    title = "Wired Earphones",
                    subtitle = "Plug in 3.5mm jack"
                )

                // Bluetooth option
                OutputTypeCard(
                    icon = Icons.Filled.Bluetooth,
                    iconColor = Secondary,
                    title = "Bluetooth",
                    subtitle = "Tap to pair device"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Saved Devices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "SAVED DEVICES",
                        style = LabelMedium.copy(letterSpacing = 2.sp),
                        color = OnSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Connected device
                if (state.headphoneConnected) {
                    ConnectedDeviceCard(
                        name = state.headphoneName,
                        battery = 87
                    )
                }

                // Inactive device
                DeviceCard(
                    name = "Wired Earphones",
                    subtitle = "Not connected"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pair new device button
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.full,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = OnPrimaryContainer
                    )
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pair New Device", style = ButtonText)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun OutputTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerHigh,
        border = BorderStroke(1.dp, Color.Transparent),
        onClick = { }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = HeadlineMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize))
                Text(subtitle, style = BodyMedium, color = OnSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = OutlineVariant)
        }
    }
}

@Composable
private fun ConnectedDeviceCard(
    name: String,
    battery: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerHighest,
        border = BorderStroke(2.dp, PrimaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .padding(start =4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left border indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(PrimaryContainer, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(PrimaryContainer.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Headphones, null, tint = PrimaryContainer, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = HeadlineMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PrimaryContainer, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Connected", style = LabelLarge.copy(fontWeight = FontWeight.Bold), color = PrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Battery5Bar, null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("$battery%", style = LabelLarge, color = OnSurfaceVariant)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreVert, null, tint = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DeviceCard(
    name: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, ActiveBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(ActiveBorder, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Headphones, null, tint = Outline, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = HeadlineMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize), color = OnSurface.copy(alpha = 0.6f))
                Text(subtitle, style = BodyMedium, color = OnSurfaceVariant)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreVert, null, tint = OnSurfaceVariant)
            }
        }
    }
}


