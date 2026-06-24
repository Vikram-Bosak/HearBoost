package com.hearboost.ui.screens

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hearboost.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingMicScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit
) {
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(micPermission.status.isGranted) {
        if (micPermission.status.isGranted) onAllow()
    }

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
                    .padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SurfaceVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SurfaceVariant, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                        .background(PrimaryContainer, CircleShape)
                )
            }

            // Hero icon
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.1f)
                        .background(PrimaryContainer, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(SurfaceContainerHigh, CircleShape)
                        .padding(0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = PrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "Microphone Access",
                style = HeadlineMedium,
                color = OnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "HearBoost needs your microphone to capture nearby speech. Your audio is processed entirely on your device. Nothing is recorded.",
                style = BodyLarge,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Footer buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { micPermission.launchPermissionRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.full,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimaryContainer
                )
            ) {
                Text("Allow Microphone", style = ButtonText)
            }

            TextButton(onClick = onSkip) {
                Text(
                    "Skip for now",
                    style = LabelLarge,
                    color = PrimaryFixed
                )
            }
        }
    }
}
