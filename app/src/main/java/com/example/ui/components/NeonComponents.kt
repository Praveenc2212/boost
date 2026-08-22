package com.example.ui.components

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.EmeraldLanBg
import com.example.ui.theme.EmeraldLanBorder
import com.example.ui.theme.EmeraldLanGreen
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.FrostedGlassBorderBright
import com.example.ui.theme.FrostedGlassFill
import com.example.ui.theme.FrostedGlassFillMedium
import com.example.ui.theme.IndigoBorder
import com.example.ui.theme.OrangeFlame

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = FrostedGlassBorder,
    backgroundColor: Color = FrostedGlassFill,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
    ) {
        content()
    }
}

@Composable
fun NeonPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "primary_button",
    gradientColors: List<Color> = listOf(AmberGlow, OrangeFlame)
) {
    val alpha = if (enabled) 1f else 0.4f
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 56.dp)
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (enabled) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = shape,
                        spotColor = OrangeFlame.copy(alpha = 0.5f),
                        ambientColor = AmberGlow.copy(alpha = 0.3f)
                    )
                } else Modifier
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = gradientColors.map { it.copy(alpha = it.alpha * alpha) }
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f * alpha),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.Black)
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = Color.Black.copy(alpha = alpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun NeonSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "secondary_button",
    gradientColors: List<Color> = listOf(Color(0x336366F1), Color(0x334F46E5))
) {
    val alpha = if (enabled) 1f else 0.4f
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 54.dp)
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (enabled) {
                    Modifier.shadow(elevation = 8.dp, shape = shape, spotColor = Color(0x33818CF8))
                } else Modifier
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = gradientColors.map { it.copy(alpha = it.alpha * alpha) }
                )
            )
            .border(
                width = 1.dp,
                color = FrostedGlassBorderBright.copy(alpha = alpha),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White)
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = Color.White.copy(alpha = alpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
fun PulseDot(
    modifier: Modifier = Modifier,
    color: Color = EmeraldLanGreen,
    size: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale = transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.size(size * 1.6f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * scale.value)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun LanStatusPill(
    connected: Boolean,
    modifier: Modifier = Modifier
) {
    // Exact styled matching HTML: bg-emerald-500/10 border border-emerald-500/20 px-3 py-1 rounded-full text-emerald-400
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (connected) EmeraldLanBg else FrostedGlassFill)
            .border(
                1.dp,
                if (connected) EmeraldLanBorder else FrostedGlassBorder,
                CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulseDot(color = if (connected) EmeraldLanGreen else Color.Gray, size = 6.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (connected) "LAN CONNECTED" else "LAN OFFLINE",
                color = if (connected) EmeraldLanGreen else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

