package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.domain.engine.InningsLiveState
import com.example.ui.theme.AccentExtra
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = GlassSurface,
    content: @Composable () -> Unit
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
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(50.dp)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(GoldBright, GoldPrimary, GoldDark))
                } else {
                    Brush.horizontalGradient(listOf(Color(0x55A67C1E), Color(0x55554010)))
                },
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
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
                color = if (enabled) CharcoalBlack else Color(0x88FFFFFF),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun OutlinedGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0x1AD4AF37),
            contentColor = GoldLight
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldBright)),
            width = 1.dp
        )
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
                color = GoldBright,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DeliveryChip(
    delivery: BallDeliveryEntity,
    modifier: Modifier = Modifier
) {
    val (bg, text, label) = when {
        delivery.isWicket -> Triple(AccentWicket, Color.White, "W")
        delivery.runsOffBat == 6 -> Triple(AccentSix, Color.White, "6")
        delivery.runsOffBat == 4 -> Triple(AccentFour, Color.White, "4")
        delivery.extrasType == ExtrasType.WIDE -> Triple(AccentExtra, CharcoalBlack, "${delivery.extrasRuns}wd")
        delivery.extrasType == ExtrasType.NO_BALL -> Triple(AccentExtra, CharcoalBlack, "${delivery.totalBallRuns}nb")
        delivery.runsOffBat == 0 && delivery.extrasType == ExtrasType.NONE -> Triple(Color(0xFF282834), TextSecondary, "•")
        else -> Triple(Color(0xFF323242), Color.White, "${delivery.totalBallRuns}")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
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
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        backgroundColor = Color(0xEE101018),
        borderColor = Color(0x66D4AF37),
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
                            .height(28.dp)
                            .background(GoldPrimary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = state.battingTeam.ifEmpty { "Rajpoot Cricket Boys" }.uppercase(),
                            color = GoldBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${state.totalRuns}/${state.totalWickets}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${state.formattedOvers} ov)",
                                color = TextSecondary,
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
                        color = GoldLight,
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

            Spacer(modifier = Modifier.height(8.dp))

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
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    val nonStriker = state.currentNonStriker
                    Text(
                        text = "   ${nonStriker?.playerName ?: "Non-striker"} ${nonStriker?.runs ?: 0} (${nonStriker?.ballsFaced ?: 0})",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Bowler
                val bowler = state.currentBowler
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${bowler?.bowlerName ?: "Bowler"}",
                        color = GoldLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${bowler?.formattedOvers ?: "0.0"}-${bowler?.maidens ?: 0}-${bowler?.runsConceded ?: 0}-${bowler?.wickets ?: 0}",
                        color = TextSecondary,
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
                        color = TextMuted,
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
