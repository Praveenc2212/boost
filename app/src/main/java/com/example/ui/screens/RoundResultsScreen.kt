package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.PlayerRoundRank
import com.example.ui.components.GlassPanel
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.NeonSecondaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.EmeraldLanBg
import com.example.ui.theme.EmeraldLanBorder
import com.example.ui.theme.EmeraldLanGreen
import com.example.ui.theme.FrostedAvatarBg
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderBright
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium
import com.example.ui.theme.IndigoHeaderGradEnd
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleHeaderGradStart

@Composable
fun RoundResultsScreen(
    roundNumber: Int,
    boosterName: String,
    results: List<PlayerRoundRank>,
    localPlayer: Player,
    isHost: Boolean,
    onNextRound: () -> Unit,
    onFinishMatch: () -> Unit,
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Round Finished Badge
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(20.dp, CircleShape, spotColor = AmberGlow.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(FrostedGlassFillMedium)
                .border(2.dp, AmberGlow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Round Result",
                tint = AmberGlow,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ROUND $roundNumber COMPLETED",
            color = AmberGlow,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "LEADERBOARD",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Booster: $boosterName (Auto Rank #1)",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Ranks Cards List
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            borderColor = FrostedGlassBorderBright,
            backgroundColor = FrostedGlassFillMedium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                results.forEachIndexed { index, rankItem ->
                    val isLocal = rankItem.playerId == localPlayer.id
                    val isTop1 = rankItem.rank == 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isLocal) Color(0x33F59E0B)
                                else if (isTop1) Color(0x2210B981)
                                else FrostedGlassFill
                            )
                            .border(
                                1.dp,
                                if (isLocal) AmberGlow.copy(alpha = 0.5f)
                                else if (isTop1) EmeraldLanGreen.copy(alpha = 0.4f)
                                else FrostedGlassBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Number Box
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (rankItem.rank) {
                                            1 -> AmberGlow
                                            2 -> Color(0xFF94A3B8)
                                            3 -> Color(0xFFB45309)
                                            else -> FrostedGlassFill
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${rankItem.rank}",
                                    color = if (rankItem.rank <= 3) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isLocal) "${rankItem.playerName} (You)" else rankItem.playerName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (rankItem.isBooster) {
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
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                if (rankItem.reactionTimeMs != null) {
                                    Text(
                                        text = "Reaction: +${rankItem.reactionTimeMs}ms",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Score & Points
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${rankItem.pointsAwarded} PTS",
                                color = EmeraldLanGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Total: ${rankItem.cumulativeScore}",
                                color = AmberTextLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Next Round Action Button
        NeonPrimaryButton(
            text = if (isHost) "START NEXT ROUND" else "READY FOR NEXT ROUND",
            onClick = onNextRound,
            icon = Icons.Default.FastForward,
            gradientColors = listOf(AmberGlow, OrangeFlame),
            testTag = "next_round_button"
        )

        if (isHost) {
            Spacer(modifier = Modifier.height(12.dp))

            NeonSecondaryButton(
                text = "FINISH MATCH & SHOW CHAMPION",
                onClick = onFinishMatch,
                icon = Icons.Default.EmojiEvents,
                gradientColors = listOf(Color(0x3310B981), Color(0x33059669)),
                testTag = "finish_match_button"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        NeonSecondaryButton(
            text = "RETURN TO LOBBY",
            onClick = onReturnToLobby,
            icon = Icons.Default.Home,
            gradientColors = listOf(Color(0x336366F1), Color(0x334F46E5)),
            testTag = "results_return_lobby_button"
        )
    }
}
