package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LiquidGlassTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Types of cricket-specific animation events.
 */
sealed class CricketAnimationType {
    object Four : CricketAnimationType()
    object Six : CricketAnimationType()
    data class Wicket(val description: String = "WICKET!") : CricketAnimationType()
    data class Milestone(val title: String = "50! HALF CENTURY") : CricketAnimationType()
}

/**
 * 3D-styled Cricket Ball with white stitched seam and cyan stadium rim reflection.
 * Defaults to a one-shot entrance rotation so it does NOT drain CPU/GPU on idle screens.
 */
@Composable
fun CricketBallSeamSpinner(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 38.dp,
    isSpinning: Boolean = false
) {
    val currentAngle = if (isSpinning) {
        val infiniteTransition = rememberInfiniteTransition(label = "cricket_ball_spin")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ball_spin_angle"
        )
        angle
    } else {
        val entranceAngle = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            entranceAngle.animateTo(
                targetValue = 385f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
        }
        entranceAngle.value
    }

    Canvas(
        modifier = modifier.size(sizeDp)
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer cyan stadium rim glow
        drawCircle(
            color = Color(0x3300E5FF),
            center = center,
            radius = radius * 1.08f
        )

        // 1. Leather Ball Body with 3D spherical shading
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF5252),
                    Color(0xFFC62828),
                    Color(0xFF4A0000)
                ),
                center = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f),
                radius = radius * 1.3f
            ),
            center = center,
            radius = radius
        )

        // 2. Leather Seam Ridge
        val rad = (currentAngle * PI / 180f).toFloat()
        val seamWidthFactor = cos(rad)

        val seamPath = Path().apply {
            val seamYOffset = sin(rad) * radius * 0.2f
            moveTo(center.x - radius * 0.9f, center.y + seamYOffset)
            cubicTo(
                center.x - radius * 0.3f, center.y + seamWidthFactor * radius * 0.8f + seamYOffset,
                center.x + radius * 0.3f, center.y + seamWidthFactor * radius * 0.8f + seamYOffset,
                center.x + radius * 0.9f, center.y - seamYOffset
            )
        }

        drawPath(
            path = seamPath,
            color = Color(0xFF2B0000),
            style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round)
        )

        drawPath(
            path = seamPath,
            color = Color(0xFFE0F7FA),
            style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round)
        )

        // 3. Specular glossy highlight
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x90FFFFFF), Color.Transparent),
                center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f),
                radius = radius * 0.45f
            ),
            center = Offset(center.x - radius * 0.38f, center.y - radius * 0.38f),
            radius = radius * 0.45f
        )
    }
}

/**
 * Animated LED Cricket Stumps and Bails that fly apart upon a Wicket!
 */
@Composable
fun FlyingWicketsAnimation(
    modifier: Modifier = Modifier,
    isTriggered: Boolean = true
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    val t = progress.value

    Canvas(
        modifier = modifier.size(160.dp, 120.dp)
    ) {
        val baseY = size.height * 0.85f
        val stumpHeight = size.height * 0.6f
        val stumpWidth = 8.dp.toPx()
        val spacing = 22.dp.toPx()
        val centerX = size.width / 2f

        val woodBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF84FFFF), Color(0xFF00E5FF), Color(0xFF00838F))
        )

        val offStumpX = centerX - spacing - (t * 8.dp.toPx())
        drawStump(offStumpX, baseY, stumpWidth, stumpHeight, woodBrush)

        val midStumpX = centerX + (t * 6.dp.toPx())
        drawStump(midStumpX, baseY + (t * 12.dp.toPx()), stumpWidth, stumpHeight, woodBrush)

        val legStumpX = centerX + spacing + (t * 4.dp.toPx())
        drawStump(legStumpX, baseY, stumpWidth, stumpHeight, woodBrush)

        val bail1X = centerX - spacing * 0.5f - (t * 60.dp.toPx())
        val bail1Y = (baseY - stumpHeight) - (sin(t * PI) * 50.dp.toPx()).toFloat()
        drawBail(bail1X, bail1Y, 18.dp.toPx(), 4.dp.toPx())

        val bail2X = centerX + spacing * 0.5f + (t * 65.dp.toPx())
        val bail2Y = (baseY - stumpHeight) - (sin(t * PI * 0.9f) * 60.dp.toPx()).toFloat()
        drawBail(bail2X, bail2Y, 18.dp.toPx(), 4.dp.toPx())

        if (t > 0.05f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FF3B5C), Color(0x4400E5FF), Color.Transparent),
                    center = Offset(centerX, baseY),
                    radius = t * 65.dp.toPx()
                ),
                center = Offset(centerX, baseY),
                radius = t * 65.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawStump(
    x: Float,
    baseY: Float,
    width: Float,
    height: Float,
    brush: Brush
) {
    val topY = baseY - height
    drawRoundRect(
        brush = brush,
        topLeft = Offset(x - width / 2f, topY),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 3f, width / 3f)
    )
}

