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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.CyberGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurplePrimary
import kotlin.random.Random

private data class ConfettiPiece(
    val initialX: Float,
    val initialY: Float,
    val speedY: Float,
    val speedX: Float,
    val width: Float,
    val height: Float,
    val rotationSpeed: Float,
    val color: Color
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    count: Int = 70
) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    val pieces = remember {
        val colors = listOf(CyberGold, NeonCyan, NeonPurplePrimary, NeonPink, NeonGreen, Color.White)
        List(count) {
            ConfettiPiece(
                initialX = Random.nextFloat(),
                initialY = -Random.nextFloat() * 0.4f,
                speedY = Random.nextFloat() * 1.2f + 0.8f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                width = Random.nextFloat() * 12f + 8f,
                height = Random.nextFloat() * 7f + 5f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = colors.random()
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        pieces.forEach { piece ->
            val curY = ((piece.initialY + progress.value * piece.speedY) % 1.4f) * h
            val curX = (piece.initialX + progress.value * piece.speedX).coerceIn(0f, 1f) * w
            val rot = progress.value * piece.rotationSpeed

            if (curY >= 0 && curY <= h) {
                rotate(rot, pivot = Offset(curX, curY)) {
                    drawRect(
                        color = piece.color,
                        topLeft = Offset(curX - piece.width / 2, curY - piece.height / 2),
                        size = Size(piece.width, piece.height)
                    )
                }
            }
        }
    }
}
