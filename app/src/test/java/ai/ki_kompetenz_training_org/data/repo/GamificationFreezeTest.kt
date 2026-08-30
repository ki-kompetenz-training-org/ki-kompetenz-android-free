package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.GamificationDao
import ai.ki_kompetenz_training_org.data.db.GamificationEntity
import android.content.Context
import android.content.SharedPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GamificationFreezeTest {

    private lateinit var dao: GamificationDao
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
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
        // Default: current week already granted → no weekly grant in most tests.
        every { prefs.getString("last_freeze_week", null) } returns GamificationRules.isoWeekKey(LocalDate.now())
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        upserted.clear()
        // Realistic single-row semantics: get() returns the latest upsert (null when empty).
        val entitySlot = slot<GamificationEntity>()
        coEvery { dao.upsert(capture(entitySlot)) } coAnswers { upserted += entitySlot.captured }
        coEvery { dao.get() } coAnswers { upserted.lastOrNull() }
        repository = GamificationRepository(db, context)
    }

    @Test
    fun `first check-in starts streak without consuming freeze`() = runBlocking {
        every { prefs.getInt("freezes", 0) } returns 1
        val result = repository.dailyCheckIn()
        assertEquals(1, result)
        // freeze kept (no consumption: putInt would have been called otherwise)
        coVerify(exactly = 0) { editor.putInt("freezes", 0) }
        assertEquals(1, upserted.last().streak)
    }

    @Test
    fun `weekly grant adds a freeze on first check-in of the week`() = runBlocking {
        every { prefs.getString("last_freeze_week", null) } returns "2026-W34"
        every { prefs.getInt("freezes", 0) } returns 0
        // fresh row: lastCheckInDay null → streak 1
        repository.dailyCheckIn()
        verifyGrant(1)
        verify { editor.putString("last_freeze_week", any()) }
    }

    @Test
    fun `one missed day with freeze continues the streak and consumes it`() = runBlocking {
        seed(GamificationEntity(streak = 5, lastCheckInDay = twoDaysAgo()))
        every { prefs.getInt("freezes", 0) } returns 1
        val result = repository.dailyCheckIn()
        assertEquals(6, result)
        verify { editor.putInt("freezes", 0) }
        assertEquals(6, upserted.last().streak)
    }

    @Test
    fun `one missed day without freeze resets the streak`() = runBlocking {
        seed(GamificationEntity(streak = 5, lastCheckInDay = twoDaysAgo()))
        every { prefs.getInt("freezes", 0) } returns 0
        val result = repository.dailyCheckIn()
        assertEquals(1, result)
        coVerify(exactly = 0) { editor.putInt("freezes", any()) } // no freeze change
        assertEquals(1, upserted.last().streak)
    }

    @Test
    fun `two missed days reset even with freeze`() = runBlocking {
        seed(GamificationEntity(streak = 5, lastCheckInDay = fourDaysAgo()))
        every { prefs.getInt("freezes", 0) } returns 2
        val result = repository.dailyCheckIn()
        assertEquals(1, result)
        coVerify(exactly = 0) { editor.putInt("freezes", 1) } // no consumption at cap
        assertEquals(1, upserted.last().streak)
    }

    @Test
    fun `purchase freeze deducts XP and increases balance`() = runBlocking {
        seed(GamificationEntity(xp = 150))
        every { prefs.getInt("freezes", 0) } returns 0
        assertTrue(repository.purchaseFreeze())
        verify { editor.putInt("freezes", 1) }
        assertEquals(50, upserted.last().xp)
    }

    @Test
    fun `purchase freeze rejected at cap`() = runBlocking {
        seed(GamificationEntity(xp = 500))
        every { prefs.getInt("freezes", 0) } returns 2
        assertFalse(repository.purchaseFreeze())
        coVerify(exactly = 0) { editor.putInt(any(), any()) }
    }

    @Test
    fun `purchase freeze rejected without XP`() = runBlocking {
        seed(GamificationEntity(xp = 50))
        every { prefs.getInt("freezes", 0) } returns 0
        assertFalse(repository.purchaseFreeze())
        coVerify(exactly = 0) { editor.putInt(any(), any()) }
    }

    private fun seed(entity: GamificationEntity) {
        upserted += entity
    }

    private fun twoDaysAgo(): String =
        LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun fourDaysAgo(): String =
        LocalDate.now().minusDays(4).format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun verifyGrant(expectedFreezes: Int) {
        io.mockk.verify { editor.putInt("freezes", expectedFreezes) }
    }
}
