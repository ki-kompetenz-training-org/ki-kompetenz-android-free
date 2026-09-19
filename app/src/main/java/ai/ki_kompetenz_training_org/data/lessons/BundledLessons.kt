/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import ai.ki_kompetenz_training_org.data.db.LessonEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Offline-Katalog der in der App gebündelten interaktiven Lektionen.
 *
 * Hintergrund (BUG 2026-09-01): Der LessonsScreen lud seine Liste nur aus
 * dem Netzwerk-API (+ Room-Cache). Ohne Serververbindung blieb die Liste
 * leer ("Lessons could not be loaded"), obwohl alle Lektionsinhalte lokal
 * verfügbar sind. Der Katalog dient als Fallback und Quelle der Wahrheit
 * für die gebündelten Lektionen.
 */
object BundledLessons {

    val all: List<InteractiveLesson> = listOf(
        Lesson1.lesson.copy(primaryDomain = "Grundlagen der KI"),
        Lesson2.lesson.copy(primaryDomain = "Grundlagen der KI"),
        Lesson3.lesson.copy(primaryDomain = "Grundlagen der KI"),
        Lesson4.lesson.copy(primaryDomain = "Datenschutz & DSGVO"),
        Lesson5.lesson.copy(primaryDomain = "Grundlagen der KI"),
        Lesson6.lesson.copy(primaryDomain = "KI-Tools im Arbeitsalltag"),
        Lesson7.lesson.copy(primaryDomain = "Grundlagen der KI"),
        Lesson8.lesson.copy(primaryDomain = "Erlaubte & verbotene Nutzung"),
        Lesson9.lesson.copy(primaryDomain = "EU AI Act & Risikoklassen"),
        Lesson10.lesson.copy(primaryDomain = "KI-Tools im Arbeitsalltag"),
        Lesson11.lesson.copy(primaryDomain = "KI-Governance im Unternehmen"),
        Lesson12.lesson.copy(primaryDomain = "Transparenzpflichten"),
        Lesson13.lesson.copy(primaryDomain = "Grundlagen der KI"),
        Lesson14.lesson.copy(primaryDomain = "KI-Tools im Arbeitsalltag"),
        Lesson15.lesson.copy(primaryDomain = "Haftung & Compliance"),
        Lesson16.lesson.copy(primaryDomain = "KI im erweiterten Kontext"),
    )

    /**
     * Lern-Loop: erste (niedrigste Nummer) unabgeschlossene Lektion einer
     * Kompetenz-Domaene. Alle abgeschlossen -> niedrigste zur Wiederholung.
     * Keine Lektion in der Domaene -> null (Aufrufer zeigt Uebersicht).
     */
    fun firstLessonForDomain(domain: String, completedIds: Set<String> = emptySet()): InteractiveLesson? {
        val ofDomain = all.filter { it.primaryDomain == domain }
        if (ofDomain.isEmpty()) return null
        return ofDomain.filter { it.id !in completedIds }.minByOrNull { it.lessonNumber }
            ?: ofDomain.minByOrNull { it.lessonNumber }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val byIdMap: Map<String, InteractiveLesson> by lazy {
        all.associateBy { it.id }
    }

    /** Lektion per ID (z. B. "lesson-1") oder null. */
    fun byId(id: String): InteractiveLesson? = byIdMap[id]

    /**
     * Konvertiert gebündelte Lektionen in [LessonEntity]-Liste (ohne body),
     * kompatibel zum Room-Schema des LessonsScreen.
     */
    fun asEntities(): List<LessonEntity> = all.map { lesson ->
        LessonEntity(
            slug = lesson.id,
            title = lesson.titleDe,
            lessonNumber = lesson.lessonNumber,
            duration = "${lesson.durationMinutes} min",
            description = lesson.descriptionDe,
            objectivesJson = json.encodeToString(
                ListSerializer(kotlinx.serialization.serializer<String>()),
                lesson.objectivesDe,
            ),
            body = null,
        )
    }
}
