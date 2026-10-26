package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.data.repository.GlassThemeMode
import com.example.domain.engine.InningsLiveState
import com.example.ui.theme.AccentExtra
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LiquidGlassTheme

/**
 * iOS-style Liquid Glass Canvas background with subtle ambient mesh gradients.
 * Translucent frosted cards placed over this background catch ambient lighting.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens

    // Ambient floating light transition
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_ambient")
    val pulseOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.backgroundBase)
    ) {
        // Draw fluid ambient light orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-right ambient orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.ambientMesh2, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f + pulseOffset),
                    radius = w * 0.7f
                )
            )

            // Center-left ambient orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.ambientMesh1, Color.Transparent),
                    center = Offset(w * 0.1f, h * 0.5f - pulseOffset),
                    radius = w * 0.65f
                )
            )

            // Bottom-right ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tokens.ambientMesh3, Color.Transparent),
                    center = Offset(w * 0.7f, h * 0.85f),
                    radius = w * 0.6f
                )
            )
        }

        content()
    }
}

/**
 * Premium iOS Liquid Glass Card with specular refraction, frosted fill, and smooth press interaction.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val effectiveBg = backgroundColor ?: tokens.glassCardBackground
    val effectiveBorder = borderColor ?: tokens.glassCardBorder

    Box(
        modifier = modifier
            .shadow(
                elevation = if (tokens.isDark) 8.dp else 12.dp,
                shape = shape,
                ambientColor = tokens.cardShadowColor,
                spotColor = tokens.cardShadowColor
            )
            .clip(shape)
            .background(effectiveBg)
            .border(borderWidth, effectiveBorder, shape)
    ) {
        // Top specular reflection highlight (characteristic of iOS liquid glass)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(tokens.glassSpecularTop, Color.Transparent),
                        startY = 0f,
                        endY = 50f
                    )
                )
        )

        content()
    }
}

/**
 * Clickable Liquid Glass Card with fluid spring bounce animation.
 */
@Composable
fun InteractiveGlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    GlassCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = shape,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        content = content
    )
}

/**
 * iOS-style Glossy Metallic Gold Liquid Button with tactile spring feedback.
 */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val tokens = LiquidGlassTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "btn_scale"
    )

    val buttonBrush = if (enabled) {
        Brush.horizontalGradient(
            listOf(tokens.accentGoldBright, tokens.accentGold, tokens.accentGoldDark)
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color(0x44D4AF37), Color(0x33A67C1E))
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x55D4AF37),
                spotColor = Color(0x55D4AF37)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(buttonBrush)
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) (if (tokens.isDark) CharcoalBlack else Color.White) else Color(0x88FFFFFF),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * iOS-style Outlined Liquid Frosted Glass Button.
 */
@Composable
fun OutlinedGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val tokens = LiquidGlassTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "out_btn_scale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(tokens.glassCardBackground),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = tokens.accentGold
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(tokens.accentGoldBright, tokens.accentGold)),
            width = 1.5.dp
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = tokens.accentGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Compact iOS Liquid Glass Theme Toggle Pill (Sun / Moon).
 */
@Composable
fun LiquidGlassThemeToggle(
    currentMode: GlassThemeMode,
    onToggle: (GlassThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tokens.glassCardElevated)
            .border(1.dp, tokens.glassCardBorder, RoundedCornerShape(20.dp))
            .padding(3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dark Mode Option
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (tokens.isDark) tokens.accentGold else Color.Transparent)
                    .clickable { onToggle(GlassThemeMode.DARK) }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Liquid Glass",
                    tint = if (tokens.isDark) CharcoalBlack else tokens.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // White Mode Option
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (!tokens.isDark) tokens.accentGold else Color.Transparent)
                    .clickable { onToggle(GlassThemeMode.WHITE) }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "White Liquid Glass",
                    tint = if (!tokens.isDark) Color.White else tokens.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DeliveryChip(
    delivery: BallDeliveryEntity,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens

    val (bg, text, label) = when {
        delivery.isWicket -> Triple(AccentWicket, Color.White, "W")
        delivery.runsOffBat == 6 -> Triple(AccentSix, Color.White, "6")
        delivery.runsOffBat == 4 -> Triple(AccentFour, Color.White, "4")
        delivery.extrasType == ExtrasType.WIDE -> Triple(AccentExtra, CharcoalBlack, "${delivery.extrasRuns}wd")
        delivery.extrasType == ExtrasType.NO_BALL -> Triple(AccentExtra, CharcoalBlack, "${delivery.totalBallRuns}nb")
        delivery.runsOffBat == 0 && delivery.extrasType == ExtrasType.NONE -> Triple(
            if (tokens.isDark) Color(0xFF222330) else Color(0xFFE2E8F0),
            tokens.textSecondary,
            "•"
        )
        else -> Triple(
            if (tokens.isDark) Color(0xFF2E3042) else Color(0xFFCBD5E1),
            if (tokens.isDark) Color.White else Color(0xFF0F172A),
            "${delivery.totalBallRuns}"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = text,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun ScoreboardOverlayComposable(
    state: InningsLiveState,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        backgroundColor = tokens.glassCardElevated,
        borderColor = tokens.accentGold,
        borderWidth = 1.5.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Batting team, Score, Overs & CRR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Gold accent bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(30.dp)
                            .background(tokens.accentGold, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = state.battingTeam.ifEmpty { "Rajpoot Cricket Boys" }.uppercase(),
                            color = tokens.accentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${state.totalRuns}/${state.totalWickets}",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${state.formattedOvers} ov)",
                                color = tokens.textSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                // Rates column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CRR: ${state.formattedCRR}",
                        color = tokens.textGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (state.target != null) {
                        Text(
                            text = "Target: ${state.target} (Req: ${state.runsRequired ?: 0})",
                            color = AccentExtra,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Batters & Bowler row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Striker & Non-striker
                Column(modifier = Modifier.weight(1f)) {
                    val striker = state.currentStriker
                    Text(
                        text = "★ ${striker?.playerName ?: "Striker"} ${striker?.runs ?: 0}* (${striker?.ballsFaced ?: 0})",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    val nonStriker = state.currentNonStriker
                    Text(
                        text = "   ${nonStriker?.playerName ?: "Non-striker"} ${nonStriker?.runs ?: 0} (${nonStriker?.ballsFaced ?: 0})",
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }

                // Bowler
                val bowler = state.currentBowler
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${bowler?.bowlerName ?: "Bowler"}",
                        color = tokens.accentGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${bowler?.formattedOvers ?: "0.0"}-${bowler?.maidens ?: 0}-${bowler?.runsConceded ?: 0}-${bowler?.wickets ?: 0}",
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Recent Deliveries Bar
            if (state.recentDeliveries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THIS OVER: ",
                        color = tokens.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.recentDeliveries.takeLast(6).forEach { ball ->
                            DeliveryChip(delivery = ball)
                        }
                    }
                }
            }
        }
    }
}
