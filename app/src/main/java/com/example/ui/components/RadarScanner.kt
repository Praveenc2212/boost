package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurplePrimary

@Composable
fun RadarScanner(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val angle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    val pulse = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f

            // Background circle
            drawCircle(
                color = Color(0x1A00E5FF),
                radius = maxRadius,
                center = center
            )

            // Concentric range rings
            drawCircle(
                color = Color(0x3300E5FF),
                radius = maxRadius * 0.33f,
                center = center,
                style = Stroke(width = 1.2f)
            )
            drawCircle(
                color = Color(0x3300E5FF),
                radius = maxRadius * 0.66f,
                center = center,
                style = Stroke(width = 1.2f)
            )
            drawCircle(
                color = Color(0x5500E5FF),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Crosshairs
            drawLine(
                color = Color(0x2200E5FF),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0x2200E5FF),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )

            // Pulse wave
            drawCircle(
                color = NeonCyan.copy(alpha = (1f - pulse.value) * 0.4f),
                radius = maxRadius * pulse.value,
                center = center,
                style = Stroke(width = 2f)
            )

            // Rotating sweep radar cone
            rotate(angle.value, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1.0f to NeonCyan.copy(alpha = 0.5f),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                    size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
                )

                // Beam line
                drawLine(
                    color = NeonCyan,
                    start = center,
                    end = Offset(center.x + maxRadius, center.y),
                    strokeWidth = 2.5f
                )
            }

            // Center beacon dot
            drawCircle(
                color = NeonPurplePrimary,
                radius = 5f,
                center = center
            )
        }
    }
}
