/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ki_kompetenz_training_org.R
import kotlin.math.cos
import kotlin.math.sin

/** Score-Schwelle, ab der eine Domaene als "schwach" (rot) markiert wird. */
internal const val RADAR_WEAK_THRESHOLD = 60

/**
 * Parst perDomainJson ("[76, 12, ...]") in eine Score-Liste fester Laenge.
 * Positionsgetreu: korrupte/fehlende Eintraege werden zu 0 (Achsen-Zuordnung
 * bleibt erhalten). Reine Funktion (unit-testbar).
 */
internal fun parseDomainScores(json: String?, domainCount: Int): List<Int> {
    if (json.isNullOrBlank()) return List(domainCount) { 0 }
    val inner = json.trim().removePrefix("[").removeSuffix("]")
    if (inner.isBlank()) return List(domainCount) { 0 }
    val parsed = inner.split(",").map { it.trim().toIntOrNull() ?: 0 }
    return List(domainCount) { i -> parsed.getOrElse(i) { 0 } }
}

/**
 * Scheitelpunkt auf Achse [index] fuer einen Score 0..100:
 * Score 0 liegt im Zentrum, 100 am Kreisrand. Achse 0 zeigt nach oben,
 * weitere Achsen im Uhrzeigersinn. Reine Funktion (unit-testbar).
 */
internal fun radarVertex(
    score: Int,
    index: Int,
    axisCount: Int,
    center: Offset,
    radius: Float,
): Offset {
    val fraction = score.coerceIn(0, 100) / 100f
    val angleRad = Math.toRadians(-90.0 + index * 360.0 / axisCount)
    return Offset(
        center.x + (radius * fraction * cos(angleRad)).toFloat(),
        center.y + (radius * fraction * sin(angleRad)).toFloat(),
    )
}

/**
 * 9-Achsen-Radar (Canvas) der Domaenen-Scores 0..100.
 * Schwache Domaenen (< [RADAR_WEAK_THRESHOLD]) werden rot markiert
 * (Achse + Scheitelpunkt). Keine neue Library — reines Compose-Canvas.
 */
@Composable
fun CompetencyRadar(
    scores: List<Int>,
    modifier: Modifier = Modifier,
    axisLabels: List<String> = emptyList(),
) {
    val axisCount = scores.size.coerceAtLeast(1)
    val primary = Color(0xFF1565C0)
    val weak = Color(0xFFC62828)
    val guide = Color(0x33000000)
    val labelPx = with(LocalDensity.current) { 10.sp.toPx() }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f * 0.72f

        // Fuehrungs-Polygone bei 33/66/100 %
        for (level in listOf(0.33f, 0.66f, 1.0f)) {
            val path = Path()
            for (i in 0 until axisCount) {
                val v = radarVertex(100, i, axisCount, center, radius * level)
                if (i == 0) path.moveTo(v.x, v.y) else path.lineTo(v.x, v.y)
            }
            path.close()
            drawPath(path, guide, style = Stroke(width = 1.dp.toPx()))
        }

        // Achsen + Markierung schwacher Domaenen + Daten-Polygon
        val dataPath = Path()
        scores.forEachIndexed { i, score ->
            val isWeak = score < RADAR_WEAK_THRESHOLD
            val edge = radarVertex(100, i, axisCount, center, radius)
            if (isWeak) {
                drawLine(weak, center, edge, strokeWidth = 2.dp.toPx())
            } else {
                drawLine(guide, center, edge, strokeWidth = 1.dp.toPx())
            }
            val v = radarVertex(score, i, axisCount, center, radius)
            if (i == 0) dataPath.moveTo(v.x, v.y) else dataPath.lineTo(v.x, v.y)
            if (isWeak) {
                drawCircle(weak, radius = 4.dp.toPx(), center = v)
            }
        }
        dataPath.close()
        drawPath(dataPath, primary.copy(alpha = 0.22f))
        drawPath(dataPath, primary, style = Stroke(width = 2.dp.toPx()))

        // Achsen-Labels (kurz, aus strings.xml) via nativeCanvas
        val androidCanvas = drawContext.canvas.nativeCanvas
        val paint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = labelPx
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        for (i in 0 until axisCount) {
            val label = axisLabels.getOrNull(i) ?: continue
            val edge = radarVertex(100, i, axisCount, center, radius)
            val dir = edge - center
            val len = dir.getDistance()
            if (len <= 0f) continue
            val pos = center + dir * ((len + labelPx * 0.9f) / len)
            androidCanvas.drawText(label, pos.x, pos.y + labelPx * 0.35f, paint)
        }
    }
}

/**
 * Radar-Karte fuer den GamificationScreen: Titel, Radar-Canvas und eine
 * kompakte Legende (KIKI-Gesamtwert + schwach/strong Domaenen).
 */
@Composable
fun CompetencyRadarCard(
    kiki: Int,
    domainScores: List<Int>,
    modifier: Modifier = Modifier,
) {
    val axisLabels = stringArrayResource(R.array.radar_axes).toList()
    val weakest = domainScores.withIndex()
        .filter { it.value < RADAR_WEAK_THRESHOLD }
        .mapNotNull { axisLabels.getOrNull(it.index) }
    val strongest = domainScores.withIndex()
        .filter { it.value >= RADAR_WEAK_THRESHOLD }
        .maxByOrNull { it.value }
        ?.let { axisLabels.getOrNull(it.index) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.radar_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            CompetencyRadar(
                scores = domainScores,
                axisLabels = axisLabels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.kiki_label, kiki.toString()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (weakest.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.radar_weak_domain, weakest.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (strongest != null) {
                Text(
                    text = stringResource(R.string.radar_strong_domain, strongest),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
