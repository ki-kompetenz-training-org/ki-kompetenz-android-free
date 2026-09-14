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
 * Zertifikat-Export (gratis) — ponytail: premium gate only if monetization requires it.
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
        dateIso: String,
        localeTag: String,
    ): List<String> {
        require(perDomain.size == 9 && domains.size == 9) {
            "expected 9 domains, got perDomain=${perDomain.size} domains=${domains.size}"
        }
        val en = localeTag.startsWith("en", ignoreCase = true)
        return buildList {
            add(if (en) "AI Literacy Certificate" else "KI-Kompetenz-Nachweis")
            add("Name: ${name.ifBlank { "-" }}")
            add(if (en) "Date: $dateIso" else "Datum: $dateIso")
            add(if (en) "AI Score (KIKI): $kiki/100" else "KI-Score (KIKI): $kiki/100")
            domains.forEachIndexed { i, domain -> add("$domain: ${perDomain[i]}/100") }
            add(if (en) "Based on EU AI Act Art. 4 - AI literacy" else "Grundlage: EU AI Act Art. 4 - KI-Kompetenz")
            add("https://ki-kompetenz-training.org")
        }
    }
}

/** Parst perDomainJson ("[72, 60, ...]") in eine 9er Score-Liste — delegiert an parseDomainScores. */
fun parsePerDomain(json: String): List<Int> = parseDomainScores(json, 9)

/** Duenner PdfDocument-Wrapper: A4, Zeilen top-down, Titelzeile fett/groesser. */
fun writePdf(lines: List<String>, out: OutputStream) {
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
    val canvas = page.canvas
    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12f }
    val title = Paint(body).apply {
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    var y = 60f
    lines.forEachIndexed { i, line ->
        val paint = if (i == 0) title else body
        canvas.drawText(line, 50f, y, paint)
        y += if (i == 0) 34f else 22f
    }
    doc.finishPage(page)
    doc.writeTo(out)
    doc.close()
}
