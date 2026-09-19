/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.srs

import ai.ki_kompetenz_training_org.data.repo.SrsQuality
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

/**
 * Lokale SM-2-Lernkarte für den Offline-Fallback.
 * Bewusst Android-frei (pure Kotlin), damit der Algorithmus unit-testbar ist.
 */
@Serializable
data class LocalSrsCard(
    val id: String,
    val question: String,
    val answer: String,
    val lessonId: String,
    val easiness: Double = 2.5,
    val interval: Int = 1,
    val repetitions: Int = 0,
    val nextReview: Long = 0L, // epoch ms; 0 = sofort fällig
)

/**
 * SM-2-Algorithmus (SuperMemo-2) als reine Funktion — keine Android-Imports.
 * Die Uhr wird als [now] übergeben (testbar, deterministisch).
 */
object LocalSrsAlgorithm {

    /** Qualität 1-5 wie im SRS-UI (AGAIN=1 ... PERFECT=5). */
    fun qualityToInt(quality: SrsQuality): Int = when (quality) {
        SrsQuality.AGAIN -> 1
        SrsQuality.HARD -> 2
        SrsQuality.GOOD -> 3
        SrsQuality.EASY -> 4
        SrsQuality.PERFECT -> 5
    }

    /**
     * SM-2: q < 3 -> Wiederholungen zurücksetzen; sonst Intervall verlängern.
     * easiness driftet je nach Qualität und floort bei 1.3.
     */
    fun update(card: LocalSrsCard, quality: SrsQuality, now: Long): LocalSrsCard {
        val q = qualityToInt(quality)
        val repetitions: Int
        val interval: Int
        if (q < 3) {
            repetitions = 0
            interval = 1
        } else {
            repetitions = card.repetitions + 1
            interval = when (repetitions) {
                1 -> 1
                2 -> 6
                else -> (card.interval * card.easiness).roundToInt().coerceAtLeast(1)
            }
        }
        // ponytail: SM-2-GOOD senkt easiness um 0.14 (Standardformel); der
        // Beispielwert "15" im Task ist nur bei konstantem easiness 2.5 erreichbar.
        val easiness = maxOf(
            1.3,
            card.easiness + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)),
        )
        return card.copy(
            easiness = easiness,
            interval = interval,
            repetitions = repetitions,
            nextReview = now + interval * 86_400_000L,
        )
    }
}

/**
 * Lokales Karten-Deck: gebündelte Karten für alle 9 Lern-Domänen.
 * Kein Persistenz-Layer — [BUNDLED_CARDS] ist eine unveränderliche Liste,
 * Fortschritt gilt pro Session. (// ponytail: Persistenz ergänzen, sobald
 * lokale Wiederholungsstände über Sessions hinweg gefordert sind.)
 */
object LocalSrsDeck {

