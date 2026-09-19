/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ki_kompetenz_training_org.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Score-Schwelle, ab der eine Domaene als "schwach" (rot) markiert wird. */
internal const val RADAR_WEAK_THRESHOLD = 60

/**
 * Anzeige-Floor in Prozent: Scheitelpunkte mit Score 0 liegen sonst ALLE exakt
 * im Zentrum — mit wenigen Datenpunkten entartet das Polygon zu einer Nadel
 * (PXL-Befund 2026-09-18, KIKI 10 mit 8 Null-Domaenen).
 *
 * ponytail: reiner Anzeige-Floor, Rechen-/Zertifikatsdaten bleiben unberuehrt;
 * echten Verlauf verzerren Werte < 12 optisch kaum.
 */
internal const val RADAR_MIN_DISPLAY = 6

/** Hebt Scores unter [RADAR_MIN_DISPLAY] fuer die ZEICHNUNG auf den Floor an. */
internal fun radarDisplayScores(scores: List<Int>): List<Int> =
    scores.map { maxOf(it, RADAR_MIN_DISPLAY) }

/**
 * KIKI-Tier (Emoji + Titel). Spiegelt die Schwellen aus KiScoreFallback
 * (tiers 0/21/41/61/81) lokal nach, damit die Radar-Karte ohne Netz-Content
 * labeln kann. Reine Funktion (unit-testbar).
 */
internal fun kikiTier(kiki: Int): Pair<String, String> = when (kiki.coerceIn(0, 100)) {
    in 0..20 -> "🌱" to "KI-Laie"
    in 21..40 -> "🔍" to "KI-Entdecker"
    in 41..60 -> "⚙️" to "KI-Praktiker"
    in 61..80 -> "💡" to "KI-Profi"
    else -> "🚀" to "KI-Visionär"
}

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
 * Glaettet das Daten-Polygon zu einem geschlossenen Bézier-Ring: pro Scheitelpunkt
 * ein Segment (Start, Kontrollpunkt=Scheitelpunkt, Ende) ueber die Kanten-
 * Mittelpunkte — der klassische weiche Radar-Look ohne Miter-Spiesschen.
 * Reine Funktion (unit-testbar); < 3 Punkte => leer (Aufrufer faellt auf Linien zurueck).
 */
internal fun radarSmoothSegments(vertices: List<Offset>): List<Triple<Offset, Offset, Offset>> {
    if (vertices.size < 3) return emptyList()
    fun mid(a: Offset, b: Offset) = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)
    return vertices.indices.map { i ->
        val prev = vertices[(i + vertices.size - 1) % vertices.size]
        val v = vertices[i]
        val next = vertices[(i + 1) % vertices.size]
        Triple(mid(prev, v), v, mid(v, next))
    }
}

/**
 * 9-Achsen-Radar (Canvas) der Domaenen-Scores 0..100.
 * Schwache Domaenen (< [RADAR_WEAK_THRESHOLD]) werden rot markiert
 * (Achse + Scheitelpunkt). Keine neue Library — reines Compose-Canvas.
 * [semanticDescription] sorgt fuer TalkBack-Zugaenglichkeit (Canvas hat
 * sonst keine Semantik).
 */
