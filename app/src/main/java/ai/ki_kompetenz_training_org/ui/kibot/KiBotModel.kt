package ai.ki_kompetenz_training_org.ui.kibot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Procedurally-drawn KiBot robot using Compose Canvas.
 * No external 3D model files needed — pure code-based rendering.
 *
 * The robot grows visually with the user's level:
 * - NEONATE: Small body, single antenna, glowing eyes
 * - LEARNER: Larger body, two antennae, small arms
 * - THINKER: Brain dome, brighter eye glow
 * - EXPERT: Full body, boosters, orbital ring
 */
@Composable
fun KiBotModel(
    stage: GrowthStage,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryDim = primary.copy(alpha = 0.3f)
    val eyeColor = Color(0xFF22D3EE) // Cyan glow
    val eyeGlow = eyeColor.copy(alpha = 0.3f)
    val antennaTip = Color(0xFFFBBF24) // Gold
    val boosterColor = Color(0xFFF97316) // Orange

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height * 0.48f
        val scale = when (stage) {
            GrowthStage.NEONATE -> 0.55f
            GrowthStage.LEARNER -> 0.70f
            GrowthStage.THINKER -> 0.85f
            GrowthStage.EXPERT -> 1.0f
        }

        val bodyW = 72.dp.toPx() * scale
        val bodyH = 90.dp.toPx() * scale
        val headR = 36.dp.toPx() * scale
        val eyeR = 5.5.dp.toPx() * scale

        // ── Body ──
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx - bodyW / 2, cy - bodyH * 0.15f),
            size = Size(bodyW, bodyH * 0.85f),
            cornerRadius = 18.dp.toPx() * scale,
        )

        // ── Head ──
        drawCircle(
            color = primary,
            radius = headR,
            center = Offset(cx, cy - bodyH * 0.15f - headR * 0.3f),
        )

        // ── Eyes ──
        val eyeY = cy - bodyH * 0.15f - headR * 0.3f - 2.dp.toPx()
        val eyeSpread = 12.dp.toPx() * scale

        // Eye glow
        drawCircle(color = eyeGlow, radius = eyeR * 2.2f, center = Offset(cx - eyeSpread, eyeY))
        drawCircle(color = eyeGlow, radius = eyeR * 2.2f, center = Offset(cx + eyeSpread, eyeY))
        // Eye core
        drawCircle(color = eyeColor, radius = eyeR, center = Offset(cx - eyeSpread, eyeY))
        drawCircle(color = eyeColor, radius = eyeR, center = Offset(cx + eyeSpread, eyeY))
        // Eye highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = eyeR * 0.35f,
            center = Offset(cx - eyeSpread + eyeR * 0.3f, eyeY - eyeR * 0.3f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = eyeR * 0.35f,
            center = Offset(cx + eyeSpread + eyeR * 0.3f, eyeY - eyeR * 0.3f),
        )

        // ── Stage-specific details ──
        when (stage) {
            GrowthStage.NEONATE -> {
                // Single antenna
                val antennaBase = cy - bodyH * 0.15f - headR * 1.3f
                val antennaTop = antennaBase - 20.dp.toPx() * scale
                drawLine(
                    color = primary,
                    start = Offset(cx, antennaBase),
                    end = Offset(cx, antennaTop),
                    strokeWidth = 3.dp.toPx() * scale,
                )
                drawCircle(
                    color = antennaTip,
                    radius = 5.dp.toPx() * scale,
                    center = Offset(cx, antennaTop - 4.dp.toPx()),
                )
            }
            GrowthStage.LEARNER -> {
                // Two antennae
                val antennaBase = cy - bodyH * 0.15f - headR * 1.3f
                val antennaH = 20.dp.toPx() * scale
                for (side in listOf(-1, 1)) {
                    val xOff = 10.dp.toPx() * scale * side
                    drawLine(
                        color = primary,
                        start = Offset(cx + xOff * 0.6f, antennaBase),
                        end = Offset(cx + xOff, antennaBase - antennaH),
                        strokeWidth = 2.5.dp.toPx(),
                    )
                    drawCircle(
                        color = antennaTip,
                        radius = 4.dp.toPx(),
                        center = Offset(cx + xOff, antennaBase - antennaH - 3.dp.toPx()),
                    )
                }
                // Small arms
                val armW = 10.dp.toPx() * scale
                val armH = 28.dp.toPx() * scale
                drawRoundRect(
                    color = primary.copy(alpha = 0.7f),
                    topLeft = Offset(cx - bodyW / 2 - armW - 2.dp.toPx(), cy + 4.dp.toPx()),
                    size = Size(armW, armH),
                    cornerRadius = 4.dp.toPx(),
                )
                drawRoundRect(
                    color = primary.copy(alpha = 0.7f),
                    topLeft = Offset(cx + bodyW / 2 + 2.dp.toPx(), cy + 4.dp.toPx()),
                    size = Size(armW, armH),
                    cornerRadius = 4.dp.toPx(),
                )
            }
            GrowthStage.THINKER -> {
                // Brain dome (translucent ring)
                val domeR = headR * 1.2f
                drawCircle(
                    color = primaryDim,
                    radius = domeR,
                    center = Offset(cx, cy - bodyH * 0.15f - headR * 0.3f),
                    style = Stroke(width = 2.dp.toPx()),
                )
                // Thinking particles (small dots orbiting dome)
                val particleR = domeR + 6.dp.toPx()
                for (i in 0..5) {
                    val angle = (i * 60f) * (Math.PI.toFloat() / 180f)
                    val px = cx + particleR * kotlin.math.cos(angle)
                    val py = cy - bodyH * 0.15f - headR * 0.3f + particleR * kotlin.math.sin(angle)
                    drawCircle(color = eyeColor.copy(alpha = 0.5f), radius = 2.dp.toPx(), center = Offset(px, py))
                }
            }
            GrowthStage.EXPERT -> {
                // Boosters
                val boosterW = 10.dp.toPx()
                val boosterH = 18.dp.toPx()
                val boosterY = cy + bodyH * 0.55f
                for (side in listOf(-1, 1)) {
                    drawRoundRect(
                        color = boosterColor,
                        topLeft = Offset(cx + side * (bodyW / 3) - boosterW / 2, boosterY),
                        size = Size(boosterW, boosterH),
                        cornerRadius = 3.dp.toPx(),
                    )
                    // Booster flame glow
                    drawCircle(
                        color = boosterColor.copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = Offset(cx + side * (bodyW / 3), boosterY + boosterH + 4.dp.toPx()),
                    )
                }
                // Orbital ring
                val orbitR = bodyW * 0.65f
                drawCircle(
                    color = primaryDim,
                    radius = orbitR,
                    center = Offset(cx, cy + bodyH * 0.25f),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
                // Orbiting dots
                for (i in 0..2) {
                    val angle = (i * 120f + 30f) * (Math.PI.toFloat() / 180f)
                    val px = cx + orbitR * kotlin.math.cos(angle)
                    val py = cy + bodyH * 0.25f + orbitR * kotlin.math.sin(angle)
                    drawCircle(color = antennaTip.copy(alpha = 0.6f), radius = 3.dp.toPx(), center = Offset(px, py))
                }
            }
        }
    }
}
