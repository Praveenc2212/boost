package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import com.example.model.Player
import com.example.ui.components.GlassPanel
import com.example.ui.components.LanStatusPill
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.NeonSecondaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
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
fun HomeScreen(
    localPlayer: Player,
    onPlayerNameChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onQuickSoloMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(localPlayer.name) }

    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanStatusPill(connected = true)

            // Player Profile Frosted Glass Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(FrostedGlassFill)
                    .border(1.dp, FrostedGlassBorder, RoundedCornerShape(20.dp))
                    .clickable {
                        tempName = localPlayer.name
                        showNameDialog = true
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(FrostedAvatarBg)
                            .border(1.dp, IndigoBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localPlayer.name.take(1).uppercase(),
                            color = AmberGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localPlayer.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = AmberGlow,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Center Hero Logo (Frosted Glass Orb)
        Box(
            modifier = Modifier
                .scale(logoScale)
                .size(112.dp)
                .shadow(30.dp, RoundedCornerShape(36.dp), spotColor = PurpleHeaderGradStart.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(36.dp))
                .background(FrostedGlassFillMedium)
                .border(
                    width = 1.5.dp,
                    color = FrostedGlassBorderBright,
                    shape = RoundedCornerShape(36.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // BOOST Title
        Text(
            text = "BOOST",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 2.sp,
            color = PurpleHeaderGradStart
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "One Card. One Move. Match Three to Win.",
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Two Main Action Buttons (Matching Frosted Glass Theme)
        NeonPrimaryButton(
            text = "CREATE ROOM",
            onClick = onCreateRoom,
            icon = Icons.Default.Add,
            gradientColors = listOf(AmberGlow, OrangeFlame),
            testTag = "create_room_button"
        )

        Spacer(modifier = Modifier.height(14.dp))

        NeonSecondaryButton(
            text = "JOIN ROOM",
            onClick = onJoinRoom,
            icon = Icons.Default.Search,
            gradientColors = listOf(Color(0x336366F1), Color(0x334F46E5)),
            testTag = "join_room_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Solo / Bot practice quick match
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(FrostedGlassFill)
                .border(1.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                .clickable { onQuickSoloMatch() }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = AmberGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Solo Practice vs AI Bots",
                    color = AmberTextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // How To Play Panel (4 frosted tiles)
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HOW TO PLAY",
                        color = AmberGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .weight(1f)
                            .background(FrostedGlassBorder)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RuleTile(
                        icon = "🎴",
                        title = "Get 3 Cards",
                        subtitle = "Secret words deck",
                        modifier = Modifier.weight(1f)
                    )
                    RuleTile(
                        icon = "🔄",
                        title = "Pass Left",
                        subtitle = "1 card to neighbor",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RuleTile(
                        icon = "⏳",
                        title = "Play in Turns",
                        subtitle = "A → B → C → D",
                        modifier = Modifier.weight(1f)
                    )
                    RuleTile(
                        icon = "🏆",
                        title = "Match & Win",
                        subtitle = "3 identical words",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Name change dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = {
                Text(
                    text = "Player Alias",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your display name for LAN matches:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it.take(14) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGlow,
                            unfocusedBorderColor = FrostedGlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            onPlayerNameChange(tempName)
                        }
                        showNameDialog = false
                    }
                ) {
                    Text("SAVE", color = AmberGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = FrostedAvatarBg
        )
    }
}

@Composable
private fun RuleTile(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FrostedGlassFill)
            .border(1.dp, FrostedGlassBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}
