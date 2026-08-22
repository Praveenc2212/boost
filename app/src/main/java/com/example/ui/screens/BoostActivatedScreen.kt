package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoostReactionState
import com.example.model.Player
import com.example.ui.components.GlassPanel
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldLanBg
import com.example.ui.theme.EmeraldLanBorder
import com.example.ui.theme.EmeraldLanGreen
import com.example.ui.theme.FrostedAvatarBg
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium
import com.example.ui.theme.IndigoHeaderGradEnd
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleHeaderGradStart

@Composable
fun BoostActivatedScreen(
    players: List<Player>,
    localPlayer: Player,
    boostReaction: BoostReactionState?,
    hasTapped: Boolean,
    onTapBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boosterName = boostReaction?.boosterPlayerName ?: "Player"
    val winningWord = boostReaction?.winningWord ?: "CARDS"
    val isLocalBooster = boostReaction?.boosterPlayerId == localPlayer.id
    val tappedPlayerIds = boostReaction?.tappedPlayerIds ?: emptyList()

    // Countdown animation
    val progress = remember { Animatable(1f) }
    LaunchedEffect(boostReaction?.startTimeEpochMs) {
        progress.snapTo(1f)
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = ((boostReaction?.durationSeconds ?: 3.8f) * 1000).toInt(),
                easing = LinearEasing
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "boost_alarm")
    val buttonGlow by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0x33EF4444),
                            Color(0x33F59E0B)
                        )
                    )
                )
                .border(1.dp, Color(0x66EF4444), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BOOST ACTIVATED BY ${boosterName.uppercase()}!",
                    color = Color(0xFFFEE2E2),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "MATCHED: $winningWord",
            color = AmberGlow,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Countdown Timer Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "REACTION WINDOW",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${((boostReaction?.durationSeconds ?: 3.8f) * progress.value).toInt() + 1}s",
                    color = if (progress.value < 0.3f) CrimsonError else AmberGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(FrostedGlassFill)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.value)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (progress.value < 0.3f) CrimsonError else EmeraldLanGreen,
                                    AmberGlow
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // GIANT REACTION BOOST BUTTON
        val isButtonActive = !hasTapped && !isLocalBooster

        Box(
            modifier = Modifier
                .scale(if (isButtonActive) buttonGlow else 1f)
                .size(200.dp)
                .shadow(
                    elevation = if (hasTapped || isLocalBooster) 12.dp else 36.dp,
                    shape = CircleShape,
                    spotColor = if (hasTapped || isLocalBooster) EmeraldLanGreen else CrimsonError
                )
                .clip(CircleShape)
                .background(
                    if (hasTapped || isLocalBooster) {
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF10B981),
                                Color(0xFF064E3B)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFEF4444),
                                Color(0xFF991B1B)
                            )
                        )
                    }
                )
                .border(
                    width = 4.dp,
                    color = if (hasTapped || isLocalBooster) EmeraldLanGreen else Color(0xFFFCA5A5),
                    shape = CircleShape
                )
                .clickable(
                    enabled = isButtonActive,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onTapBoost()
                }
                .testTag("reaction_boost_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (hasTapped || isLocalBooster) Icons.Default.CheckCircle else Icons.Default.Bolt,
                    contentDescription = "Boost Tap",
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isLocalBooster) "BOOSTER\n(1ST PLACE)" else if (hasTapped) "RECORDED!\nTAP SAVED" else "TAP BOOST\nNOW!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Live Tapped Leaderboard Tracker
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE REACTION ORDER",
                        color = PurpleHeaderGradStart,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${tappedPlayerIds.size}/${players.size} Tapped",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                players.forEachIndexed { _, player ->
                    val hasPlayerTapped = player.id in tappedPlayerIds
                    val tapRank = if (hasPlayerTapped) tappedPlayerIds.indexOf(player.id) + 1 else null
                    val isBooster = player.id == boostReaction?.boosterPlayerId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasPlayerTapped) EmeraldLanGreen.copy(alpha = 0.2f)
                                        else FrostedGlassFill
                                    )
                                    .border(
                                        1.dp,
                                        if (hasPlayerTapped) EmeraldLanGreen else FrostedGlassBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasPlayerTapped) {
                                    Text(
                                        text = "#$tapRank",
                                        color = EmeraldLanGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (player.id == localPlayer.id) "${player.name} (You)" else player.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isBooster) FontWeight.Black else FontWeight.SemiBold
                            )

                            if (isBooster) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AmberGlow)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "BOOSTER",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (hasPlayerTapped) "TAPPED" else "WAITING...",
                            color = if (hasPlayerTapped) EmeraldLanGreen else Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
