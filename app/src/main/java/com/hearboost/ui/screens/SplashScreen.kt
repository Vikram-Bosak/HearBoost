package com.hearboost.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hearboost.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }

    // Animate progress bar
    LaunchedEffect(Unit) {
        animate(0f, 1f, animationSpec = tween(durationMillis = 2500)) { value, _ ->
            progress = value
        }
        delay(200)
        onFinished()
    }

    // Pulse animation for sound arcs
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with animated sound arcs
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer arc
                Box(
                    modifier = Modifier
                        .size((pulseScale * 160).dp)
                        .alpha(pulseAlpha * 0.3f)
                        .background(Color.Transparent, CircleShape)
                        .padding(0.dp)
                )
                // Middle arc
                Box(
                    modifier = Modifier
                        .size((pulseScale * 130).dp)
                        .alpha(pulseAlpha * 0.5f)
                        .background(Color.Transparent, CircleShape)
                )
                // Inner arc
                Box(
                    modifier = Modifier
                        .size((pulseScale * 100).dp)
                        .alpha(pulseAlpha * 0.8f)
                        .background(Color.Transparent, CircleShape)
                )
                // Central icon
                Icon(
                    imageVector = Icons.Filled.Hearing,
                    contentDescription = "HearBoost",
                    modifier = Modifier.size(72.dp),
                    tint = PrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = "HearBoost",
                style = HeadlineLarge,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Hear Every Word, Clearly",
                style = BodyMedium,
                color = Color(0xFF7A9AB5),
                textAlign = TextAlign.Center
            )
        }

        // Progress bar at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .fillMaxWidth(0.8f)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = PrimaryContainer,
                trackColor = ActiveBorder
            )
        }
    }
}
