package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.GlassPanel
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.NeonSecondaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.EmeraldLanGreen
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderBright
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium
import com.example.ui.theme.OrangeFlame

@Composable
fun WinnerScreen(
    winnerName: String,
    winnerScore: Int,
    winningWord: String,
    players: List<Player>,
    isHost: Boolean,
    onPlayAgain: () -> Unit,
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophy_glow")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Full screen festive confetti particles
        ConfettiEffect()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy badge (Frosted Glass with Amber Glow)
            Box(
                modifier = Modifier
                    .scale(trophyScale)
                    .size(100.dp)
                    .shadow(30.dp, CircleShape, spotColor = AmberGlow.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(FrostedGlassFillMedium)
                    .border(2.dp, AmberGlow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 46.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MATCH CHAMPION",
                color = AmberGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${winnerName.uppercase()} WINS!",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            if (winnerScore > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Final Score: $winnerScore PTS",
                    color = EmeraldLanGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Final Leaderboard / Stats Card
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                borderColor = FrostedGlassBorderBright,
                backgroundColor = FrostedGlassFillMedium
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL STANDINGS",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val sortedPlayers = players.sortedByDescending { it.totalScore }
                    sortedPlayers.forEachIndexed { index, p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (index == 0) Color(0x33F59E0B) else FrostedGlassFill)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${index + 1}",
                                    color = if (index == 0) AmberGlow else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = p.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "${p.totalScore} PTS",
                                color = if (index == 0) AmberGlow else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Buttons
            NeonPrimaryButton(
                text = "PLAY AGAIN",
                onClick = onPlayAgain,
                icon = Icons.Default.Refresh,
                gradientColors = listOf(AmberGlow, OrangeFlame),
                testTag = "play_again_button"
            )

            Spacer(modifier = Modifier.height(12.dp))

            NeonSecondaryButton(
                text = "RETURN TO LOBBY",
                onClick = onReturnToLobby,
                icon = Icons.Default.Home,
                gradientColors = listOf(Color(0x336366F1), Color(0x334F46E5)),
                testTag = "return_to_lobby_button"
            )
        }
    }
}


