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
import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit-Tests für die restlichen Gamification-Belohnungen:
 * [GamificationRepository.onSrsReview] (Karten-XP + Session-Bonus),
 * [GamificationRepository.onTeamJoined] und
 * [GamificationRepository.badgeIds] (Resilienz gegen korrupte
 * SharedPreferences-Daten).
 *
 * Ergänzt GamificationUnlockTest (Quiz/Mini-Game-Verträge) und
 * GamificationFreezeTest (Streaks).
 */
class GamificationRewardsTest {

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
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        upserted.clear()
        val entitySlot = slot<GamificationEntity>()
        coEvery { dao.upsert(capture(entitySlot)) } coAnswers { upserted += entitySlot.captured }
        coEvery { dao.get() } coAnswers { upserted.lastOrNull() }

        missions = mockk(relaxed = true)
        repository = GamificationRepository(db, context)
        repository.missions = missions
    }

    // ── onSrsReview ──────────────────────────────────────────────────────

    @Test
    fun `SRS-Karte gibt 5 XP, SRS_CARDS-Mission wird protokolliert`() = runTest {
        repository.onSrsReview(sessionFinished = false, sessionSize = 1)

        assertThat(upserted.last().xp).isEqualTo(5) // xpPerSrsReview
        coVerify(exactly = 1) { missions.record(MissionMetric.SRS_CARDS) }
    }

    @Test
    fun `Session-Bonus (20 XP) nur bei beendeter Session mit mindestens 5 Karten`() = runTest {
        // beendet, aber zu kurz
        repository.onSrsReview(sessionFinished = true, sessionSize = 4)
        assertThat(upserted.last().xp).isEqualTo(5)

        // nicht beendet, groß genug
        repository.onSrsReview(sessionFinished = false, sessionSize = 10)
        assertThat(upserted.last().xp).isEqualTo(10) // 2 Reviews x 5, kein Bonus

        // beendet UND mindestens 5 → Bonus (XP akkumuliert: 3x5 + 20)
        repository.onSrsReview(sessionFinished = true, sessionSize = 5)
        assertThat(upserted.last().xp).isEqualTo(35)
    }

    @Test
    fun `Session-Bonus exakt an der Grenze - 5 Karten reicht, 4 nicht`() = runTest {
        val xpBefore4 = 0
        repository.onSrsReview(sessionFinished = true, sessionSize = 4)
        val xpAfter4 = upserted.last().xp
        assertThat(xpAfter4 - xpBefore4).isEqualTo(GamificationRules.xpPerSrsReview)

        repository.onSrsReview(sessionFinished = true, sessionSize = 5)
        val xpAfter5 = upserted.last().xp
        assertThat(xpAfter5 - xpAfter4).isEqualTo(
            GamificationRules.xpPerSrsReview + GamificationRules.srsSessionBonus
        )
    }

    // ── onTeamJoined ─────────────────────────────────────────────────────

    @Test
    fun `Team beitreten gibt 20 XP und team_player-Badge`() = runTest {
        repository.onTeamJoined()

        assertThat(upserted.last().xp).isEqualTo(20) // xpPerTeamJoin
        val ids = upserted.last().badgesJson
        assertThat(ids).contains("team_player")
    }

    // ── badgeIds: Resilienz (Reward-Flow liest SharedPreferences) ────────

    @Test
    fun `badgeIds - null und leer liefern leere Liste (frische Installation)`() {
        assertThat(repository.badgeIds(null)).isEmpty()
        assertThat(repository.badgeIds("")).isEmpty()
        assertThat(repository.badgeIds("   ")).isEmpty()
    }

    @Test
    fun `badgeIds - korruptes JSON liefert leere Liste statt Crash`() {
        assertThat(repository.badgeIds("not-json{")).isEmpty()
        assertThat(repository.badgeIds("[1,2,3]")).isEmpty() // falscher Elementtyp
    }

    @Test
    fun `badgeIds - gueltiges JSON wird geparst (Roundtrip mit unlock)`() = runTest {
        repository.onTeamJoined()
        val json = upserted.last().badgesJson

        assertThat(repository.badgeIds(json)).containsExactly("team_player")
    }

    // ── addXp-Guard ──────────────────────────────────────────────────────

    @Test
    fun `addXp ignoriert 0 und negative Punkte (kein DB-Write, kein Crash)`() = runTest {
        repository.addXp(0)
        repository.addXp(-5)

        coVerify(exactly = 0) { dao.upsert(any()) }
        assertThat(upserted).isEmpty()
    }
}
