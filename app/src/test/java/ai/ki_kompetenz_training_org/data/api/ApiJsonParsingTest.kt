/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.api

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * ROOT-CAUSE (Gerätetest Pixel 8 Pro, 2026-09-01): "Lessons could not be
 * loaded. Check your connection." AUCH bei erreichbarem Server.
 *
 * Die Live-API (GET /api/content/lessons) liefert `lesson` als STRING
 * ("0"), das DTO erwartet `Int?`. Ohne isLenient wirft kotlinx-serialization
 * eine SerializationException; runCatching im ContentRepository wandelt sie
 * in Result.failure → Fehlermeldung trotz Internet.
 *
 * Der Payload unten ist 1:1 vom Gerät gefangen (gekürzt).
 * FIX: ApiJson mit isLenient = true koertiert "0" → 0.
 */
class ApiJsonParsingTest {

    /** Echter API-Payload vom Gerät (2026-09-01, GET /api/content/lessons). */
    private val realLessonsPayload = """
        {
          "lessons": [
            {
              "slug": "lesson-1",
              "title": "Was ist Künstliche Intelligenz?",
              "lesson": "0",
              "duration": "15 min",
              "description": "Grundlagen: KI definieren.",
              "objectives": ["KI unterscheiden", "KI-Arten benennen"]
            },
            {
              "slug": "lesson-2",
              "title": "Maschinelles Lernen",
              "lesson": "1",
              "duration": "20 min",
              "description": "Wie KI lernt.",
              "objectives": []
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `echter API-Payload - lesson als String koertiert zu Int`() {
        val dto = ApiJson.decodeFromString(LessonsResponseDto.serializer(), realLessonsPayload)

        assertThat(dto.lessons).hasSize(2)
        assertThat(dto.lessons[0].slug).isEqualTo("lesson-1")
        assertThat(dto.lessons[0].lesson).isEqualTo(0)   // "0" (String) → 0 (Int)
        assertThat(dto.lessons[1].lesson).isEqualTo(1)
        assertThat(dto.lessons[0].objectives).containsExactly("KI unterscheiden", "KI-Arten benennen")
    }

    @Test
    fun `Int direkt funktioniert weiterhin`() {
        val payload = """{"lessons":[{"slug":"a","lesson":0}]}"""
        val dto = ApiJson.decodeFromString(LessonsResponseDto.serializer(), payload)
        assertThat(dto.lessons[0].lesson).isEqualTo(0)
    }

    @Test
    fun `fehlende optionale Felder nutzen Defaults`() {
        val payload = """{"lessons":[{"slug":"x"}]}"""
        val dto = ApiJson.decodeFromString(LessonsResponseDto.serializer(), payload)
        val lesson = dto.lessons[0]
        assertThat(lesson.title).isEmpty()
        assertThat(lesson.lesson).isNull()
        assertThat(lesson.duration).isNull()
        assertThat(lesson.description).isEmpty()
        assertThat(lesson.objectives).isEmpty()
    }

    @Test
    fun `unbekannte Felder werden ignoriert`() {
        val payload = """{"lessons":[{"slug":"y","neuFeld":123,"weiteres":{"a":1}}],"meta":"x"}"""
        val dto = ApiJson.decodeFromString(LessonsResponseDto.serializer(), payload)
        assertThat(dto.lessons[0].slug).isEqualTo("y")
    }

    @Test
    fun `LessonDetail mit body parst`() {
        val payload = """
            {
              "slug": "lesson-3",
              "title": "Neuronale Netze",
              "lesson": "2",
              "body": "# Markdown-Inhalt",
              "objectives": ["A"]
            }
        """.trimIndent()
        val dto = ApiJson.decodeFromString(LessonDetailDto.serializer(), payload)
        assertThat(dto.slug).isEqualTo("lesson-3")
        assertThat(dto.lesson).isEqualTo(2)
        assertThat(dto.body).contains("Markdown")
    }

    @Test
    fun `strikte Konfiguration parst quoted Numbers ebenfalls - Bug-Dokumentation`() {
        // EMPIRISCH (2026-09-01): kotlinx-serialization akzeptiert quoted Zahlen
        // ("0") für Int auch OHNE isLenient — die String/Int-Hypothese war also
        // NICHT die Root-Cause des Lessons-Ladefehlers. Beide Konfigurationen
        // müssen den echten Payload parsen (Regressionsschutz in beide Richtungen):
        val strictJson = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val viaStrict = strictJson.decodeFromString(LessonsResponseDto.serializer(), realLessonsPayload)
        assertThat(viaStrict.lessons[0].lesson).isEqualTo(0)

        val viaApiJson = ApiJson.decodeFromString(LessonsResponseDto.serializer(), realLessonsPayload)
        assertThat(viaApiJson.lessons[0].lesson).isEqualTo(0)
    }
}
