package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
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
import com.example.ui.theme.IndigoBorder
import com.example.ui.theme.IndigoHeaderGradEnd
import com.example.ui.theme.IndigoText
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PurpleHeaderGradStart

@Composable
fun LobbyScreen(
    roomName: String,
    roomCode: String,
    players: List<Player>,
    localPlayer: Player,
    isHost: Boolean,
    onToggleReady: () -> Unit,
    onStartGame: () -> Unit,
    onAddBot: () -> Unit,
    onRemovePlayer: (String) -> Unit,
    onLeaveRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allReady = players.size >= 2 && players.all { it.isReady }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header row with Back / Leave
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onLeaveRoom,
                modifier = Modifier
                    .testTag("lobby_leave_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Leave",
                    tint = Color.White
                )
            }

            Text(
                text = "MATCH LOBBY",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            // Player count pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${players.size}/6 Players",
                    color = AmberTextLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top Glass Panel: Room info
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            borderColor = FrostedGlassBorder,
            backgroundColor = FrostedGlassFill
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = roomName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val host = players.firstOrNull { it.isHost }?.name ?: "Host"
                        Text(
                            text = "Hosted by $host",
                            color = AmberGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Room Code badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FrostedGlassFillMedium)
                            .border(1.dp, FrostedGlassBorderBright, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = roomCode,
                            color = AmberGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Middle: Connected Players Grid
        Text(
            text = "CONNECTED PLAYERS",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(players, key = { it.id }) { player ->
                LobbyPlayerCard(
                    player = player,
                    isLocalPlayer = player.id == localPlayer.id,
                    canKick = isHost && !player.isHost,
                    onKick = { onRemovePlayer(player.id) }
                )
            }

            // Host can add Bot button if room not full
            if (isHost && players.size < 6) {
                item {
                    Box(
                        modifier = Modifier
                            .height(130.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(FrostedGlassFill)
                            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(18.dp))
                            .clickable { onAddBot() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FrostedAvatarBg)
                                    .border(1.dp, IndigoBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Bot",
                                    tint = PurpleHeaderGradStart,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "+ Add Bot",
                                color = PurpleHeaderGradStart,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Controls
        if (isHost) {
            NeonPrimaryButton(
                text = if (allReady) "START GAME" else "WAITING FOR PLAYERS (READY)",
                onClick = onStartGame,
                enabled = allReady,
                icon = Icons.Default.PlayArrow,
                gradientColors = listOf(AmberGlow, OrangeFlame),
                testTag = "start_game_button"
            )
            if (!allReady) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (players.size < 2) "Need at least 2 players to start" else "All players must be Ready",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Client Ready Button
            val isReady = localPlayer.isReady
            NeonSecondaryButton(
                text = if (isReady) "READY ✓ (TAP TO UNREADY)" else "TAP TO READY",
                onClick = onToggleReady,
                gradientColors = if (isReady) listOf(Color(0x3310B981), Color(0x33059669)) else listOf(Color(0x336366F1), Color(0x334F46E5)),
                testTag = "ready_button"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Waiting for host to start the game...",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LobbyPlayerCard(
    player: Player,
    isLocalPlayer: Boolean,
    canKick: Boolean,
    onKick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPanel(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(18.dp),
        borderColor = if (player.isReady) EmeraldLanGreen.copy(alpha = 0.5f) else FrostedGlassBorder,
        backgroundColor = if (player.isReady) EmeraldLanBg else FrostedGlassFill
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (canKick) {
                IconButton(
                    onClick = onKick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kick",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FrostedAvatarBg)
                            .border(
                                1.5.dp,
                                if (player.isReady) EmeraldLanGreen else (if (player.isHost) AmberGlow else IndigoBorder),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.name.take(1).uppercase(),
                            color = if (player.isHost) AmberGlow else (if (player.isReady) EmeraldLanGreen else IndigoText),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (player.isHost) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AmberGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isLocalPlayer) "${player.name} (You)" else player.name,
                    color = if (isLocalPlayer) AmberTextLight else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Ready status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (player.isReady) EmeraldLanBg else FrostedGlassFillMedium)
                        .border(0.8.dp, if (player.isReady) EmeraldLanBorder else FrostedGlassBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (player.isReady) "READY" else "WAITING",
                        color = if (player.isReady) EmeraldLanGreen else Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

