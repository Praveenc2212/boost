package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.FrostedAvatarBg
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium
import com.example.ui.theme.IndigoBorder
import com.example.ui.theme.IndigoText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerRingView(
    players: List<Player>,
    currentTurnPlayerId: String,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 4
    ) {
        players.forEachIndexed { index, player ->
            val isCurrentTurn = player.id == currentTurnPlayerId
            PlayerAvatarBadge(
                player = player,
                isCurrentTurn = isCurrentTurn,
                seatIndex = index + 1
            )
        }
    }
}

@Composable
fun PlayerAvatarBadge(
    player: Player,
    isCurrentTurn: Boolean,
    seatIndex: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCurrentTurn) AmberGlow else IndigoBorder,
        animationSpec = tween(300),
        label = "border_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 6.dp)
            .width(76.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
        ) {
            // Glow backdrop on active turn
            if (isCurrentTurn) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AmberGlow.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
            }

            // Avatar circle (matching HTML: w-12 h-12 rounded-full bg-[#0A0B1E])
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (isCurrentTurn) {
                            Modifier.shadow(12.dp, CircleShape, spotColor = AmberGlow.copy(alpha = 0.6f))
                        } else Modifier
                    )
                    .clip(CircleShape)
                    .background(FrostedAvatarBg)
                    .border(
                        width = if (isCurrentTurn) 2.dp else 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.take(1).uppercase(),
                    color = if (isCurrentTurn) AmberGlow else IndigoText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Host star badge
            if (player.isHost) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AmberGlow)
                        .border(1.dp, FrostedAvatarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Host",
                        tint = Color.Black,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Bot badge
            if (player.isBot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(FrostedGlassFillMedium)
                        .border(0.5.dp, FrostedGlassBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("AI", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Player Name (matching HTML: YOU in amber-200 or name in white/50)
        Text(
            text = player.name.uppercase(),
            color = if (isCurrentTurn) AmberTextLight else Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Cards count badge
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(FrostedGlassFill)
                .border(0.8.dp, if (isCurrentTurn) AmberGlow.copy(alpha = 0.4f) else FrostedGlassBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                text = "${player.cardCount} cards",
                color = if (isCurrentTurn) AmberGlow else Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

