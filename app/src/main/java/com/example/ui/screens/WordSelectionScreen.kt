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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.network.GameEngine
import com.example.ui.components.GlassPanel
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.CrimsonError
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSelectionScreen(
    wordInput: String,
    validationError: String?,
    isSubmitted: Boolean,
    players: List<Player>,
    localPlayer: Player,
    onWordChanged: (String) -> Unit,
    onWordChipSelected: (String) -> Unit,
    onSubmitWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isValid = wordInput.length in 3..12 && validationError == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "CHOOSE SECRET WORD",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Everyone enters one unique word (3-12 letters). Each word appears ×3 in the deck!",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isSubmitted) {
            // Word Input Card
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                borderColor = if (validationError != null) CrimsonError else FrostedGlassBorder
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR WORD",
                        color = AmberGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = wordInput,
                        onValueChange = onWordChanged,
                        placeholder = {
                            Text(
                                "e.g. TIGER",
                                color = Color.Gray,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        textStyle = TextStyle(
                            color = AmberGlow,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberGlow,
                            unfocusedBorderColor = FrostedGlassBorder,
                            cursorColor = AmberGlow
                        ),
                        modifier = Modifier
                            .testTag("word_input_field")
                            .fillMaxWidth()
                    )

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = validationError,
                            color = CrimsonError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Suggestion Chips
                    Text(
                        text = "QUICK SUGGESTIONS",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GameEngine.WORD_SUGGESTIONS.take(10).forEach { suggestion ->
                            val isSelected = wordInput.equals(suggestion, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) AmberGlow else FrostedGlassFill)
                                    .border(
                                        1.dp,
                                        if (isSelected) AmberGlow else FrostedGlassBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onWordChipSelected(suggestion) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            NeonPrimaryButton(
                text = "SUBMIT WORD",
                onClick = onSubmitWord,
                enabled = isValid,
                icon = Icons.Default.Check,
                gradientColors = listOf(AmberGlow, OrangeFlame),
                testTag = "submit_word_button"
            )
        } else {
            // Already submitted state
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                borderColor = EmeraldLanGreen.copy(alpha = 0.6f),
                backgroundColor = EmeraldLanBg
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Submitted",
                        tint = EmeraldLanGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "WORD SUBMITTED!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wordInput.uppercase(),
                        color = AmberGlow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Waiting for other players to submit their secret words...",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress List: Who has submitted
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "PLAYER SUBMISSION STATUS",
                    color = PurpleHeaderGradStart,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                players.forEach { player ->
                    val isDone = player.hasSubmittedWord || (player.id == localPlayer.id && isSubmitted)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isDone) EmeraldLanGreen else AmberGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (player.id == localPlayer.id) "${player.name} (You)" else player.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = if (isDone) "READY" else "CHOOSING...",
                            color = if (isDone) EmeraldLanGreen else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
