package ai.ki_kompetenz_training_org.data.daily

import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DailyChallengeRepositoryTest {

    private lateinit var repository: DailyChallengeRepository
    private lateinit var mockPrefsEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private val today = LocalDate.of(2026, 8, 25)

    @Before
    fun setup() {
        mockPrefs = mockk(relaxed = true)
        mockPrefsEditor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockPrefsEditor
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns null
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 0
        every { mockPrefsEditor.putString(any(), any()) } returns mockPrefsEditor
        every { mockPrefsEditor.putInt(any(), any()) } returns mockPrefsEditor
        repository = DailyChallengeRepository(mockPrefs)
    }

    @Test
    fun getTodayChallenge_returns_a_MiniGame() {
        val challenge = repository.getTodayChallenge(today, MiniGames.ALL)
        assertTrue(challenge != null)
    }

    @Test
    fun getTodayChallenge_returns_same_game_for_same_date() {
        val c1 = repository.getTodayChallenge(today, MiniGames.ALL)
        val c2 = repository.getTodayChallenge(today, MiniGames.ALL)
        assertEquals(c1, c2)
    }

    @Test
    fun getTodayChallenge_returns_null_for_empty_games() {
        assertEquals(null, repository.getTodayChallenge(today, emptyList()))
    }

    @Test
    fun isCompletedToday_returns_false_when_never_completed() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns null
        assertFalse(repository.isCompletedToday(today))
    }

    @Test
    fun isCompletedToday_returns_true_when_completed_today() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns "2026-08-25"
        assertTrue(repository.isCompletedToday(today))
    }

    @Test
    fun isCompletedToday_returns_false_when_completed_yesterday() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns "2026-08-24"
        assertFalse(repository.isCompletedToday(today))
    }

    @Test
    fun getStreak_returns_0_when_never_completed() {
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 0
        assertEquals(0, repository.getStreak())
    }

    @Test
    fun getStreak_returns_persisted_value() {
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 5
        assertEquals(5, repository.getStreak())
    }

    @Test
    fun completeChallenge_returns_XP_for_first_day() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns null
        val xp = repository.completeChallenge(today, perfect = false)
        assertEquals(20, xp)
    }

    @Test
    fun completeChallenge_first_day_perfect() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns null
        val xp = repository.completeChallenge(today, perfect = true)
        assertEquals(35, xp)
    }

    @Test
    fun completeChallenge_persists_date() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns null
        repository.completeChallenge(today, perfect = false)
        val dateSlot = slot<String>()
        verify { mockPrefsEditor.putString("daily_challenge_last_date", capture(dateSlot)) }
        assertEquals("2026-08-25", dateSlot.captured)
    }

    @Test
    fun completeChallenge_persists_streak_1() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns null
        repository.completeChallenge(today, perfect = false)
        val streakSlot = slot<Int>()
        verify { mockPrefsEditor.putInt("daily_challenge_streak", capture(streakSlot)) }
        assertEquals(1, streakSlot.captured)
    }

    @Test
    fun completeChallenge_increments_streak_consecutive() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns "2026-08-24"
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 1
        val xp = repository.completeChallenge(today, perfect = false)
        assertEquals(25, xp)
        val streakSlot = slot<Int>()
        verify { mockPrefsEditor.putInt("daily_challenge_streak", capture(streakSlot)) }
        assertEquals(2, streakSlot.captured)
    }

    @Test
    fun completeChallenge_resets_streak_after_gap() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns "2026-08-22"
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 5
        val xp = repository.completeChallenge(today, perfect = false)
        assertEquals(20, xp)
        val streakSlot = slot<Int>()
        verify { mockPrefsEditor.putInt("daily_challenge_streak", capture(streakSlot)) }
        assertEquals(1, streakSlot.captured)
    }

    @Test
    fun completeChallenge_returns_0_when_already_completed() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns "2026-08-25"
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 3
        assertEquals(0, repository.completeChallenge(today, perfect = true))
    }

    @Test
    fun completeChallenge_no_persist_when_already_done() {
        every { mockPrefs.getString("daily_challenge_last_date", null) } returns "2026-08-25"
        repository.completeChallenge(today, perfect = false)
        verify(exactly = 0) { mockPrefsEditor.putString(any(), any()) }
    }

    @Test
    fun calculateXpPreview_uses_current_streak() {
        every { mockPrefs.getInt("daily_challenge_streak", 0) } returns 3
        assertEquals(30, repository.calculateXpPreview(perfect = false))
        assertEquals(45, repository.calculateXpPreview(perfect = true))
    }
}
