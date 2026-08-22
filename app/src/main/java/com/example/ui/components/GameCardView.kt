package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberTextLight
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium

@Composable
fun GameCardView(
    word: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cardIndex: Int = 0
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-8).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "card_offset_y"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AmberGlow else FrostedGlassBorder,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "card_border_color"
    )

    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .testTag("game_card_$cardIndex")
            .offset(y = offsetY)
            .scale(scale)
            .width(106.dp)
            .height(152.dp)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 20.dp,
                        shape = shape,
                        spotColor = AmberGlow.copy(alpha = 0.5f),
                        ambientColor = AmberGlow.copy(alpha = 0.3f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(if (isSelected) FrostedGlassFillMedium else FrostedGlassFill)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AmberGlow)
            ) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Selected checkmark badge in top right (matching HTML: absolute top-2 right-2 bg-amber-400 rounded-full)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AmberGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Word text (matching HTML: text-lg font-black tracking-tighter text-center leading-tight)
            Text(
                text = word.uppercase(),
                color = if (isSelected) AmberTextLight else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