    val BUNDLED_CARDS: List<LocalSrsCard> = listOf(
        // ── Grundlagen der KI ──
        LocalSrsCard(
            id = "local-basics-1", lessonId = "Grundlagen der KI",
            question = "Was ist maschinelles Lernen?",
            answer = "Ein Teilbereich der KI, bei dem Systeme Muster aus Daten lernen, statt explizit programmiert zu werden.",
        ),
        LocalSrsCard(
            id = "local-basics-2", lessonId = "Grundlagen der KI",
            question = "Warum 'halluzinieren' KI-Sprachmodelle?",
            answer = "Sie berechnen wahrscheinliche Wortfolgen statt Wissen — plausible, aber falsche Antworten entstehen ohne Warnung.",
        ),
        LocalSrsCard(
            id = "local-basics-3", lessonId = "Grundlagen der KI",
            question = "Was ist ein KI-Modell?",
            answer = "Ein auf Daten trainierter Algorithmus, der für neue Eingaben Vorhersagen trifft — z. B. ein neuronales Netz.",
        ),

        // ── Datenschutz & DSGVO ──
        LocalSrsCard(
            id = "local-dsgvo-1", lessonId = "Datenschutz & DSGVO",
            question = "Darfst du personenbezogene Daten in ein beliebiges Online-KI-Tool hochladen?",
            answer = "Nur mit Rechtsgrundlage (z. B. Einwilligung) und wenn das Tool DSGVO-konform verarbeitet — nicht in ungeprüften öffentlichen Tools.",
        ),
        LocalSrsCard(
            id = "local-dsgvo-2", lessonId = "Datenschutz & DSGVO",
            question = "Was bedeutet das 'Recht auf Auskunft' laut DSGVO?",
            answer = "Betroffene dürfen erfahren, welche ihrer Daten verarbeitet werden und zu welchem Zweck.",
        ),
        LocalSrsCard(
            id = "local-dsgvo-3", lessonId = "Datenschutz & DSGVO",
            question = "Fallen Chatverläufe mit Namen unter die DSGVO?",
            answer = "Ja — personenbezogene Daten (auch pseudonymisiert) unterliegen der DSGVO; nur echte Anonymisierung fällt nicht darunter.",
        ),

        // ── EU AI Act & Risikoklassen ──
        LocalSrsCard(
            id = "local-aiact-1", lessonId = "EU AI Act & Risikoklassen",
            question = "Welche Risikoklassen definiert der EU AI Act?",
            answer = "Unannehmbares, hohes, begrenztes und minimales Risiko — von Verbot bis nahezu unreguliert.",
        ),
        LocalSrsCard(
            id = "local-aiact-2", lessonId = "EU AI Act & Risikoklassen",
            question = "Was kennzeichnet ein Hochrisiko-KI-System?",
            answer = "Einsatz in Bereichen mit starken Auswirkungen, z. B. Personalentscheidungen oder kritische Infrastruktur — mit strengen Pflichten.",
        ),
        LocalSrsCard(
            id = "local-aiact-3", lessonId = "EU AI Act & Risikoklassen",
            question = "Welche KI ist im EU AI Act verboten?",
            answer = "KI mit unannehmbarem Risiko, z. B. Social Scoring oder manipulative Systeme.",
        ),

        // ── Haftung & Compliance ──
        LocalSrsCard(
            id = "local-haftung-1", lessonId = "Haftung & Compliance",
            question = "Wer haftet für Schäden durch eine KI?",
            answer = "In der Regel Betreiber und Anbieter — die Verantwortung lässt sich nicht pauschal auf das System abwälzen (Produkt- und KI-Haftungsregeln).",
        ),
        LocalSrsCard(
            id = "local-haftung-2", lessonId = "Haftung & Compliance",
            question = "Was bedeutet 'Human Oversight'?",
            answer = "Menschen müssen KI-Entscheidungen kontrollieren und eingreifen können — besonders bei Hochrisiko-Anwendungen.",
        ),
        LocalSrsCard(
            id = "local-haftung-3", lessonId = "Haftung & Compliance",
            question = "Brauchen Unternehmen eine Dokumentation für KI-Systeme?",
            answer = "Für Hochrisiko-Systeme verlangt der EU AI Act Dokumentation und Registrierung — Nachweise erleichtern die Compliance.",
        ),

        // ── KI-Governance im Unternehmen ──
        LocalSrsCard(
            id = "local-governance-1", lessonId = "KI-Governance im Unternehmen",
            question = "Was ist KI-Governance?",
            answer = "Ein Rahmen aus Richtlinien, Rollen und Prozessen, der den verantwortungsvollen KI-Einsatz im Unternehmen steuert.",
        ),
        LocalSrsCard(
            id = "local-governance-2", lessonId = "KI-Governance im Unternehmen",
            question = "Wozu dient eine AI-Policy?",
            answer = "Sie legt fest, welche KI-Tools erlaubt sind, wie Daten behandelt werden und wer verantwortlich ist.",
        ),
        LocalSrsCard(
            id = "local-governance-3", lessonId = "KI-Governance im Unternehmen",
            question = "Wer sollte beim KI-Tool-Einkauf beteiligt sein?",
            answer = "Fachbereich, IT, Datenschutz, Recht und Compliance — nicht nur der Einkauf.",
        ),

        // ── KI-Tools im Arbeitsalltag ──
        LocalSrsCard(
            id = "local-tools-1", lessonId = "KI-Tools im Arbeitsalltag",
            question = "Dürfen Mitarbeitende private KI-Tools für Firmendaten nutzen?",
            answer = "Nur wenn die Unternehmensrichtlinie es erlaubt — Firmendaten gehören in genehmigte, sichere Lösungen.",
        ),
        LocalSrsCard(
            id = "local-tools-2", lessonId = "KI-Tools im Arbeitsalltag",
            question = "Wie prüfst du das Ergebnis eines KI-Textgenerators?",
            answer = "Fakten gegenprüfen, Quellen suchen und auf Halluzinationen sowie Voreingenommenheit achten.",
        ),
        LocalSrsCard(
            id = "local-tools-3", lessonId = "KI-Tools im Arbeitsalltag",
            question = "Was ist Prompt Engineering?",
            answer = "Die gezielte Formulierung von Anweisungen, um präzisere und bessere KI-Antworten zu erhalten.",
        ),

        // ── Transparenzpflichten ──
        LocalSrsCard(
            id = "local-transparenz-1", lessonId = "Transparenzpflichten",
            question = "Muss kenntlich gemacht werden, wenn ein Text von KI stammt?",
            answer = "Ja — bei KI-Interaktion mit Nutzern und bei synthetischen Inhalten gilt eine Offenlegungspflicht (Transparenz).",
        ),
        LocalSrsCard(
            id = "local-transparenz-2", lessonId = "Transparenzpflichten",
            question = "Was verlangt der EU AI Act an Transparenz für Chatbots?",
            answer = "Nutzer müssen darüber informiert werden, dass sie mit einer KI sprechen.",
        ),
        LocalSrsCard(
            id = "local-transparenz-3", lessonId = "Transparenzpflichten",
            question = "Gilt die Transparenzpflicht für alle KI-Modelle gleich?",
            answer = "Nein — besonders Interaktionssysteme und synthetische Inhalte wie Deepfakes sind betroffen.",
        ),

        // ── Erlaubte & verbotene Nutzung ──
        LocalSrsCard(
            id = "local-nutzung-1", lessonId = "Erlaubte & verbotene Nutzung",
            question = "Darf man KI für Prüfungsantworten nutzen?",
            answer = "Nur wenn ausdrücklich erlaubt — unerlaubte Nutzung verstößt gegen Prüfungs- und Arbeitsregeln.",
        ),
        LocalSrsCard(
            id = "local-nutzung-2", lessonId = "Erlaubte & verbotene Nutzung",
            question = "Was ist eine typische verbotene KI-Nutzung im Job?",
            answer = "Das Hochladen vertraulicher Kundendaten in freie Tools ohne Genehmigung.",
        ),
        LocalSrsCard(
            id = "local-nutzung-3", lessonId = "Erlaubte & verbotene Nutzung",
            question = "Wann ist KI-Nutzung am Arbeitsplatz erlaubt?",
            answer = "Wenn sie der Unternehmensrichtlinie entspricht, keine Datenrisiken schafft und Ergebnisse geprüft werden.",
        ),

        // ── KI im erweiterten Kontext ──
        LocalSrsCard(
            id = "local-kontext-1", lessonId = "KI im erweiterten Kontext",
            question = "Wie verändert KI die Rolle von Fachkräften?",
            answer = "Die Arbeit verlagert sich auf Kontrolle, Bewertung und Steuerung von KI-Ergebnissen statt reiner Ausführung.",
        ),
        LocalSrsCard(
            id = "local-kontext-2", lessonId = "KI im erweiterten Kontext",
            question = "Was unterscheidet KI von klassischer Automatisierung?",
            answer = "Automatisierung folgt festen Regeln — KI lernt Muster und passt sich an neue Daten an.",
        ),
        LocalSrsCard(
            id = "local-kontext-3", lessonId = "KI im erweiterten Kontext",
            question = "Welche Kompetenz ist im KI-Zeitalter entscheidend?",
            answer = "KI-Kompetenz (AI Literacy): Chancen und Risiken verstehen und Ergebnisse kritisch bewerten.",
        ),
    )