private fun DrawScope.drawBail(
    x: Float,
    y: Float,
    length: Float,
    height: Float
) {
    drawRoundRect(
        color = Color(0xFFFF3B5C),
        topLeft = Offset(x - length / 2f, y - height / 2f),
        size = Size(length, height),
        cornerRadius = CornerRadius(height / 2f, height / 2f)
    )
}

/**
 * Animated High-Speed Cricket Ball Trajectory for Boundaries (4s and 6s).
 */
@Composable
fun BoundaryTrajectoryAnimation(
    modifier: Modifier = Modifier,
    isSix: Boolean = false,
    onFinished: () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
        delay(500)
        onFinished()
    }

    val t = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val start = Offset(w * 0.15f, h * 0.82f)
        val end = if (isSix) Offset(w * 0.88f, h * 0.18f) else Offset(w * 0.9f, h * 0.65f)
        val control = if (isSix) Offset(w * 0.45f, h * 0.05f) else Offset(w * 0.5f, h * 0.72f)

        val bx = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x
        val by = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y

        val trailPath = Path().apply {
            moveTo(start.x, start.y)
            for (step in 1..20) {
                val subT = (t * (step / 20f))
                val px = (1 - subT) * (1 - subT) * start.x + 2 * (1 - subT) * subT * control.x + subT * subT * end.x
                val py = (1 - subT) * (1 - subT) * start.y + 2 * (1 - subT) * subT * control.y + subT * subT * end.y
                lineTo(px, py)
            }
        }

        val trailColor = if (isSix) Color(0xFF00E5FF) else Color(0xFF00E676)
        drawPath(
            path = trailPath,
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, trailColor, Color.White),
                start = start,
                end = Offset(bx, by)
            ),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFF00E5FF), Color(0xFF006064)),
                center = Offset(bx, by),
                radius = 12.dp.toPx()
            ),
            center = Offset(bx, by),
            radius = 10.dp.toPx()
        )

        if (t > 0.85f) {
            val sparkProgress = (t - 0.85f) / 0.15f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, trailColor, Color.Transparent),
                    center = end,
                    radius = sparkProgress * 40.dp.toPx()
                ),
                center = end,
                radius = sparkProgress * 40.dp.toPx()
            )
        }
    }
}

/**
 * Liquid Glass Cricket Celebration Modal / Overlay.
 */
@Composable
fun CricketCelebrationOverlay(
    event: CricketAnimationType?,
    onDismiss: () -> Unit
) {
    if (event == null) return

    val tokens = LiquidGlassTheme.tokens

    LaunchedEffect(event) {
        delay(2200)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77020814))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        when (event) {
            is CricketAnimationType.Four -> BoundaryTrajectoryAnimation(isSix = false)
            is CricketAnimationType.Six -> BoundaryTrajectoryAnimation(isSix = true)
            else -> {}
        }

        GlassCard(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .liquid3DEntrance(),
            backgroundColor = tokens.glassCardElevated,
            borderColor = tokens.accentGold,
            borderWidth = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (event) {
                    is CricketAnimationType.Four -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CricketBallSeamSpinner(sizeDp = 42.dp, isSpinning = true)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "FOUR! 4",
                                color = AccentFour,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SMASHED TO THE BOUNDARY ROPE!",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    is CricketAnimationType.Six -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CricketBallSeamSpinner(sizeDp = 46.dp, isSpinning = true)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "MAXIMUM! 6",
                                color = AccentSix,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "HIGH, HANDSOME & INTO THE STANDS!",
                            color = GoldBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    is CricketAnimationType.Wicket -> {
                        FlyingWicketsAnimation(isTriggered = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "WICKET!",
                            color = AccentWicket,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description.uppercase(),
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    is CricketAnimationType.Milestone -> {
                        CricketBallSeamSpinner(sizeDp = 48.dp, isSpinning = true)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = event.title,
                            color = GoldPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BATSMAN RAISES THE WILLOW!",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
