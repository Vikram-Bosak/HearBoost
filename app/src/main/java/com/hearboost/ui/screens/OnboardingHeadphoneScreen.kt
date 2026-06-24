package com.hearboost.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hearboost.ui.theme.*

@Composable
fun OnboardingHeadphoneScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stepper dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(SurfaceVariant, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(24.dp).height(8.dp).background(PrimaryContainer, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(SurfaceVariant, CircleShape))
            }

            // Headphone icon cluster
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .alpha(0.1f)
                    .background(PrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wired card
                    Column(
                        modifier = Modifier
                            .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                            .border(2.dp, OutlineVariant, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Headphones,
                            contentDescription = "Wired",
                            modifier = Modifier.size(56.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Wired", style = LabelLarge, color = OnSurfaceVariant)
                    }

                    // Bluetooth card
                    Column(
                        modifier = Modifier
                            .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                            .border(2.dp, PrimaryContainer, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.BluetoothAudio,
                            contentDescription = "Wireless",
                            modifier = Modifier.size(56.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Wireless", style = LabelLarge, color = Primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Connect Your Headphones",
                style = HeadlineMedium,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Works with: Any wired earphones, Bluetooth headphones, Hearing loop devices. Plug in or pair your headphones now.",
                style = BodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Compatibility badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryContainer.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Hearing, null, tint = Primary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Universal Compatibility", style = LabelLarge, color = OnSurface)
                        Text(
                            "Optimized for standard audio output.",
                            style = LabelMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimaryContainer
                )
            ) {
                Text("Continue", style = ButtonText)
            }

            TextButton(onClick = onSkip) {
                Text("Already done", style = ButtonText, color = PrimaryFixed)
            }
        }
    }
}
