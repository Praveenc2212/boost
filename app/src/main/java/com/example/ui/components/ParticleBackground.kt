package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FrostedCanvas
import com.example.ui.theme.FrostedIndigoAmbient
import com.example.ui.theme.FrostedPurpleAmbient
import kotlin.random.Random

private data class DustParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_transition")
    val animProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val particles = remember {
        val colors = listOf(
            Color(0x66C084FC),
            Color(0x66818CF8),
            Color(0x66FBBF24),
            Color(0x44FFFFFF)
        )
        List(particleCount) {
            DustParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 1f,
                speed = Random.nextFloat() * 0.3f + 0.15f,
                alpha = Random.nextFloat() * 0.35f + 0.15f,
                color = colors.random()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Deep obsidian canvas base
        drawRect(color = FrostedCanvas)

        // Top-left ambient soft purple blur orb (from HTML design: top-[-10%] left-[-10%] bg-purple-900/30)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    FrostedPurpleAmbient,
                    FrostedPurpleAmbient.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(width * 0.1f, height * 0.1f),
                radius = width * 0.85f
            ),
            center = Offset(width * 0.1f, height * 0.1f),
            radius = width * 0.85f
        )

        // Bottom-right ambient soft indigo blur orb (from HTML design: bottom-[-10%] right-[-10%] bg-indigo-900/30)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    FrostedIndigoAmbient,
                    FrostedIndigoAmbient.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(width * 0.9f, height * 0.9f),
                radius = width * 0.85f
            ),
            center = Offset(width * 0.9f, height * 0.9f),
            radius = width * 0.85f
        )

        // Subtle floating ambient dust particles
        particles.forEach { p ->
            val curY = ((p.yRatio + animProgress.value * p.speed) % 1f) * height
            val curX = p.xRatio * width
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(curX, curY)
            )
        }
    }
}

