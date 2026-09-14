/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

/**
 * Zertifikat-Export — Premium-Gated (siehe GamificationScreen: Auth + Subscription).
 * [buildLines]/[parsePerDomain] sind rein und unit-testbar; [writePdf] ist ein duenner
 * android.graphics-Wrapper (nicht unit-testbar, kein Robolectric).
 */
object CertificateContent {

    /** Baut die Zertifikat-Textzeilen (DE Standard, EN ab localeTag "en*"). */
    fun buildLines(
        kiki: Int,
        perDomain: List<Int>,
        domains: List<String>,
        name: String,
        date: String,
        localeTag: String,
    ): List<String> {
        require(perDomain.size == 9 && domains.size == 9) {
            "expected 9 domains, got perDomain=${perDomain.size} domains=${domains.size}"
        }
        val en = localeTag.startsWith("en", ignoreCase = true)
        return buildList {
            add(if (en) "AI Literacy Certificate" else "KI-Kompetenz-Nachweis")
            add("Name: ${name.ifBlank { "-" }}")
            add(if (en) "Date: $date" else "Datum: $date")
            add(if (en) "AI Score (KIKI): $kiki/100" else "KI-Score (KIKI): $kiki/100")
            domains.forEachIndexed { i, domain -> add("$domain: ${perDomain[i]}/100") }
            add(if (en) "Based on EU AI Act Art. 4 - AI literacy" else "Grundlage: EU AI Act Art. 4 - KI-Kompetenz")
            add("https://ki-kompetenz-training.org")
        }
    }
}

/** Parst perDomainJson ("[72, 60, ...]") in eine 9er Score-Liste — delegiert an parseDomainScores. */
fun parsePerDomain(json: String): List<Int> = parseDomainScores(json, 9)

/**
 * Duenner PdfDocument-Wrapper: A4 mit Rahmen, zentriertem Titel, Trennlinie,
 * Footer unten. Positionskontrakt (gesichert durch CertificateContentTest):
 * Zeile 0 = Titel, letzte zwei Zeilen = Fusszeile, mind. 4 Zeilen overall.
 */
fun writePdf(lines: List<String>, out: OutputStream) {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val canvas = page.canvas
    val w = 595f

    val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = 0xFF9E9E9E.toInt()
    }
    canvas.drawRect(40f, 40f, 555f, 802f, frame)

    fun centered(text: String, y: Float, paint: Paint) =
        canvas.drawText(text, (w - paint.measureText(text)) / 2f, y, paint)

    val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = 0xFF1A237E.toInt()
    }
    val strong = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
    val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f; color = 0xFF666666.toInt() }

    centered(lines[0], 120f, title)
    canvas.drawLine(150f, 140f, 445f, 140f, frame)

    val bodyEnd = lines.size - 2
    var y = 185f
    for (i in 1 until bodyEnd) {
        val paint = if (i == 1) strong else body
        canvas.drawText(lines[i], 70f, y, paint)
        y += if (i == 1) 30f else 24f
    }

    centered(lines[lines.size - 2], 756f, footer)
    centered(lines.last(), 776f, footer)

    doc.finishPage(page)
    doc.writeTo(out)
    doc.close()
}
