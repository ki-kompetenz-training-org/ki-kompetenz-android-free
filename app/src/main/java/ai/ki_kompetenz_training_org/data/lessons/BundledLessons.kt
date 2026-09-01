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
        Lesson1.lesson,
        Lesson2.lesson,
        Lesson3.lesson,
        Lesson4.lesson,
        Lesson5.lesson,
        Lesson6.lesson,
        Lesson7.lesson,
        Lesson8.lesson,
        Lesson9.lesson,
        Lesson10.lesson,
        Lesson11.lesson,
        Lesson12.lesson,
        Lesson13.lesson,
        Lesson14.lesson,
    )

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
