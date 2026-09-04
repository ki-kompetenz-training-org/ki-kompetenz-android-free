/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.home

import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.ki_kompetenz_training_org.R

/**
 * KIKI-Delta zwischen den beiden juengsten Snapshots:
 * null bei weniger als 2 Snapshots. Reine Funktion (unit-testbar).
 */
internal fun kikiDelta(snapshots: List<CompetencySnapshotEntity>): Int? {
    if (snapshots.size < 2) return null
    val sorted = snapshots.sortedBy { it.weekKey }
    val prev = sorted[sorted.size - 2].kiki
    val last = sorted.last().kiki
    return last - prev
}

/**
 * Mappt KIKI-Werte (0..100, chronologisch) auf Polyline-Punkte ueber
 * [width]x[height] mit [padding]-Rand. Hoeherer Wert = hoeher (kleineres y).
 * Reine Funktion (unit-testbar).
 */
internal fun sparklinePoints(
    values: List<Int>,
    width: Float,
    height: Float,
    padding: Float,
): List<Offset> {
    if (values.isEmpty()) return emptyList()
    val usableW = (width - 2 * padding).coerceAtLeast(0f)
    val usableH = (height - 2 * padding).coerceAtLeast(0f)
    return values.mapIndexed { i, v ->
        val x = if (values.size == 1) width / 2f else padding + i * usableW / (values.size - 1)
        val fraction = (v.coerceIn(0, 100)) / 100f
        val y = height - padding - fraction * usableH
        Offset(x, y)
    }
}

/**
 * Sparkline-Karte fuer den HomeScreen: KIKI-Trend ueber die letzten
 * <=8 Wochensnapshots mit Delta-Badge (gruen/rot) — z. B. "54 -> 61 (+7)".
 * Ohne Daten erscheint ein Empty-State-Hinweis.
 */
@Composable
fun KikiSparklineCard(
    snapshots: List<CompetencySnapshotEntity>,
    modifier: Modifier = Modifier,
) {
    val line = Color(0xFF1565C0)
    val fill = Color(0xFF1565C0).copy(alpha = 0.15f)

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.kiki_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            if (snapshots.isEmpty()) {
                Text(
                    text = stringResource(R.string.kiki_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            val chronological = snapshots.sortedBy { it.weekKey }
            val values = chronological.map { it.kiki }
            val delta = kikiDelta(chronological)
            val latest = values.last()

            Row(verticalAlignment = Alignment.CenterVertically) {
                // "54 -> 61" bzw. nur "61" bei einem Snapshot
                val trendText = if (values.size >= 2) {
                    stringResource(
                        R.string.kiki_delta,
                        values[values.size - 2].toString(),
                        latest.toString(),
                        if (delta != null && delta >= 0) "+${delta}" else "${delta ?: 0}",
                    )
                } else {
                    latest.toString()
                }
                Text(
                    text = trendText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.width(8.dp))
                if (delta != null) {
                    Badge(
                        containerColor = if (delta >= 0) {
                            Color(0xFF2E7D32)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        contentColor = Color.White,
                    ) {
                        Text(if (delta >= 0) "+$delta" else "$delta")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            ) {
                val points = sparklinePoints(values, size.width, size.height, 12f)
                if (points.isEmpty()) return@Canvas
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    // Flaeche unter der Kurve
                    val area = Path().apply {
                        addPath(path)
                        lineTo(points.last().x, size.height)
                        lineTo(points.first().x, size.height)
                        close()
                    }
                    drawPath(area, fill)
                    drawPath(path, line, style = Stroke(width = 3.dp.toPx()))
                }
                // Punkt am neuesten Wert
                drawCircle(line, radius = 5.dp.toPx(), center = points.last())
            }
        }
    }
}
