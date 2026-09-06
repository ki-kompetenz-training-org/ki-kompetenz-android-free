/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.GamificationDao
import ai.ki_kompetenz_training_org.data.db.GamificationEntity
import ai.ki_kompetenz_training_org.data.missions.MissionMetric
import ai.ki_kompetenz_training_org.data.missions.WeeklyMissionsRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.coAnswers
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit-Tests für die Badge-/Missions-Unlock-Verträge von
 * [GamificationRepository.onQuizFinished] und
 * [GamificationRepository.onMiniGameFinished].
 *
 * Der Badge-KATALOG ist in [BadgesTest] abgedeckt — hier geht es um die
 * Regel, WANN ein Badge vergeben wird, inklusive der fiesen Grenzen
 * ("visionary" braucht score >= 81, NICHT >= 80; QUIZ_GOOD erst ab 80 %
 * richtigen Antworten).
 *
 * Setup folgt dem Muster aus GamificationFreezeTest: echte Repository mit
 * gemocktem Room-DAO und SharedPreferences.
 */
class GamificationUnlockTest {

    private lateinit var dao: GamificationDao
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var missions: WeeklyMissionsRepository
    private lateinit var repository: GamificationRepository
    private val upserted = mutableListOf<GamificationEntity>()

    @Before
    fun setup() {
        dao = mockk()
        val db = mockk<AppDatabase> { every { gamificationDao() } returns dao }
        prefs = mockk()
        editor = mockk()
        val context = mockk<Context>()
        every { context.getSharedPreferences("kikompetenz_gamification", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { prefs.getInt("freezes", 0) } returns 0
        every { prefs.getStringSet("played_games", any()) } returns mutableSetOf()
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putStringSet(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        upserted.clear()
        val entitySlot = slot<GamificationEntity>()
        coEvery { dao.upsert(capture(entitySlot)) } coAnswers { upserted += entitySlot.captured }
        coEvery { dao.get() } coAnswers { upserted.lastOrNull() }

        missions = mockk(relaxed = true)
        repository = GamificationRepository(db, context)
        repository.missions = missions
    }

    private fun badgeIds(): Set<String> =
        upserted.lastOrNull()?.badgesJson
            ?.removeSurrounding("[", "]")
            ?.split(",") // kotlinx JSON encodes ohne Leerzeichen
            ?.map { it.trim().trim('"') }
            ?.toSet()
            ?: emptySet()

    // ── onQuizFinished ───────────────────────────────────────────────────

    @Test
    fun `onQuizFinished - XP entspricht quizXp (10 von 10 = 150: 10x10 + 50 Bonus)`() = runTest {
        repository.onQuizFinished(correctCount = 10, totalQuestions = 10, score = 90)

        assertThat(upserted.last().xp).isEqualTo(150)
    }

    @Test
    fun `first_score wird beim ersten Quiz vergeben`() = runTest {
        repository.onQuizFinished(correctCount = 5, totalQuestions = 10, score = 40)

        assertThat("first_score" in badgeIds()).isTrue()
    }

    @Test
    fun `perfect_score nur bei vollem Treffer (9 von 10 reicht nicht)`() = runTest {
        repository.onQuizFinished(correctCount = 9, totalQuestions = 10, score = 60)
        assertThat("perfect_score" in badgeIds()).isFalse()

        repository.onQuizFinished(correctCount = 10, totalQuestions = 10, score = 90)
        assertThat("perfect_score" in badgeIds()).isTrue()
    }

    @Test
    fun `visionary - Grenze ist 81, NICHT 80 (Combo-Score ist kein Prozentwert)`() = runTest {
        repository.onQuizFinished(correctCount = 8, totalQuestions = 10, score = 80)
        assertThat("visionary" in badgeIds()).isFalse()

        repository.onQuizFinished(correctCount = 8, totalQuestions = 10, score = 81)
        assertThat("visionary" in badgeIds()).isTrue()
    }

    @Test
    fun `QUIZ_PLAYED-Mission wird immer protokolliert`() = runTest {
        repository.onQuizFinished(correctCount = 0, totalQuestions = 10, score = 0)

        coVerify(exactly = 1) { missions.record(MissionMetric.QUIZ_PLAYED) }
    }

    @Test
    fun `QUIZ_GOOD-Mission erst ab 80 Prozent richtigen Antworten`() = runTest {
        repository.onQuizFinished(correctCount = 7, totalQuestions = 10, score = 50)
        coVerify(exactly = 0) { missions.record(MissionMetric.QUIZ_GOOD) }

        repository.onQuizFinished(correctCount = 8, totalQuestions = 10, score = 55)
        coVerify(exactly = 1) { missions.record(MissionMetric.QUIZ_GOOD) }
    }

    // ── onMiniGameFinished ───────────────────────────────────────────────

    @Test
    fun `onMiniGameFinished - XP = quizXp und mini_game-Badge`() = runTest {
        repository.onMiniGameFinished(correctCount = 8, totalQuestions = 10, gameId = "orb_hunt")

        assertThat(upserted.last().xp).isEqualTo(GamificationRules.quizXp(8, 10))
        assertThat("mini_game" in badgeIds()).isTrue()
    }

    @Test
    fun `gespielte Games werden lokal persistiert (DSGVO: nur auf dem Geraet)`() = runTest {
        repository.onMiniGameFinished(correctCount = 5, totalQuestions = 10, gameId = "orb_hunt")

        verify {
            editor.putStringSet("played_games", match<Set<String>> { "orb_hunt" in it })
        }
    }

    @Test
    fun `mini_game_all - Badge erst wenn ALLE Mini-Games gespielt wurden`() = runTest {
        val allIds = MiniGames.ALL.map { it.id }
        val allButLast = allIds.dropLast(1).toMutableSet()
        every { prefs.getStringSet("played_games", any()) } returns allButLast

        // vorletztes Spiel: Katalog noch nicht voll
        repository.onMiniGameFinished(correctCount = 5, totalQuestions = 10, gameId = allIds.last())
        assertThat("mini_game_all" in badgeIds()).isFalse()

        // Katalog voll: alle Spiele inklusive dem letzten sind gespielt
        every { prefs.getStringSet("played_games", any()) } returns allIds.toMutableSet()
        repository.onMiniGameFinished(correctCount = 5, totalQuestions = 10, gameId = allIds.first())
        assertThat("mini_game_all" in badgeIds()).isTrue()
    }

    @Test
    fun `fake_or_real - Badge nur bei 10 von 10 im fake_or_real-Game`() = runTest {
        repository.onMiniGameFinished(correctCount = 9, totalQuestions = 10, gameId = "fake_or_real")
        assertThat("fake_or_real" in badgeIds()).isFalse()

        repository.onMiniGameFinished(correctCount = 10, totalQuestions = 10, gameId = "fake_or_real")
        assertThat("fake_or_real" in badgeIds()).isTrue()
    }

    @Test
    fun `perfect_score gilt auch im Mini-Game (gleiche Regel, zweiter Trigger)`() = runTest {
        repository.onMiniGameFinished(correctCount = 10, totalQuestions = 10, gameId = "orb_hunt")

        assertThat("perfect_score" in badgeIds()).isTrue()
    }

    @Test
    fun `MINIGAME_PLAYED-Mission wird protokolliert`() = runTest {
        repository.onMiniGameFinished(correctCount = 5, totalQuestions = 10, gameId = "orb_hunt")

        coVerify(exactly = 1) { missions.record(MissionMetric.MINIGAME_PLAYED) }
    }
}
