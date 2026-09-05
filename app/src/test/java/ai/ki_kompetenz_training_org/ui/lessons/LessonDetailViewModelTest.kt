/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.ui.common.UiError
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit-Tests für [LessonDetailViewModel] — Quiz-Scoring und Abschluss-Guard.
 *
 * Kontext BUG-Report 2026-09-05: Der "Test bestehen, um abzuschließen"-
 * Button war deaktiviert (Sackgasse) und der Abschluss musste durch den
 * Guard in markCompleted() geschützt sein. Diese Tests fixieren das
 * Scoring (>= 60 %) und den Guard unabhängig von der UI.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LessonDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private lateinit var contentRepository: ContentRepository
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var gamificationRepository: GamificationRepository

    private val lessonDto = LessonDetailDto(
        slug = "lesson-1",
        title = "Grundlagen der KI",
        lesson = 1,
        duration = "20 min",
        description = "KI definieren und abgrenzen",
        objectives = listOf("KI unterscheiden"),
        body = "<p>Inhalt</p>",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        contentRepository = mockk()
        premiumRepository = mockk(relaxed = true)
        gamificationRepository = mockk(relaxed = true)
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns
            Result.success(lessonDto)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(slug: String = "lesson-1") = LessonDetailViewModel(
        slug = slug,
        contentRepository = contentRepository,
        premiumRepository = premiumRepository,
        gamificationRepository = gamificationRepository,
        coroutineDispatcher = dispatcher,
    )

    // ── Load ─────────────────────────────────────────────────────────────

    @Test
    fun `load Erfolg - Lesson gesetzt, Quiz geladen (3 Fragen fuer lesson-1), kein Fehler`() {
        val vm = viewModel()

        val s = vm.state.value
        assertThat(s.loading).isFalse()
        assertThat(s.error).isNull()
        assertThat(s.lesson?.slug).isEqualTo("lesson-1")
        assertThat(s.quizQuestions).hasSize(3)
    }

    @Test
    fun `load Failure - UiError LESSON_LOAD und loading beendet`() {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns
            Result.failure(Exception("offline"))

        val vm = viewModel()

        assertThat(vm.state.value.loading).isFalse()
        assertThat(vm.state.value.error).isEqualTo(UiError.LESSON_LOAD)
        assertThat(vm.state.value.lesson).isNull()
    }

    // ── submitAnswer / Scoring ───────────────────────────────────────────

    @Test
    fun `submitAnswer - korrekte Antwort addiert Frage-Punkte, noch nicht bestanden`() {
        val vm = viewModel()
        vm.startQuiz()

        vm.submitAnswer("q1", selectedOptionIndex = 0) // 25 Punkte

        val s = vm.state.value
        assertThat(s.currentScore).isEqualTo(25)
        assertThat(s.isTestPassed).isFalse() // 25 von 100 = 25 % < 60 %
        assertThat(s.showQuiz).isTrue()
    }

    @Test
    fun `submitAnswer - falsche Antwort addiert 0 Punkte`() {
        val vm = viewModel()
        vm.startQuiz()

        vm.submitAnswer("q1", selectedOptionIndex = 2) // falsch (correct = 0)

        assertThat(vm.state.value.currentScore).isEqualTo(0)
        assertThat(vm.state.value.isTestPassed).isFalse()
    }

    @Test
    fun `submitAnswer - alle 3 korrekt = 100 Punkte → Test bestanden`() {
        val vm = viewModel()

        vm.submitAnswer("q1", 0) // 25
        vm.submitAnswer("q2", 3) // 25
        vm.submitAnswer("q3", 1) // 50

        val s = vm.state.value
        assertThat(s.currentScore).isEqualTo(100)
        assertThat(s.isTestPassed).isTrue()
    }

    @Test
    fun `submitAnswer - nur 1 von 3 korrekt (25 Prozent) → nicht bestanden`() {
        val vm = viewModel()

        vm.submitAnswer("q1", 0)
        vm.submitAnswer("q2", 0)
        vm.submitAnswer("q3", 0)

        assertThat(vm.state.value.currentScore).isEqualTo(25)
        assertThat(vm.state.value.isTestPassed).isFalse()
    }

    @Test
    fun `submitAnswer mit unbekannter Frage-ID wird ignoriert`() {
        val vm = viewModel()

        vm.submitAnswer("does-not-exist", 0)

        assertThat(vm.state.value.currentScore).isEqualTo(0)
    }

    // ── startQuiz / Reset ────────────────────────────────────────────────

    @Test
    fun `startQuiz - resettet Score und bestanden-Status`() {
        val vm = viewModel()
        vm.submitAnswer("q1", 0)
        vm.submitAnswer("q2", 3)

        vm.startQuiz()

        val s = vm.state.value
        assertThat(s.showQuiz).isTrue()
        assertThat(s.currentScore).isEqualTo(0)
        assertThat(s.isTestPassed).isFalse()
    }

    // ── markCompleted / Abschluss-Guard ──────────────────────────────────

    @Test
    fun `markCompleted ohne bestandenen Test wird IGNORIERT (Guard)`() {
        val vm = viewModel()

        vm.markCompleted() // kein einziges Quiz beantwortet

        assertThat(vm.state.value.completed).isNull()
    }

    @Test
    fun `markCompleted nach bestandenem Test setzt Abschluss`() {
        val vm = viewModel()
        vm.submitAnswer("q1", 0)
        vm.submitAnswer("q2", 3)
        vm.submitAnswer("q3", 1) // 100 % → bestanden

        vm.markCompleted()

        val completed = vm.state.value.completed
        assertThat(completed).isNotNull()
        assertThat(completed!!.scorePct).isEqualTo(100)
        assertThat(completed.xpGained).isEqualTo(GamificationRules.xpPerCompletedLesson)
        assertThat(completed.nextSlug).isEqualTo("lesson-2")
    }

    @Test
    fun `markCompleted nach Teilerfolg (25 Prozent) wird ignoriert`() {
        val vm = viewModel()
        vm.submitAnswer("q1", 0)
        vm.submitAnswer("q2", 0)
        vm.submitAnswer("q3", 0) // 25 % → nicht bestanden

        vm.markCompleted()

        assertThat(vm.state.value.completed).isNull()
    }

    @Test
    fun `canCompleteLesson spiegelt isTestPassed`() {
        val vm = viewModel()
        assertThat(vm.canCompleteLesson()).isFalse()

        vm.submitAnswer("q1", 0)
        vm.submitAnswer("q2", 3)
        vm.submitAnswer("q3", 1)

        assertThat(vm.canCompleteLesson()).isTrue()
    }
}
