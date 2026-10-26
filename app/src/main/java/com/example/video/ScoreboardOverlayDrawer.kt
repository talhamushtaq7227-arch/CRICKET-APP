package com.example.video

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.model.ExtrasType
import com.example.data.model.OverlayStyle
import com.example.domain.engine.InningsLiveState

object ScoreboardOverlayDrawer {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    fun drawScoreboard(
        canvas: Canvas,
        width: Int,
        height: Int,
        state: InningsLiveState,
        style: OverlayStyle = OverlayStyle.LOWER_THIRD,
        scale: Float = 1.0f
    ) {
        val isPortrait = height > width
        val density = (width.coerceAtMost(height) / 360f).coerceIn(1.0f, 3.5f) * scale

        when (style) {
            OverlayStyle.LOWER_THIRD -> drawLowerThird(canvas, width, height, state, density, isPortrait)
            OverlayStyle.WIDE_BANNER -> drawTopBanner(canvas, width, height, state, density)
            OverlayStyle.COMPACT_CORNER -> drawCompactCorner(canvas, width, height, state, density)
        }
    }

    private fun drawLowerThird(
        canvas: Canvas,
        width: Int,
        height: Int,
        state: InningsLiveState,
        density: Float,
        isPortrait: Boolean
    ) {
        val marginHorizontal = (16f * density).coerceAtLeast(16f)
        val bannerWidth = width - (marginHorizontal * 2)
        val bannerHeight = (if (isPortrait) 96f else 78f) * density
        val bottomMargin = (24f * density).coerceAtLeast(20f)

        val top = height - bottomMargin - bannerHeight
        val bottom = height - bottomMargin
        val left = marginHorizontal
        val right = left + bannerWidth
        val rect = RectF(left, top, right, bottom)
        val cornerRadius = 10f * density

        // Draw translucent dark glass background
        bgPaint.shader = LinearGradient(
            left, top, right, bottom,
            Color.argb(235, 12, 12, 16),
            Color.argb(245, 18, 18, 25),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        // Draw metallic gold border
        borderPaint.color = Color.argb(160, 212, 175, 55)
        borderPaint.strokeWidth = 1.8f * density
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        // Left gold accent strip
        val stripRect = RectF(left, top, left + (4f * density), bottom)
        bgPaint.shader = null
        bgPaint.color = Color.rgb(212, 175, 55)
        canvas.drawRoundRect(stripRect, 4f * density, 4f * density, bgPaint)

        val padX = 14f * density
        val startX = left + padX

        // Row 1: Batting Team & Big Score
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 15f * density
        textPaint.color = Color.rgb(255, 224, 130) // Gold
        val teamText = state.battingTeam.ifEmpty { "RCB" }.uppercase()
        canvas.drawText(teamText, startX, top + (20f * density), textPaint)

        // Score Numerals: e.g. "148/3"
        textPaint.textSize = 24f * density
        textPaint.color = Color.WHITE
        val scoreText = "${state.totalRuns}/${state.totalWickets}"
        val teamWidth = textPaint.measureText(teamText)
        val scoreX = startX + (if (isPortrait) 0f else (teamWidth * 0.7f + 20f * density))
        val scoreY = top + (if (isPortrait) 44f else 22f) * density
        canvas.drawText(scoreText, if (isPortrait) startX else scoreX, scoreY, textPaint)

        // Overs text: "(14.2 ov)"
        textPaint.textSize = 13f * density
        textPaint.color = Color.rgb(200, 200, 215)
        val overText = "(${state.formattedOvers} ov)"
        val scoreWidth = textPaint.measureText(scoreText)
        val overX = (if (isPortrait) startX else scoreX) + (if (isPortrait) scoreWidth + 12f * density else scoreWidth + 10f * density)
        canvas.drawText(overText, overX, scoreY, textPaint)

        // Run rate (CRR / RRR)
        val crrText = "CRR: ${state.formattedCRR}" + (state.formattedRRR?.let { " | REQ: $it" } ?: "")
        textPaint.textSize = 11f * density
        textPaint.color = Color.rgb(255, 215, 0)
        canvas.drawText(crrText, right - (130f * density), top + (20f * density), textPaint)

        // Row 2: Batters & Bowler
        val row2Y = top + (if (isPortrait) 68f else 48f) * density
        textPaint.textSize = 12f * density
        textPaint.color = Color.WHITE

        val striker = state.currentStriker
        val strikerText = if (striker != null) {
            "★ ${striker.playerName} ${striker.runs}*(${striker.ballsFaced})"
        } else "★ Batting"
        canvas.drawText(strikerText, startX, row2Y, textPaint)

        val nonStriker = state.currentNonStriker
        if (nonStriker != null) {
            textPaint.color = Color.rgb(180, 180, 195)
            val nonStrikerText = " | ${nonStriker.playerName} ${nonStriker.runs}(${nonStriker.ballsFaced})"
            val strikerW = textPaint.measureText(strikerText)
            canvas.drawText(nonStrikerText, startX + strikerW, row2Y, textPaint)
        }

        // Bowler stats on right
        val bowler = state.currentBowler
        if (bowler != null) {
            textPaint.color = Color.rgb(220, 220, 235)
            val bowlerText = "Bowl: ${bowler.bowlerName} ${bowler.formattedOvers}-${bowler.maidens}-${bowler.runsConceded}-${bowler.wickets}"
            val bowlerW = textPaint.measureText(bowlerText)
            canvas.drawText(bowlerText, right - bowlerW - (12f * density), row2Y, textPaint)
        }

        // Row 3: Recent deliveries chips
        val row3Y = top + (if (isPortrait) 86f else 66f) * density
        drawRecentDeliveriesChips(canvas, startX, row3Y, state, density)
    }

    private fun drawRecentDeliveriesChips(
        canvas: Canvas,
        startX: Float,
        centerY: Float,
        state: InningsLiveState,
        density: Float
    ) {
        val balls = state.recentDeliveries.takeLast(6)
        if (balls.isEmpty()) return

        var curX = startX
        val badgeW = 20f * density
        val badgeH = 14f * density

        for (ball in balls) {
            val (badgeBg, badgeBorder, labelText) = when {
                ball.isWicket -> Triple(Color.rgb(220, 38, 38), Color.WHITE, "W")
                ball.runsOffBat == 6 -> Triple(Color.rgb(124, 58, 237), Color.WHITE, "6")
                ball.runsOffBat == 4 -> Triple(Color.rgb(5, 150, 105), Color.WHITE, "4")
                ball.extrasType == ExtrasType.WIDE -> Triple(Color.rgb(217, 119, 6), Color.WHITE, "${ball.extrasRuns}wd")
                ball.extrasType == ExtrasType.NO_BALL -> Triple(Color.rgb(217, 119, 6), Color.WHITE, "${ball.totalBallRuns}nb")
                ball.runsOffBat == 0 && ball.extrasType == ExtrasType.NONE -> Triple(Color.rgb(40, 40, 50), Color.rgb(80, 80, 95), "•")
                else -> Triple(Color.rgb(50, 50, 65), Color.rgb(100, 100, 120), "${ball.totalBallRuns}")
            }

            val bRect = RectF(curX, centerY - (badgeH / 2), curX + badgeW, centerY + (badgeH / 2))
            badgePaint.color = badgeBg
            canvas.drawRoundRect(bRect, 4f * density, 4f * density, badgePaint)

            badgeBorderPaint.color = badgeBorder
            badgeBorderPaint.strokeWidth = 1f * density
            canvas.drawRoundRect(bRect, 4f * density, 4f * density, badgeBorderPaint)

            textPaint.textSize = 9f * density
            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val textW = textPaint.measureText(labelText)
            canvas.drawText(labelText, curX + (badgeW - textW) / 2, centerY + (3f * density), textPaint)

            curX += badgeW + (4f * density)
        }
    }

    private fun drawTopBanner(
        canvas: Canvas,
        width: Int,
        height: Int,
        state: InningsLiveState,
        density: Float
    ) {
        val bannerH = 46f * density
        val rect = RectF(0f, 0f, width.toFloat(), bannerH)
        bgPaint.color = Color.argb(230, 10, 10, 15)
        canvas.drawRect(rect, bgPaint)

        borderPaint.color = Color.argb(160, 212, 175, 55)
        borderPaint.strokeWidth = 2f * density
        canvas.drawLine(0f, bannerH, width.toFloat(), bannerH, borderPaint)

        textPaint.color = Color.rgb(255, 215, 0)
        textPaint.textSize = 14f * density
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(state.battingTeam.ifEmpty { "RCB" }, 16f * density, 28f * density, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 18f * density
        canvas.drawText("${state.totalRuns}/${state.totalWickets} (${state.formattedOvers})", 90f * density, 28f * density, textPaint)

        val crr = "CRR: ${state.formattedCRR}"
        textPaint.textSize = 12f * density
        textPaint.color = Color.rgb(255, 220, 100)
        canvas.drawText(crr, width - (100f * density), 28f * density, textPaint)
    }

    private fun drawCompactCorner(
        canvas: Canvas,
        width: Int,
        height: Int,
        state: InningsLiveState,
        density: Float
    ) {
        val cardW = 150f * density
        val cardH = 60f * density
        val rect = RectF(16f * density, 24f * density, 16f * density + cardW, 24f * density + cardH)

        bgPaint.color = Color.argb(230, 12, 12, 18)
        canvas.drawRoundRect(rect, 8f * density, 8f * density, bgPaint)

        borderPaint.color = Color.rgb(212, 175, 55)
        borderPaint.strokeWidth = 1.5f * density
        canvas.drawRoundRect(rect, 8f * density, 8f * density, borderPaint)

        textPaint.color = Color.rgb(255, 215, 0)
        textPaint.textSize = 11f * density
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(state.battingTeam.ifEmpty { "RCB" }, rect.left + 10f * density, rect.top + 18f * density, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 18f * density
        canvas.drawText("${state.totalRuns}/${state.totalWickets}", rect.left + 10f * density, rect.top + 40f * density, textPaint)

        textPaint.textSize = 11f * density
        textPaint.color = Color.LTGRAY
        canvas.drawText("${state.formattedOvers} ov", rect.left + 85f * density, rect.top + 40f * density, textPaint)
    }
}
