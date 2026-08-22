package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceDeepNavy
import kotlin.math.sin

@Composable
fun DeckShuffleAnimation(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shuffle")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow aura
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonPurplePrimary.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                // 3 Animated Swapping Cards
                val cardColors = listOf(
                    listOf(Color(0xFF6B21A8), NeonPurplePrimary),
                    listOf(Color(0xFF0E7490), NeonCyan),
                    listOf(Color(0xFFB45309), CyberGold)
                )

                for (i in 0..2) {
                    val angleOffset = i * 2.094f
                    val offsetX = (sin(phase + angleOffset) * 45f).dp
                    val rotZ = (sin(phase + angleOffset) * 18f)

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX)
                            .rotate(rotZ)
                            .width(80.dp)
                            .height(115.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(cardColors[i])
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SHUFFLING CARDS...",
                color = CyberGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Balancing secret words & dealing 3 cards each",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
