/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.ContentDao
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * BUG-3 (Gerätetest Pixel 8 Pro, 2026-09-01): Offline zeigt der
 * LessonsScreen die Fehlermeldung "Lessons could not be loaded" obwohl die
 * App 14 Lektionen bündelt. Der ViewModel muss bei fehlgeschlagenem
 * Netz-Fetch UND leerem Room-Cache auf `BundledLessons` zurückfallen und
 * `loadFailed` NICHT setzen, solange gebündelte Inhalte verfügbar sind.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LessonsViewModelFallbackTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var contentRepository: ContentRepository
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var gamificationRepository: GamificationRepository
    private lateinit var settingsStore: SettingsStore
    private lateinit var db: AppDatabase
    private lateinit var dao: ContentDao

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic("ai.ki_kompetenz_training_org.data.minigames.MiniGamesKt")
        every { currentLang(any()) } returns "de"

        contentRepository = mockk(relaxed = true)
        premiumRepository = mockk(relaxed = true)
        gamificationRepository = mockk(relaxed = true)
        settingsStore = mockk(relaxed = true)
        db = mockk(relaxed = true)
        dao = mockk(relaxed = true)

        every { db.contentDao() } returns dao
        // WICHTIG: Direkt am Repository-Mock stubben (nicht am DAO) — ein
        // relaxed Mock des Repositorys liefert sonst einen nie-emittierenden
        // Mock-Flow, und der ViewModel-Collect hängt leer.
        every { contentRepository.observeLessons() } returns flowOf(emptyList())
        every { settingsStore.lastLesson } returns flowOf(null)
        every { gamificationRepository.observeLessonProgress() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVm() = LessonsViewModel(
        contentRepository = contentRepository,
        premiumRepository = premiumRepository,
        gamificationRepository = gamificationRepository,
        settingsStore = settingsStore,
    )

    @Test
    fun `Offline ohne Cache - fällt auf gebündelte Lektionen zurück statt Fehler`() = runTest(dispatcher) {
        // API nicht erreichbar:
        coEvery { contentRepository.fetchLessons(any()) } returns Result.failure(java.io.IOException("offline"))

        val vm = createVm()
        advanceUntilIdle()

        val state = vm.state.value
        // KEIN Fehler: gebündelte Lektionen sind vorhanden
        assertThat(state.loadFailed).isFalse()
        assertThat(state.lessons).hasSize(14)
        assertThat(state.lessons.map { it.slug }).contains("lesson-1")
        assertThat(state.loading).isFalse()
    }

    @Test
    fun `Offline ohne Cache - gebündelte Fallback-Entity ist wohlgeformt`() = runTest(dispatcher) {
        coEvery { contentRepository.fetchLessons(any()) } returns Result.failure(java.io.IOException("offline"))

        val vm = createVm()
        advanceUntilIdle()

        val first = vm.state.value.lessons.first { it.slug == "lesson-1" }
        assertThat(first.title).isNotEmpty()
        assertThat(first.description).isNotEmpty()
        assertThat(first.lessonNumber).isEqualTo(1)
        assertThat(first.objectivesJson).contains("[")
        assertThat(first.body).isNull() // Detail-Body kommt beim Öffnen aus dem gebündelten Objekt
    }

    @Test
    fun `Online-Erfolg übersteuert den gebündelten Katalog`() = runTest(dispatcher) {
        val remote = listOf(
            ai.ki_kompetenz_training_org.data.api.LessonSummaryDto(
                slug = "lesson-kids",
                title = "KI für Kinder",
                lesson = 0,
                duration = "10 min",
                description = "Spielerisch lernen",
                objectives = listOf("A", "B"),
            ),
        )
        coEvery { contentRepository.fetchLessons(any()) } returns Result.success(remote)
        coEvery { contentRepository.fetchLesson(any(), any()) } returns Result.failure(java.io.IOException())
        // Room liefert nach Upsert den Remote-Stand (direkt am Repository-Mock):
        every { contentRepository.observeLessons() } returns flowOf(
            listOf(
                ai.ki_kompetenz_training_org.data.db.LessonEntity(
                    slug = "lesson-kids",
                    title = "KI für Kinder",
                    lessonNumber = 0,
                    duration = "10 min",
                    description = "Spielerisch lernen",
                    objectivesJson = """["A","B"]""",
                    body = null,
                ),
            ),
        )

        val vm = createVm()
        advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.loadFailed).isFalse()
        assertThat(state.lessons).hasSize(1)
        assertThat(state.lessons[0].slug).isEqualTo("lesson-kids")
    }

    @Test
    fun `retry setzt loadFailed zurück und lädt erneut`() = runTest(dispatcher) {
        coEvery { contentRepository.fetchLessons(any()) } returns
            Result.failure(java.io.IOException("offline")) andThen
            Result.failure(java.io.IOException("still offline"))

        val vm = createVm()
        advanceUntilIdle()

        // Mit Fallback soll NICHT failed gemeldet werden:
        assertThat(vm.state.value.loadFailed).isFalse()

        vm.retry()
        advanceUntilIdle()
        assertThat(vm.state.value.loadFailed).isFalse()
        assertThat(vm.state.value.lessons).hasSize(14)
    }
}
