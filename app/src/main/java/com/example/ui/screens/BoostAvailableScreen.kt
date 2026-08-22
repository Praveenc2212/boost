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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GameCardView
import com.example.ui.components.GlassPanel
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.EmeraldLanBg
import com.example.ui.theme.EmeraldLanBorder
import com.example.ui.theme.EmeraldLanGreen
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassFillMedium

@Composable
fun BoostAvailableScreen(
    winningWord: String,
    myCards: List<String>,
    onTriggerBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boost_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(24.dp, CircleShape, spotColor = EmeraldLanGreen.copy(alpha = 0.6f))
                .clip(CircleShape)
                .background(EmeraldLanBg)
                .border(2.dp, EmeraldLanGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = EmeraldLanGreen,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "3 OF A KIND FORMED!",
            color = EmeraldLanGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "BOOST UNLOCKED",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3 Matched Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cardsToShow = if (myCards.size >= 3) myCards.take(3) else listOf(winningWord, winningWord, winningWord)
            cardsToShow.forEachIndexed { index, cardWord ->
                GameCardView(
                    word = cardWord,
                    isSelected = true,
                    onClick = {},
                    enabled = false,
                    cardIndex = index
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Explanation Card
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            borderColor = EmeraldLanBorder,
            backgroundColor = FrostedGlassFillMedium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REACTION SPEED CHALLENGE",
                    color = EmeraldLanGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "When you press the BOOST button, all players' screens will instantly trigger a lightning-fast reaction race! You will get 1st Place points, and everyone else races to tap.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Giant Glowing Trigger BOOST Button
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .fillMaxWidth()
        ) {
            NeonPrimaryButton(
                text = "⚡ TRIGGER BOOST NOW! ⚡",
                onClick = onTriggerBoost,
                icon = Icons.Default.Bolt,
                gradientColors = listOf(EmeraldLanGreen, Color(0xFF059669)),
                testTag = "trigger_boost_button"
            )
        }
    }
}
