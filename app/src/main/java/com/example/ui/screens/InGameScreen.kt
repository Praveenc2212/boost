package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.GamePlayState
import com.example.model.Player
import com.example.ui.components.GameCardView
import com.example.ui.components.GlassPanel
import com.example.ui.components.LanStatusPill
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.PlayerRingView
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.FrostedAvatarBg
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium
import com.example.ui.theme.IndigoHeaderGradEnd
import com.example.ui.theme.PurpleHeaderGradStart

@Composable
fun InGameScreen(
    players: List<Player>,
    localPlayer: Player,
    playState: GamePlayState,
    myCards: List<String>,
    selectedCardIndex: Int?,
    onSelectCard: (Int) -> Unit,
    onPassCard: () -> Unit,
    onLeaveGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuitDialog by remember { mutableStateOf(false) }

    val isMyTurn = playState.turnPlayerId == localPlayer.id
    val targetPlayer = players.firstOrNull { it.id == playState.targetPlayerId }?.name ?: "Left Neighbour"

    val infiniteTransition = rememberInfiniteTransition(label = "turn_pulse")
    val bannerScale by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Game Header Bar (matching HTML: LAN Connected, BOOST gradient title, quit button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanStatusPill(connected = true)

            // Centered Title
            Text(
                text = "BOOST",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.5).sp,
                color = PurpleHeaderGradStart
            )

            // Frosted Circular Close Button
            IconButton(
                onClick = { showQuitDialog = true },
                modifier = Modifier
                    .testTag("game_quit_button")
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quit",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Round indicator label (matching HTML: text-[11px] font-bold text-white/40 uppercase tracking-[0.2em])
        Text(
            text = "ROUND ${playState.roundNumber} • ${if (playState.isFirstRotationComplete) "WIN ACTIVE" else "ROTATION 1"}",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Turn Status Glass Banner (matching HTML: bg-gradient-to-r from-amber-500/20 to-orange-500/20 border border-amber-500/40)
        if (isMyTurn) {
            Box(
                modifier = Modifier
                    .scale(bannerScale)
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0x33F59E0B))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0x33F59E0B),
                                Color(0x33F97316)
                            )
                        )
                    )
                    .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚡",
                            color = AmberGlow,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOUR TURN",
                            color = Color(0xFFFEF3C7),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Pass one card to ",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = targetPlayer.uppercase(),
                            color = AmberTextLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                borderColor = FrostedGlassBorder,
                backgroundColor = FrostedGlassFill
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = IndigoHeaderGradEnd,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Waiting for ${playState.turnPlayerName}...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Players Circular Ring View
        PlayerRingView(
            players = players,
            currentTurnPlayerId = playState.turnPlayerId
        )

        // Center card passing alert if available
        if (playState.lastPassedFromPlayer != null && playState.lastPassedToPlayer != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(FrostedGlassFill)
                    .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🔄 ${playState.lastPassedFromPlayer} passed a card to ${playState.lastPassedToPlayer}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hand Section Header (matching HTML: Your Hand (3) | Select a card)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "YOUR HAND (${myCards.size})",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isMyTurn) (if (selectedCardIndex != null) "Card selected" else "Select a card") else "Waiting",
                    color = if (selectedCardIndex != null) AmberTextLight else Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            myCards.forEachIndexed { index, cardWord ->
                GameCardView(
                    word = cardWord,
                    isSelected = selectedCardIndex == index,
                    onClick = {
                        if (isMyTurn) {
                            onSelectCard(index)
                        }
                    },
                    enabled = isMyTurn,
                    cardIndex = index
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Action Button: Pass Selected Card (matching HTML: Pass Selected Card)
        val canPass = isMyTurn && selectedCardIndex != null
        NeonPrimaryButton(
            text = if (isMyTurn) {
                if (selectedCardIndex != null) "PASS SELECTED CARD"
                else "SELECT A CARD TO PASS"
            } else {
                "WAITING FOR YOUR TURN..."
            },
            onClick = onPassCard,
            enabled = canPass,
            icon = Icons.Default.Send,
            testTag = "pass_card_button"
        )
    }

    // Quit match dialog
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Leave Match?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to exit to lobby?", color = Color(0xFF94A3B8)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuitDialog = false
                        onLeaveGame()
                    }
                ) {
                    Text("LEAVE", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = FrostedAvatarBg
        )
    }
}

