/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity
import ai.ki_kompetenz_training_org.data.db.GamificationEntity
import ai.ki_kompetenz_training_org.data.db.LessonProgressEntity
import ai.ki_kompetenz_training_org.data.missions.MissionMetric
import ai.ki_kompetenz_training_org.data.missions.MissionTemplate
import ai.ki_kompetenz_training_org.data.missions.WeeklyMissionsRepository
import ai.ki_kompetenz_training_org.data.missions.WeeklyMissionsState
import ai.ki_kompetenz_training_org.data.repo.Badge
import ai.ki_kompetenz_training_org.data.repo.CompetencyRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
 * Unit tests für [GamificationViewModel]: prüft das reine State-Mapping vom
 * (gemockten) [GamificationRepository] in den [GamificationUiState].
 *
 * Kein Turbine, kein Robolectric — der StateFlow wird nach advanceUntilIdle()
 * direkt über .value gelesen. Die drei Repository-Flows werden über flowOf(...)
 * vorab bereitgestellt; freezes() und missions sind einfache Properties/Methoden.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GamificationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var gamification: GamificationRepository
    private lateinit var missionsRepo: WeeklyMissionsRepository
    private lateinit var competency: CompetencyRepository

    private fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private fun yesterday(): String =
        LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        gamification = mockk()
        missionsRepo = mockk()
        competency = mockk()
        every { competency.observeLatest() } returns flowOf(null)

        // Grund-Stubbing: alle im init-Block kombinierten Flows + Leszugriffe.
        every { gamification.observe() } returns flowOf(GamificationEntity())
        every { gamification.observeBadgeState() } returns flowOf(emptyList())
        every { gamification.observeLessonProgress() } returns flowOf(emptyList())
        every { gamification.freezes() } returns 0
        every { gamification.missions } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVm(): GamificationViewModel = GamificationViewModel(gamification, competency)

    private fun lessonProgress(vararg slugs: String): List<LessonProgressEntity> =
        slugs.map { LessonProgressEntity(slug = it) }

    // ── (1) State-Mapping: XP → Level-Kennzahlen ─────────────────────────

    @Test
    fun `XP 250 mappt auf Level 2 mit korrekten Rest- und Zielwerten`() = runTest(dispatcher) {
        every { gamification.observe() } returns flowOf(GamificationEntity(xp = 250))

        val vm = createVm()
        advanceUntilIdle()

        val state = vm.state.value
        // 250 XP: L2 beginnt bei 100 (50·2·1), L3 bei 300 (50·3·2).
        assertThat(state.xp).isEqualTo(250)
        assertThat(state.level).isEqualTo(2)
        assertThat(state.xpIntoLevel).isEqualTo(150) // 250 − 100
        assertThat(state.xpNeeded).isEqualTo(200)    // 300 − 100
        // Konsistenz mit den Regeln als Invariante (xpIntoLevel < xpNeeded):
        assertThat(state.xpIntoLevel).isLessThan(state.xpNeeded)
    }

    @Test
    fun `Level-Grenze - exakt 100 XP ergibt Level 2 ohne Rest-XP`() = runTest(dispatcher) {
        every { gamification.observe() } returns flowOf(GamificationEntity(xp = 100))

        val vm = createVm()
        advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.level).isEqualTo(2)
        assertThat(state.xpIntoLevel).isEqualTo(0)
        assertThat(state.xpNeeded).isEqualTo(200)
    }

    // ── (2) streak + lessonProgress im UiState ───────────────────────────

    @Test
    fun `streak und abgeschlossene Lektionen erscheinen im UiState`() = runTest(dispatcher) {
        every { gamification.observe() } returns flowOf(GamificationEntity(xp = 120, streak = 4))
        every { gamification.observeLessonProgress() } returns
            flowOf(lessonProgress("lesson-1", "lesson-2", "lesson-5"))

        val vm = createVm()
        advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.streak).isEqualTo(4)
        assertThat(state.lessonProgress).isEqualTo(3)
    }

    // ── (3) checkedInToday ────────────────────────────────────────────────

    @Test
    fun `checkedInToday ist true wenn lastCheckInDay heute ist`() = runTest(dispatcher) {
        every { gamification.observe() } returns
            flowOf(GamificationEntity(streak = 3, lastCheckInDay = today()))

        val vm = createVm()
        advanceUntilIdle()

        assertThat(vm.state.value.checkedInToday).isTrue()
    }

    @Test
    fun `checkedInToday ist false wenn lastCheckInDay gestern ist`() = runTest(dispatcher) {
        every { gamification.observe() } returns
            flowOf(GamificationEntity(streak = 3, lastCheckInDay = yesterday()))

        val vm = createVm()
        advanceUntilIdle()

        assertThat(vm.state.value.checkedInToday).isFalse()
    }

    // ── (4) freezes ───────────────────────────────────────────────────────

    @Test
    fun `freezes aus dem Repository erscheinen im State`() = runTest(dispatcher) {
        every { gamification.observe() } returns flowOf(GamificationEntity(xp = 40))
        every { gamification.freezes() } returns 1

        val vm = createVm()
        advanceUntilIdle()

        assertThat(vm.state.value.freezes).isEqualTo(1)
    }

    // ── (5) entity == null → Defaults ─────────────────────────────────────

    @Test
    fun `fehlende Entity liefert Defaults ohne Exception`() = runTest(dispatcher) {
        every { gamification.observe() } returns flowOf(null)

        val vm = createVm()
        advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.xp).isEqualTo(0)
        assertThat(state.level).isEqualTo(1)
        assertThat(state.xpIntoLevel).isEqualTo(0)
        assertThat(state.xpNeeded).isEqualTo(100) // L2 − L1 = 100 − 0
        assertThat(state.streak).isEqualTo(0)
        assertThat(state.checkedInToday).isFalse()
        assertThat(state.lessonProgress).isEqualTo(0)
    }

    // ── (6) missions-Mapping ──────────────────────────────────────────────

    @Test
    fun `Missions-Tasks werden mit Progress und Completed-Flag gemappt`() = runTest(dispatcher) {
        val week = "2026-W35"
        every { gamification.missions } returns missionsRepo
        every { missionsRepo.current() } returns WeeklyMissionsState(
            week = week,
            progress = mapOf("quiz_play" to 2, "srs_cards" to 20),
            completed = setOf("srs_cards"),
            bonusAwarded = false,
        )
        every { missionsRepo.selectedFor(week) } returns listOf(
            MissionTemplate("quiz_play", MissionMetric.QUIZ_PLAYED, 3),
            MissionTemplate("srs_cards", MissionMetric.SRS_CARDS, 20),
            MissionTemplate("daily", MissionMetric.DAILY_COMPLETED, 3),
        )

        val vm = createVm()
        advanceUntilIdle()

        val missions = vm.state.value.missions
        assertThat(missions).hasSize(3)

        val byId = missions.associateBy { it.id }
        // Fortschritt aus state.progress:
        assertThat(byId.getValue("quiz_play").target).isEqualTo(3)
        assertThat(byId.getValue("quiz_play").progress).isEqualTo(2)
        assertThat(byId.getValue("quiz_play").completed).isFalse()
        // Abgeschlossen: progress == target und completed-Flag gesetzt:
        assertThat(byId.getValue("srs_cards").progress).isEqualTo(20)
        assertThat(byId.getValue("srs_cards").completed).isTrue()
        // Fehlender Progress-Eintrag → 0:
        assertThat(byId.getValue("daily").progress).isEqualTo(0)
        assertThat(byId.getValue("daily").completed).isFalse()
    }

    // ── (7) missions == null ──────────────────────────────────────────────

    @Test
    fun `missions null liefert eine leere Missionsliste im State`() = runTest(dispatcher) {
        every { gamification.missions } returns null

        val vm = createVm()
        advanceUntilIdle()

        assertThat(vm.state.value.missions).isEmpty()
    }

    // ── (8) Badges durchreichen ───────────────────────────────────────────

    @Test
    fun `badges werden unverändert durchgereicht`() = runTest(dispatcher) {
        val badges = listOf(
            Badge("first_score", "🎯", "Erster Schritt", "Erziele deinen ersten KI-Score") to true,
            Badge("streak_7", "⚡", "Wochen-Serie", "7 Tage in Folge aktiv") to false,
        )
        every { gamification.observeBadgeState() } returns flowOf(badges)

        val vm = createVm()
        advanceUntilIdle()

        assertThat(vm.state.value.badges).isEqualTo(badges)
        assertThat(vm.state.value.badges).hasSize(2)
        assertThat(vm.state.value.badges.first().second).isTrue()
    }

    // ── (9) KIKI-Snapshot (Radar) ────────────────────────────────────────

    @Test
    fun `neuester KIKI-Snapshot erscheint im UiState`() = runTest(dispatcher) {
        val snapshot = CompetencySnapshotEntity(
            weekKey = "2026-W36",
            kiki = 61,
            perDomainJson = "[76, 12, 40, 0, 0, 0, 0, 0, 0]",
            createdAt = 1234L,
        )
        every { competency.observeLatest() } returns flowOf(snapshot)

        val vm = createVm()
        advanceUntilIdle()

        val latest = vm.state.value.latestSnapshot
        assertThat(latest).isNotNull()
        assertThat(latest!!.kiki).isEqualTo(61)
    }

    @Test
    fun `ohne Competency-Daten bleibt latestSnapshot null`() = runTest(dispatcher) {
        val vm = createVm()
        advanceUntilIdle()

        assertThat(vm.state.value.latestSnapshot).isNull()
    }
}
