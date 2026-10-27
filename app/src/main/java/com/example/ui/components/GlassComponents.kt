package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.LiquidGlassTheme
import kotlinx.coroutines.delay

/**
 * One-shot 3D perspective entrance animation modifier for cards and panels.
 * Runs once on composition and rests at 0 CPU/GPU cost afterwards.
 */
fun Modifier.liquid3DEntrance(delayMs: Int = 0): Modifier = composed {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (delayMs > 0) delay(delayMs.toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
        )
    }
    val p = progress.value
    this.graphicsLayer {
        alpha = p
        translationY = (1f - p) * 20f
        rotationX = (1f - p) * 6f
        scaleX = 0.97f + (0.03f * p)
        scaleY = 0.97f + (0.03f * p)
        cameraDistance = 14f * density
    }
}

/**
 * Clean Solid Midnight Navy & Deep Blue Sports App Background.
 * Uses [drawWithCache] so subtle top stadium spotlight gradients are hardware-cached
 * with 0% continuous CPU/GPU usage and no frosted glass blur.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val isDark = tokens.isDark
    val bgBase = tokens.backgroundBase
    val mesh1 = tokens.ambientMesh1
    val mesh2 = tokens.ambientMesh2

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBase)
            .drawWithCache {
                val w = size.width
                val h = size.height

                val topGlowColor = if (isDark) Color(0x2400D8FF) else Color(0x180091EA)
                val topSpotlight = Brush.radialGradient(
                    colors = listOf(topGlowColor, Color.Transparent),
                    center = Offset(w * 0.5f, 0f),
                    radius = w * 0.95f
                )
                val midDepth = Brush.radialGradient(
                    colors = listOf(mesh2.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.22f),
                    radius = w * 0.75f
                )
                val bottomDepth = Brush.radialGradient(
                    colors = listOf(mesh1.copy(alpha = 0.50f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.75f),
                    radius = w * 0.80f
                )

                onDrawBehind {
                    drawCircle(brush = topSpotlight, center = Offset(w * 0.5f, 0f), radius = w * 0.95f)
                    drawCircle(brush = midDepth, center = Offset(w * 0.85f, h * 0.22f), radius = w * 0.75f)
                    drawCircle(brush = bottomDepth, center = Offset(w * 0.15f, h * 0.75f), radius = w * 0.80f)
                }
            }
    ) {
        content()
    }
}

/**
 * Clean, Solid Modern Midnight Navy Card with crisp cyan-blue border and subtle 3D elevation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
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
                elevation = if (tokens.isDark) 8.dp else 4.dp,
                shape = shape,
                ambientColor = tokens.cardShadowColor,
                spotColor = tokens.cardShadowColor
            )
            .clip(shape)
            .background(effectiveBg)
            .border(borderWidth, effectiveBorder, shape)
    ) {
        content()
    }
}

/**
 * Clickable Solid Modern Card with smooth 3D perspective press physics.
 */
