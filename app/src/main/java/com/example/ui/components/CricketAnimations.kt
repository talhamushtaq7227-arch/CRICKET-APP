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
 * Authentic 3D-styled Cricket Leather Ball with rotating white hand-stitched seam.
 */
@Composable
fun CricketBallSeamSpinner(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 38.dp,
    isSpinning: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cricket_ball_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ball_spin_angle"
    )

    val currentAngle = if (isSpinning) angle else 25f

    Canvas(
        modifier = modifier
            .size(sizeDp)
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Red Leather Ball Body with spherical shadow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE53935), // Shiny red highlight
                    Color(0xFFB71C1C), // Deep crimson leather
                    Color(0xFF5D0000)  // Deep shadow at bottom
                ),
                center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                radius = radius * 1.3f
            ),
            center = center,
            radius = radius
        )

        // 2. Leather Seam Ridge (Rotating)
        val rad = (currentAngle * PI / 180f).toFloat()
        val seamWidthFactor = cos(rad) // Oscillates between -1 and 1 to simulate 3D rotation

        val seamPath = Path().apply {
            val seamYOffset = sin(rad) * radius * 0.2f
            moveTo(center.x - radius * 0.9f, center.y + seamYOffset)
            cubicTo(
                center.x - radius * 0.3f, center.y + seamWidthFactor * radius * 0.8f + seamYOffset,
                center.x + radius * 0.3f, center.y + seamWidthFactor * radius * 0.8f + seamYOffset,
                center.x + radius * 0.9f, center.y - seamYOffset
            )
        }

        // Seam background track
        drawPath(
            path = seamPath,
            color = Color(0xFF2B0000),
            style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round)
        )

        // White hand-stitched threads
        drawPath(
            path = seamPath,
            color = Color(0xFFFFF9E6),
            style = Stroke(
                width = radius * 0.08f,
                cap = StrokeCap.Round
            )
        )

        // Specular glossy highlight reflection (makes it look like lacquer polished leather)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x80FFFFFF), Color.Transparent),
                center = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f),
                radius = radius * 0.45f
            ),
            center = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f),
            radius = radius * 0.45f
        )
    }
}

/**
 * Animated Cricket Stumps and Bails that fly apart upon a Wicket!
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
        modifier = modifier
            .size(160.dp, 120.dp)
    ) {
        val baseY = size.height * 0.85f
        val stumpHeight = size.height * 0.6f
        val stumpWidth = 8.dp.toPx()
        val spacing = 22.dp.toPx()
        val centerX = size.width / 2f

        // Stumps wood gradient
        val woodBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0A96D), Color(0xFF8D5B28))
        )

        // Off Stump (shudders slightly)
        val offStumpX = centerX - spacing - (t * 8.dp.toPx())
        val offTilt = -t * 12f
        drawStump(offStumpX, baseY, stumpWidth, stumpHeight, woodBrush, offTilt)

        // Middle Stump (shatters back and rotates)
        val midStumpX = centerX + (t * 6.dp.toPx())
        val midTilt = t * 25f
        drawStump(midStumpX, baseY + (t * 12.dp.toPx()), stumpWidth, stumpHeight, woodBrush, midTilt)

        // Leg Stump (stays or bends)
        val legStumpX = centerX + spacing + (t * 4.dp.toPx())
        val legTilt = t * 8f
        drawStump(legStumpX, baseY, stumpWidth, stumpHeight, woodBrush, legTilt)

        // Flying Bail 1 (Flies to top-left)
        val bail1X = centerX - spacing * 0.5f - (t * 60.dp.toPx())
        val bail1Y = (baseY - stumpHeight) - (sin(t * PI) * 50.dp.toPx()).toFloat()
        val bail1Rot = -t * 360f
        drawBail(bail1X, bail1Y, 18.dp.toPx(), 4.dp.toPx(), bail1Rot)

        // Flying Bail 2 (Flies to top-right)
        val bail2X = centerX + spacing * 0.5f + (t * 65.dp.toPx())
        val bail2Y = (baseY - stumpHeight) - (sin(t * PI * 0.9f) * 60.dp.toPx()).toFloat()
        val bail2Rot = t * 420f
        drawBail(bail2X, bail2Y, 18.dp.toPx(), 4.dp.toPx(), bail2Rot)

        // Impact ground dust / shockwave
        if (t > 0.05f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FF5252), Color.Transparent),
                    center = Offset(centerX, baseY),
                    radius = t * 60.dp.toPx()
                ),
                center = Offset(centerX, baseY),
                radius = t * 60.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawStump(
    x: Float,
    baseY: Float,
    width: Float,
    height: Float,
    brush: Brush,
    rotationDeg: Float
) {
    val topY = baseY - height
    drawRoundRect(
        brush = brush,
        topLeft = Offset(x - width / 2f, topY),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 3f, width / 3f)
    )
}

private fun DrawScope.drawBail(
    x: Float,
    y: Float,
    length: Float,
    height: Float,
    rotationDeg: Float
) {
    drawRoundRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(x - length / 2f, y - height / 2f),
        size = Size(length, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2f, height / 2f)
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
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
        delay(600)
        onFinished()
    }

    val t = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val start = Offset(w * 0.15f, h * 0.82f)
        val end = if (isSix) Offset(w * 0.88f, h * 0.18f) else Offset(w * 0.9f, h * 0.65f)
        val control = if (isSix) Offset(w * 0.45f, h * 0.05f) else Offset(w * 0.5f, h * 0.72f)

        // Calculate current ball position on quadratic bezier curve
        val bx = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x
        val by = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y

        // Glowing comet trail
        val trailPath = Path().apply {
            moveTo(start.x, start.y)
            for (step in 1..20) {
                val subT = (t * (step / 20f))
                val px = (1 - subT) * (1 - subT) * start.x + 2 * (1 - subT) * subT * control.x + subT * subT * end.x
                val py = (1 - subT) * (1 - subT) * start.y + 2 * (1 - subT) * subT * control.y + subT * subT * end.y
                lineTo(px, py)
            }
        }

        val trailColor = if (isSix) Color(0xFFC084FC) else Color(0xFF34D399)
        drawPath(
            path = trailPath,
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, trailColor, Color.White),
                start = start,
                end = Offset(bx, by)
            ),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw ball at current head position
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFE53935), Color(0xFF8E0000)),
                center = Offset(bx, by),
                radius = 12.dp.toPx()
            ),
            center = Offset(bx, by),
            radius = 10.dp.toPx()
        )

        // Impact spark on reaching boundary
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
 * Appears dynamically when boundaries, wickets, or milestones are triggered.
 */
@Composable
fun CricketCelebrationOverlay(
    event: CricketAnimationType?,
    onDismiss: () -> Unit
) {
    if (event == null) return

    val tokens = LiquidGlassTheme.tokens

    LaunchedEffect(event) {
        delay(2600)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Trajectory for Boundaries
        when (event) {
            is CricketAnimationType.Four -> {
                BoundaryTrajectoryAnimation(isSix = false)
            }
            is CricketAnimationType.Six -> {
                BoundaryTrajectoryAnimation(isSix = true)
            }
            else -> {}
        }

        // Center celebration glass banner
        GlassCard(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            backgroundColor = tokens.glassCardBackground,
            borderColor = tokens.accentGold
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
                            CricketBallSeamSpinner(sizeDp = 42.dp)
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
                            CricketBallSeamSpinner(sizeDp = 46.dp)
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
                        CricketBallSeamSpinner(sizeDp = 48.dp)
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
