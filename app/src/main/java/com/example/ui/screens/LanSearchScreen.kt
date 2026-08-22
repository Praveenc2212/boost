package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiscoveredRoom
import com.example.ui.components.GlassPanel
import com.example.ui.components.RadarScanner
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
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
fun LanSearchScreen(
    discoveredRooms: List<DiscoveredRoom>,
    onJoinRoom: (DiscoveredRoom) -> Unit,
    onDirectIpJoin: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManualIpDialog by remember { mutableStateOf(false) }
    var manualIpText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .testTag("search_back_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "LAN GAMES",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            IconButton(
                onClick = { showManualIpDialog = true },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Direct IP",
                    tint = AmberGlow
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Radar Scanner
        RadarScanner(size = 140.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Searching nearby games on Wi-Fi...",
            color = AmberTextLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Rooms List or Empty State
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (discoveredRooms.isEmpty()) {
                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📡",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No games found yet",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ask a friend to tap 'Create Room' on the same Wi-Fi network.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(discoveredRooms, key = { it.hostIp + it.roomCode }) { room ->
                        RoomCardItem(
                            room = room,
                            onJoin = { onJoinRoom(room) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom Wi-Fi notice panel
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PurpleHeaderGradStart,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Make sure all players are connected to the same Wi-Fi network.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }

    // Manual Direct IP fallback dialog
    if (showManualIpDialog) {
        AlertDialog(
            onDismissRequest = { showManualIpDialog = false },
            title = { Text("Direct IP Connection", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "If Wi-Fi multicast is restricted by your router, enter the host's local IP address:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualIpText,
                        onValueChange = { manualIpText = it },
                        placeholder = { Text("192.168.1.X", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGlow,
                            unfocusedBorderColor = FrostedGlassBorder,
                            focusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (manualIpText.isNotBlank()) {
                            onDirectIpJoin(manualIpText.trim())
                            showManualIpDialog = false
                        }
                    }
                ) {
                    Text("CONNECT", color = AmberGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualIpDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = FrostedAvatarBg
        )
    }
}

@Composable
private fun RoomCardItem(
    room: DiscoveredRoom,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = FrostedGlassBorder,
        backgroundColor = FrostedGlassFill
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Crown icon for host avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FrostedAvatarBg)
                        .border(1.5.dp, AmberGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Host Crown",
                        tint = AmberGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = room.roomName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Host: ${room.hostName}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Player count tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FrostedGlassFillMedium)
                                .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${room.currentPlayers}/${room.maxPlayers}",
                                color = AmberTextLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Ping and Join Button
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${room.pingMs}ms",
                    color = EmeraldLanGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .testTag("join_room_item_button")
                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = AmberGlow.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(listOf(AmberGlow, OrangeFlame))
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onJoin() }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JOIN",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