@Composable
fun InteractiveGlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_3d_press"
    )

    GlassCard(
        modifier = modifier
            .graphicsLayer {
                val scale = 1f - (0.022f * pressProgress)
                scaleX = scale
                scaleY = scale
                rotationX = pressProgress * 2.0f
                cameraDistance = 12f * density
            }
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
 * Rajpoot Cricket Boys ("RCB") Official Shield Crest Logo with Gold Crown and Cricket Ball
 * matching the reference image.
 */
@Composable
fun RcbCrestLogo(
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .drawWithCache {
                val w = this.size.width
                val h = this.size.height

                val goldRim = Color(0xFFE5C158)
                val goldLight = Color(0xFFF9E295)
                val navyFill = Color(0xFF061326)
                val rimBrush = Brush.verticalGradient(listOf(goldLight, goldRim))

                val crownPath = Path().apply {
                    moveTo(w * 0.28f, h * 0.22f)
                    lineTo(w * 0.24f, h * 0.10f)
                    lineTo(w * 0.38f, h * 0.16f)
                    lineTo(w * 0.50f, h * 0.05f)
                    lineTo(w * 0.62f, h * 0.16f)
                    lineTo(w * 0.76f, h * 0.10f)
                    lineTo(w * 0.72f, h * 0.22f)
                    close()
                }

                val shieldPath = Path().apply {
                    moveTo(w * 0.16f, h * 0.25f)
                    lineTo(w * 0.84f, h * 0.25f)
                    lineTo(w * 0.84f, h * 0.58f)
                    cubicTo(
                        w * 0.84f, h * 0.78f,
                        w * 0.66f, h * 0.90f,
                        w * 0.50f, h * 0.97f
                    )
                    cubicTo(
                        w * 0.34f, h * 0.90f,
                        w * 0.16f, h * 0.78f,
                        w * 0.16f, h * 0.58f
                    )
                    close()
                }

                val shieldStroke = Stroke(width = w * 0.055f)
                val ballStroke = Stroke(width = w * 0.02f)
                val ballCenter = Offset(w * 0.50f, h * 0.74f)
                val ballRadius = w * 0.11f

                onDrawBehind {
                    drawPath(path = crownPath, color = goldRim)
                    drawPath(path = shieldPath, color = navyFill)
                    drawPath(path = shieldPath, brush = rimBrush, style = shieldStroke)
                    drawLine(
                        color = goldRim.copy(alpha = 0.28f),
                        start = Offset(w * 0.36f, h * 0.27f),
                        end = Offset(w * 0.36f, h * 0.85f),
                        strokeWidth = w * 0.025f
                    )
                    drawLine(
                        color = goldRim.copy(alpha = 0.28f),
                        start = Offset(w * 0.64f, h * 0.27f),
                        end = Offset(w * 0.64f, h * 0.85f),
                        strokeWidth = w * 0.025f
                    )
                    drawCircle(color = goldRim, radius = ballRadius, center = ballCenter)
                    drawCircle(
                        color = navyFill,
                        radius = ballRadius * 0.75f,
                        center = ballCenter,
                        style = ballStroke
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RCB",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.25f).sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = (size.value * 0.06f).dp)
        )
    }
}

/**
 * Team Crest Shield Badge (renders RCB crest for Rajpoot Boys or a Rival Shield Crest for opponents).
 */
@Composable
fun TeamShieldBadge(
    teamName: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val isRcb = teamName.contains("Rajpoot", ignoreCase = true) ||
        teamName.equals("RCB", ignoreCase = true)

    if (isRcb) {
        RcbCrestLogo(size = size, modifier = modifier)
    } else {
        val initials = remember(teamName) {
            teamName.trim().split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "XI" }
        }
        Box(
            modifier = modifier
                .size(size)
                .drawWithCache {
                    val w = this.size.width
                    val h = this.size.height
                    val shieldPath = Path().apply {
                        moveTo(w * 0.16f, h * 0.16f)
                        lineTo(w * 0.84f, h * 0.16f)
                        lineTo(w * 0.84f, h * 0.56f)
                        cubicTo(
                            w * 0.84f, h * 0.76f,
                            w * 0.66f, h * 0.88f,
                            w * 0.50f, h * 0.96f
                        )
                        cubicTo(
                            w * 0.34f, h * 0.88f,
                            w * 0.16f, h * 0.76f,
                            w * 0.16f, h * 0.56f
                        )
                        close()
                    }
                    val fillBrush = Brush.verticalGradient(listOf(Color(0xFF4A1228), Color(0xFF14182E)))
                    val rimStroke = Stroke(width = w * 0.055f)

                    onDrawBehind {
                        drawPath(path = shieldPath, brush = fillBrush)
                        drawPath(path = shieldPath, color = Color(0xFFE05268), style = rimStroke)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.26f).sp
            )
        }
    }
}

/**
 * Red `((•)) LIVE` Pill Badge matching the reference image.
 */
@Composable
fun LiveBadgePill(
    text: String = "LIVE",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFF2442))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    }
}

/**
 * Signature Bright Sky-Cyan / Ice-Aqua Live Match Score Banner Card
 * matching both the Home Dashboard and the Live Match Screen in the reference image.
 */