    /** Alle Karten, deren [LocalSrsCard.nextReview] fällig ist (<= now). */
    fun getDueCards(now: Long, cards: List<LocalSrsCard> = BUNDLED_CARDS): List<LocalSrsCard> =
        cards.filter { it.nextReview <= now }

    /** Ein Review offline anwenden (reine Funktion). */
    fun reviewCard(card: LocalSrsCard, quality: SrsQuality, now: Long): LocalSrsCard =
        LocalSrsAlgorithm.update(card, quality, now)

    private val stateJson = Json { ignoreUnknownKeys = true }
    private val stateSerializer =
        MapSerializer(String.serializer(), LocalSrsCard.serializer())

    /** SM-2-Fortschritt als JSON persistieren (SharedPreferences-Wert). */
    fun serializeState(state: Map<String, LocalSrsCard>): String =
        stateJson.encodeToString(stateSerializer, state)

    /** Gespeicherten Zustand laden; defekt/leer -> kein Fortschritt (still). */
    fun deserializeState(json: String?): Map<String, LocalSrsCard> =
        runCatching { stateJson.decodeFromString(stateSerializer, json ?: "") }.getOrDefault(emptyMap())

    /** Gespeicherte Karten per id ueber die gebuendelten stuelpen. */
    fun mergeState(saved: Map<String, LocalSrsCard>): List<LocalSrsCard> =
        BUNDLED_CARDS.map { saved[it.id] ?: it }
}
