/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.lessons

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * BUG-3 (Gerätetest Pixel 8 Pro, 2026-09-01): "Lessons could not be loaded.
 * Check your connection." obwohl die App 14 vollständige Lektionen BÜNDELT.
 * Der LessonsScreen hängt ausschließlich am Netzwerk-API + Room-Cache; ohne
 * Erreichbarkeit des Servers ist die Liste leer und die gebündelten
 * Lektionen sind unerreichbar.
 *
 * Fix (TDD-RED): Ein `BundledLessons`-Katalog macht die gebündelten
 * Lektionen der Lektionsliste als Offline-Fallback zugänglich.
 */
class BundledLessonsTest {

    @Test
    fun `bündelt genau 14 Lektionen`() {
        assertThat(BundledLessons.all).hasSize(14)
    }

    @Test
    fun `IDs sind lesson-1 bis lesson-14`() {
        val ids = BundledLessons.all.map { it.id }
        assertThat(ids).containsExactlyElementsIn((1..14).map { "lesson-$it" })
    }

    @Test
    fun `Lektionnummern sind 1 bis 14 und eindeutig`() {
        val numbers = BundledLessons.all.map { it.lessonNumber }
        assertThat(numbers).containsExactlyElementsIn((1..14).toList())
    }

    @Test
    fun `jede Lektion hat Titel Beschreibung Dauer und Ziele in beiden Sprachen`() {
        BundledLessons.all.forEach { lesson ->
            assertThat(lesson.titleDe).isNotEmpty()
            assertThat(lesson.titleEn).isNotEmpty()
            assertThat(lesson.descriptionDe).isNotEmpty()
            assertThat(lesson.descriptionEn).isNotEmpty()
            assertThat(lesson.durationMinutes).isGreaterThan(0)
            assertThat(lesson.objectivesDe).isNotEmpty()
            assertThat(lesson.objectivesEn).isNotEmpty()
        }
    }

    @Test
    fun `jede Lektion hat mindestens einen Abschnitt mit Blöcken`() {
        BundledLessons.all.forEach { lesson ->
            assertThat(lesson.sections).isNotEmpty()
            lesson.sections.forEach { section ->
                assertThat(section.blocks).isNotEmpty()
            }
        }
    }

    @Test
    fun `Lektion 1 ist der KI-Grundlagenkurs`() {
        val lesson1 = BundledLessons.byId("lesson-1")
        assertThat(lesson1).isNotNull()
        assertThat(lesson1!!.titleDe).isEqualTo("Was ist Künstliche Intelligenz?")
        assertThat(lesson1.titleEn).isEqualTo("What is Artificial Intelligence?")
    }

    @Test
    fun `byId liefert null für unbekannte IDs`() {
        assertThat(BundledLessons.byId("lesson-99")).isNull()
        assertThat(BundledLessons.byId("")).isNull()
    }

    @Test
    fun `Lektionen enthalten die interaktiven ContentBlock-Typen`() {
        val allBlocks = BundledLessons.all.flatMap { l -> l.sections.flatMap { it.blocks } }
        assertThat(allBlocks.filterIsInstance<ContentBlock.RiskThermometer>()).isNotEmpty()
        assertThat(allBlocks.filterIsInstance<ContentBlock.Quiz>()).isNotEmpty()
        assertThat(allBlocks.filterIsInstance<ContentBlock.KnowledgeCheck>()).isNotEmpty()
    }

    // ── asEntities (Offline-Fallback → LessonsScreen) ────────────────────

    @Test
    fun `asEntities bildet alle 14 Lektionen 1-zu-1 ab`() {
        val entities = BundledLessons.asEntities()
        assertThat(entities).hasSize(14)
        assertThat(entities.map { it.slug }).containsExactlyElementsIn((1..14).map { "lesson-$it" })
    }

    @Test
    fun `asEntities - Slug Titel und Nummer stimmen mit gebündelter Lektion überein`() {
        val entities = BundledLessons.asEntities().associateBy { it.slug }
        BundledLessons.all.forEach { lesson ->
            val e = entities.getValue(lesson.id)
            assertThat(e.title).isEqualTo(lesson.titleDe)
            assertThat(e.lessonNumber).isEqualTo(lesson.lessonNumber)
            assertThat(e.description).isEqualTo(lesson.descriptionDe)
            assertThat(e.duration).isEqualTo("${lesson.durationMinutes} min")
            assertThat(e.body).isNull() // Detail-Body kommt beim Öffnen aus dem Objekt
        }
    }

    @Test
    fun `asEntities - objectivesJson round-trip zu ObjectivesDe`() {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }
        BundledLessons.asEntities().forEachIndexed { index, e ->
            val decoded = json.decodeFromString<List<String>>(e.objectivesJson)
            assertThat(decoded).isEqualTo(BundledLessons.all[index].objectivesDe)
        }
    }

    // ── Cognitive Level (Bloom-Taxonomie) ────────────────────────────────

    @Test
    fun `CognitiveLevel hat 3 Tier mit lokalen Labels`() {
        assertThat(CognitiveLevel.entries).hasSize(3)
        assertThat(CognitiveLevel.FOUNDATION.labelDe).isEqualTo("Grundlagen")
        assertThat(CognitiveLevel.FOUNDATION.labelEn).isEqualTo("Foundation")
        assertThat(CognitiveLevel.APPLICATION.labelDe).isEqualTo("Anwendung")
        assertThat(CognitiveLevel.APPLICATION.labelEn).isEqualTo("Application")
        assertThat(CognitiveLevel.MASTERY.labelDe).isEqualTo("Expertise")
        assertThat(CognitiveLevel.MASTERY.labelEn).isEqualTo("Mastery")
    }

    @Test
    fun `jede Lektion hat einen CognitiveLevel`() {
        BundledLessons.all.forEach { lesson ->
            assertThat(lesson.cognitiveLevel).isNotNull()
        }
    }

    @Test
    fun `Grundlagen-Lektionen sind FOUNDATION`() {
        val foundationLessons = BundledLessons.all.filter {
            it.cognitiveLevel == CognitiveLevel.FOUNDATION
        }.map { it.lessonNumber }
        // L1-L8 (Grundlagen) + L13 (LLM-Konzepte, verstehen-basiert)
        assertThat(foundationLessons).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 13)
    }

    @Test
    fun `Anwendungs-Lektionen sind APPLICATION`() {
        val applicationLessons = BundledLessons.all.filter {
            it.cognitiveLevel == CognitiveLevel.APPLICATION
        }.map { it.lessonNumber }
        // L9 (Strategie/ROI), L10 (Prompt Engineering), L11 (Audit), L14 (Green AI)
        assertThat(applicationLessons).containsExactly(9, 10, 11, 14)
    }

    @Test
    fun `Lektion 12 Change Management ist MASTERY`() {
        val lesson12 = BundledLessons.byId("lesson-12")!!
        assertThat(lesson12.cognitiveLevel).isEqualTo(CognitiveLevel.MASTERY)
    }
}