@Composable
fun SkyCyanLiveScoreBanner(
    leftTeamName: String,
    leftScore: String,
    leftOvers: String,
    rightTeamName: String,
    rightScore: String,
    rightOvers: String,
    statusText: String,
    modifier: Modifier = Modifier,
    matchTitle: String? = null,
    matchSubtitle: String? = null,
    isLive: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val skyCyanBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB5F3FF),
            Color(0xFF8BE7FF),
            Color(0xFFA6F0FF)
        )
    )
    val darkNavyText = Color(0xFF041022)
    val mutedNavyText = Color(0xFF1A3F66)

    val cardModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(22.dp),
            ambientColor = Color(0x6629E2FF),
            spotColor = Color(0x8829E2FF)
        )
        .clip(RoundedCornerShape(22.dp))
        .background(skyCyanBrush)
        .border(1.5.dp, Color(0xFFD6F9FF), RoundedCornerShape(22.dp))
        .let { base ->
            if (onClick != null) base.clickable(onClick = onClick) else base
        }

    Column(
        modifier = cardModifier.padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Top Header Row (LIVE pill + optional match title & venue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveBadgePill(text = if (isLive) "LIVE" else "MATCH")
                if (!matchTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = matchTitle,
                            color = darkNavyText,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!matchSubtitle.isNullOrBlank()) {
                            Text(
                                text = matchSubtitle,
                                color = Color(0xFF006994),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Score Comparison Row (Left Team vs Right Team)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Team Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TeamShieldBadge(teamName = leftTeamName, size = 44.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = leftTeamName,
                    color = darkNavyText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = leftScore,
                    color = darkNavyText,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = leftOvers,
                    color = mutedNavyText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            // Center VS Circle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF081C38))
                    .border(1.5.dp, Color(0xFF29E2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }

            // Right Team Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TeamShieldBadge(teamName = rightTeamName, size = 44.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rightTeamName,
                    color = darkNavyText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = rightScore,
                    color = darkNavyText,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = rightOvers,
                    color = mutedNavyText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0x28041022), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            color = darkNavyText,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Rounded Pill Tab Row matching the reference image (`Match Info`, `Live`, `Scoreboard`, `Commentary`
 * and `Batter`, `Bowler`, `All Rounder`, `Team`).
 */
@Composable
fun CyanPillTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        itemsIndexed(tabs) { index, title ->
            val selected = index == selectedIndex
            val bgBrush = if (selected) {
                Brush.horizontalGradient(listOf(Color(0xFF56ECFF), Color(0xFF24D2F7)))
            } else {
                Brush.horizontalGradient(listOf(tokens.glassCardBackground, tokens.glassCardBackground))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgBrush)
                    .border(
                        width = 1.dp,
                        color = if (selected) Color(0xFF9AF5FF) else tokens.glassCardBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (selected) Color(0xFF041022) else tokens.textPrimary,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Outer Segmented Format Pill Selector matching the top of the Rankings screen in the reference image.
 */
@Composable
fun SegmentedFormatSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.glassCardBackground)
            .border(1.dp, tokens.glassCardBorder, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { idx, label ->
            val isSelected = idx == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(listOf(Color(0xFF52ECFF), Color(0xFF24D2F7)))
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSelect(idx) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color(0xFF041022) else tokens.textPrimary,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Glowing Cyan Solid Action Button with 3D press depth (named GoldButton for API compatibility).
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
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
        label = "btn_scale"
    )

    val buttonBrush = if (enabled) {
        Brush.horizontalGradient(
            listOf(Color(0xFF56ECFF), Color(0xFF22D4FA))
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color(0x4400E5FF), Color(0x3300838F))
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(48.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x5529E2FF),
                spotColor = Color(0x5529E2FF)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(buttonBrush)
            .border(1.dp, Color(0xFF9AF5FF), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
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
                color = if (enabled) (if (tokens.isDark) Color(0xFF041022) else Color.White) else Color(0x88FFFFFF),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Outlined Midnight Navy & Cyan Action Button.
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
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
        label = "out_btn_scale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0A213C)),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = tokens.accentGold
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFF29E2FF), Color(0xFF0099CC))),
            width = 1.5.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
                color = tokens.accentGoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Compact Theme Toggle Pill (Midnight Navy Dark / Daylight Ice).
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    contentDescription = "Dark Midnight Navy",
                    tint = if (tokens.isDark) CharcoalBlack else tokens.textSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

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
                    contentDescription = "Daylight Mode",
                    tint = if (!tokens.isDark) Color.White else tokens.textSecondary,
                    modifier = Modifier.size(15.dp)
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
        delivery.runsOffBat == 6 -> Triple(Color(0xFF00E5FF), Color(0xFF041022), "6")
        delivery.runsOffBat == 4 -> Triple(Color(0xFF3CE2FF), Color(0xFF041022), "4")
        delivery.extrasType == ExtrasType.WIDE -> Triple(AccentExtra, CharcoalBlack, "${delivery.extrasRuns}wd")
        delivery.extrasType == ExtrasType.NO_BALL -> Triple(AccentExtra, CharcoalBlack, "${delivery.totalBallRuns}nb")
        delivery.runsOffBat == 0 && delivery.extrasType == ExtrasType.NONE -> Triple(
            if (tokens.isDark) Color(0xFF0D213F) else Color(0xFFD6E8F8),
            tokens.textSecondary,
            "0"
        )
        else -> Triple(
            if (tokens.isDark) Color(0xFF122B52) else Color(0xFFC2DEF6),
            if (tokens.isDark) Color.White else Color(0xFF051329),
            "${delivery.totalBallRuns}"
        )
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (delivery.runsOffBat >= 4) Color(0xFFA4F0FF) else tokens.glassCardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = text,
            fontWeight = FontWeight.ExtraBold,
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
        backgroundColor = tokens.glassCardElevated,
        borderColor = tokens.accentGold,
        borderWidth = 1.5.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamShieldBadge(
                        teamName = state.battingTeam.ifEmpty { "Rajpoot Boys" },
                        size = 34.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = state.battingTeam.ifEmpty { "Rajpoot Cricket Boys" }.uppercase(),
                            color = tokens.accentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.6.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${state.totalRuns}/${state.totalWickets}",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${state.formattedOvers} ov)",
                                color = tokens.textSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CRR: ${state.formattedCRR}",
                        color = tokens.textGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (state.target != null) {
                        Text(
                            text = "Target: ${state.target} (Need ${state.runsRequired ?: 0})",
                            color = AccentExtra,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val striker = state.currentStriker
                    Text(
                        text = "★ ${striker?.playerName ?: "Striker"} ${striker?.runs ?: 0}* (${striker?.ballsFaced ?: 0})",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    val nonStriker = state.currentNonStriker
                    Text(
                        text = "   ${nonStriker?.playerName ?: "Non-striker"} ${nonStriker?.runs ?: 0} (${nonStriker?.ballsFaced ?: 0})",
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }

                val bowler = state.currentBowler
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = bowler?.bowlerName ?: "Bowler",
                        color = tokens.accentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${bowler?.formattedOvers ?: "0.0"}-${bowler?.maidens ?: 0}-${bowler?.runsConceded ?: 0}-${bowler?.wickets ?: 0}",
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (state.recentDeliveries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