@Composable
fun CompetencyRadar(
    scores: List<Int>,
    modifier: Modifier = Modifier,
    axisLabels: List<String> = emptyList(),
    semanticDescription: String = "",
) {
    val axisCount = scores.size.coerceAtLeast(1)
    val primary = Color(0xFF1565C0)
    val weak = Color(0xFFC62828)
    val guide = Color(0x33000000)
    val labelPx = with(LocalDensity.current) { 10.sp.toPx() }

    // Polygon waechst beim ersten Erscheinen weich aus dem Zentrum
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    val radiusFactor = progress.value

    Canvas(modifier = modifier.semantics { contentDescription = semanticDescription }) {
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

        // Achsen + Markierung schwacher Domaenen
        scores.forEachIndexed { i, score ->
            val isWeak = score < RADAR_WEAK_THRESHOLD
            val edge = radarVertex(100, i, axisCount, center, radius)
            if (isWeak) {
                drawLine(weak, center, edge, strokeWidth = 2.dp.toPx())
            } else {
                drawLine(guide, center, edge, strokeWidth = 1.dp.toPx())
            }
        }

        // Daten-Polygon: geglaettet, mit Display-Floor gegen Zentrums-Kollaps
        val dataRadius = radius * radiusFactor
        val dataPath = Path()
        val vertices = radarDisplayScores(scores)
            .mapIndexed { i, s -> radarVertex(s, i, axisCount, center, dataRadius) }
        val segments = radarSmoothSegments(vertices)
        if (segments.isNotEmpty()) {
            dataPath.moveTo(segments.first().first.x, segments.first().first.y)
            segments.forEach { (_, control, end) ->
                dataPath.quadraticBezierTo(control.x, control.y, end.x, end.y)
            }
            dataPath.close()
        } else {
            vertices.forEachIndexed { i, v ->
                if (i == 0) dataPath.moveTo(v.x, v.y) else dataPath.lineTo(v.x, v.y)
            }
            dataPath.close()
        }
        drawPath(dataPath, primary.copy(alpha = 0.22f))
        drawPath(
            dataPath,
            primary,
            style = Stroke(
                width = 2.dp.toPx(),
                join = StrokeJoin.Round,
                cap = StrokeCap.Round,
            ),
        )

        // rote Punkte schwacher Domaenen sitzen auf dem (gefloorten) Polygon
        scores.forEachIndexed { i, score ->
            if (score < RADAR_WEAK_THRESHOLD) {
                val v = vertices[i]
                drawCircle(weak, radius = 4.dp.toPx(), center = v)
            }
        }

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
 * kompakte Legende (KIKI-Gesamtwert + Tier, Delta zum Vor-Snapshot,
 * schwach/stark Domaenen).
 */
/**
 * Lern-Loop-CTA: schwächste Domäne unter [RADAR_WEAK_THRESHOLD] mit vorhandenem
 * Achsen-Label — null, wenn nichts schwach ist (kein Button).
 */
fun radarCtaDomain(domainScores: List<Int>, axisLabels: List<String>): String? =
    domainScores.withIndex()
        .filter { it.value < RADAR_WEAK_THRESHOLD }
        .minByOrNull { it.value }
        ?.let { axisLabels.getOrNull(it.index) }

@Composable
fun CompetencyRadarCard(
    kiki: Int,
    domainScores: List<Int>,
    modifier: Modifier = Modifier,
    previousKiki: Int? = null,
    onOpenLesson: (domain: String) -> Unit = {},
) {
    val axisLabels = stringArrayResource(R.array.radar_axes).toList()
    val weakest = domainScores.withIndex()
        .filter { it.value < RADAR_WEAK_THRESHOLD }
        .mapNotNull { axisLabels.getOrNull(it.index) }
    val strongest = domainScores.withIndex()
        .filter { it.value >= RADAR_WEAK_THRESHOLD }
        .maxByOrNull { it.value }
        ?.let { axisLabels.getOrNull(it.index) }
    val (tierEmoji, tierTitle) = kikiTier(kiki)
    val radarDesc = remember(domainScores, axisLabels) {
        domainScores.mapIndexed { i, s -> "${axisLabels.getOrNull(i) ?: "?"} $s%" }
            .joinToString(", ")
    }

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
                semanticDescription = stringResource(R.string.radar_title) + ": " + radarDesc,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.kiki_label, kiki.toString()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$tierEmoji $tierTitle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (previousKiki != null && previousKiki != kiki) {
                val diff = kiki - previousKiki
                val sign = if (diff > 0) "+" else "−"
                Text(
                    text = stringResource(R.string.radar_delta, sign + abs(diff).toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (diff > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                )
            }
            if (weakest.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.radar_weak_domain, weakest.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            radarCtaDomain(domainScores, axisLabels)?.let { ctaDomain ->
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onOpenLesson(ctaDomain) }) {
                    Text(stringResource(R.string.radar_open_lesson, ctaDomain))
                }
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
