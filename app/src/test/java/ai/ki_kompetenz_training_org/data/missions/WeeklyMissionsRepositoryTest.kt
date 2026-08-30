package ai.ki_kompetenz_training_org.data.missions

import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import android.content.SharedPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class WeeklyMissionsRepositoryTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var gamification: GamificationRepository
    private lateinit var repository: WeeklyMissionsRepository
    private val today = LocalDate.of(2026, 8, 30) // ISO week 2026-W35

    private val progressWrites = mutableListOf<Pair<String, Int>>()
    private val boolWrites = mutableListOf<Pair<String, Boolean>>()
    private val xpAwards = mutableListOf<Int>()

    @Before
    fun setup() {
        prefs = mockk()
        editor = mockk()
        gamification = mockk()
        every { prefs.edit() } returns editor
        every { prefs.getString("missions_week", null) } returns "2026-W35"
        // Dynamic SharedPreferences semantics: reads reflect accumulated writes.
        every { prefs.getInt(any(), any()) } answers {
            val key = it.invocation.args[0] as String
            progressWrites.lastOrNull { w -> w.first == key }?.second ?: (it.invocation.args[1] as Int)
        }
        every { prefs.getBoolean(any(), any()) } answers {
            val key = it.invocation.args[0] as String
            boolWrites.lastOrNull { b -> b.first == key }?.second ?: (it.invocation.args[1] as Boolean)
        }
        every { editor.putInt(any(), any()) } answers {
            progressWrites += (it.invocation.args[0] as String) to (it.invocation.args[1] as Int); editor
        }
        every { editor.putBoolean(any(), any()) } answers {
            boolWrites += (it.invocation.args[0] as String) to (it.invocation.args[1] as Boolean); editor
        }
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        progressWrites.clear()
        boolWrites.clear()
        xpAwards.clear()
        val xpSlot = slot<Int>()
        coEvery { gamification.addXp(capture(xpSlot)) } coAnswers { xpAwards += xpSlot.captured }
        repository = WeeklyMissionsRepository(prefs, gamification)
    }

    @Test
    fun `week rollover resets progress and selection`() {
        every { prefs.getString("missions_week", null) } returns "2026-W34"
        val state = repository.current(today)
        assertEquals("2026-W35", state.week)
        assertTrue(state.progress.isEmpty())
        assertTrue(state.completed.isEmpty())
    }

    @Test
    fun `current builds progress for the selected week`() = runBlocking {
        val selected = repository.selectedFor("2026-W35")
        val m = selected.first()
        repository.record(m.metric, amount = 1, today = today)
        val state = repository.current(today)
        assertEquals(1, state.progress[m.id])
    }

    @Test
    fun `record advances only missions of the matching metric`() = runBlocking {
        val selected = repository.selectedFor("2026-W35")
        repository.record(MissionMetric.QUIZ_PLAYED, amount = 1, today = today)
        selected.forEach { template ->
            val written = progressWrites.firstOrNull { it.first == "mission_progress_${template.id}" }
            if (template.metric == MissionMetric.QUIZ_PLAYED) {
                assertEquals(1, written?.second)
            } else {
                if (written != null) assertEquals(0, written.second.coerceAtMost(0)) // nothing advanced
                assertTrue(written == null || written.second == 0)
            }
        }
    }

    @Test
    fun `completing a mission awards reward XP once`() = runBlocking {
        val mission = repository.selectedFor("2026-W35").first()
        repeat(mission.target) { repository.record(mission.metric, today = today) }
        assertEquals(1, xpAwards.count { it == MissionsEngine.rewardXp })
        // Further records after completion do not re-award.
        repository.record(mission.metric, today = today)
        assertEquals(1, xpAwards.count { it == MissionsEngine.rewardXp })
        assertTrue(boolWrites.any { it.first == "mission_completed_${mission.id}" && it.second })
    }

    @Test
    fun `completing all three selected missions awards weekly bonus once`() = runBlocking {
        // Drive each selected mission to completion via its own metric.
        val selected = repository.selectedFor("2026-W35")
        for (template in selected) {
            repeat(template.target) { repository.record(template.metric, today = today) }
        }
        assertEquals(3, xpAwards.count { it == MissionsEngine.rewardXp })
        assertEquals(1, xpAwards.count { it == MissionsEngine.allBonusXp })
        assertTrue(boolWrites.any { it.first == "missions_bonus" && it.second })
        // Bonus is not double-awarded.
        repository.record(MissionMetric.QUIZ_PLAYED, today = today)
        assertEquals(1, xpAwards.count { it == MissionsEngine.allBonusXp })
    }

    @Test
    fun `record failure does not throw`() = runBlocking {
        every { prefs.edit() } throws RuntimeException("prefs broken")
        repository.record(MissionMetric.QUIZ_PLAYED, today = today) // must not throw
        assertTrue(true)
    }

    @Test
    fun `state reports completion correctly`() = runBlocking {
        val mission = repository.selectedFor("2026-W35").last()
        repeat(mission.target) { repository.record(mission.metric, today = today) }
        val state = repository.current(today)
        assertTrue(state.completed.contains(mission.id))
        assertFalse(state.completed.isEmpty())
    }
}
