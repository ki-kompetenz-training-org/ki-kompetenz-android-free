package ai.ki_kompetenz_training_org.ui.minigames3d

import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyLog
import ai.ki_kompetenz_training_org.data.minigames3d.GameEngine
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.GameRules
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyStatement
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MiniGame3DViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vmWith(mastery: MasteryTracker, gamification: GamificationRepository): MiniGame3DViewModel =
        MiniGame3DViewModel(GameMode.ORB_HUNT, gamification, mastery)

    @Test
    fun initial_phase_is_countdown() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        assertNull(vm.game)
    }

    @Test
    fun start_creates_engine_state_and_phase_playing() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        vm.start()
        assertNotNull(vm.game)
        assertEquals(ArenaPhase.PLAYING, vm.state.value.phase)
    }

    @Test
    fun step_updates_hud_after_throttle() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        vm.start()
        vm.step(ai.ki_kompetenz_training_org.data.minigames3d.InputState(false, false, false, false, false, null), 0.12)
        assertTrue(vm.state.value.hud.timeLeft <= 60)
    }

    @Test
    fun computer_xp_win_withful_mastery_gets_win_bonus() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        s.score = 250
        s.won = true
        s.classifyStreak = 3
        // 10 correct / 10 total -> masteryShare 1.0
        val xp = vm.computeXp(s, 10, 10)
        // base = 15 * 2 * 1.0 = 30, streak 3*2=6, win 15 -> 51
        assertEquals(51, xp)
    }

    @Test
    fun compute_xp_low_mastery_stays_above_floor() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        val xp = vm.computeXp(s, 0, 10)
        assertTrue(xp >= 10)
    }

    @Test
    fun compute_xp_scales_with_mastery_share() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        val low = vm.computeXp(s, 2, 10)
        val high = vm.computeXp(s, 8, 10)
        assertTrue(high > low)
    }

    @Test
    fun handleEnd_records_classifications_into_mastery_and_awards_xp() {
        val gamification = mockk<GamificationRepository>(relaxed = true)
        val tracker = MasteryTracker(InMemoryPrefsMastery())
        val vm = vmWith(tracker, gamification)
        vm.start()
        val s = vm.game!!
        s.ended = true
        s.won = true
        s.classifications.add(ClassifyLog("Grundlagen der KI", true,
            LiteracyStatement("a", "a", "Grundlagen der KI", false)))
        s.classifications.add(ClassifyLog("Datenschutz & DSGVO", false,
            LiteracyStatement("b", "b", "Datenschutz & DSGVO", true)))
        vm.step(ai.ki_kompetenz_training_org.data.minigames3d.InputState(false, false, false, false, false, null), 0.016)
        assertEquals(ArenaPhase.RESULT, vm.state.value.phase)
        assertNotNull(vm.state.value.result)
        val result = vm.state.value.result!!
        assertEquals(1, result.correct)
        assertEquals(2, result.total)
        assertTrue(result.earnedXp > 0)
        // mastery recorded
        assertEquals(1, tracker.getMastery("Grundlagen der KI").correct)
        assertEquals(1, tracker.getMastery("Datenschutz & DSGVO").total)
        // gamification XP called
        coVerify { gamification.addXp(result.earnedXp) }
    }

    @Test
    fun step_after_end_only_handles_once() {
        val gamification = mockk<GamificationRepository>(relaxed = true)
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), gamification)
        vm.start()
        val s = vm.game!!
        s.ended = true
        GameRules.endGame(s, ai.ki_kompetenz_training_org.data.minigames3d.EndReason.TIME)
        vm.step(ai.ki_kompetenz_training_org.data.minigames3d.InputState(false, false, false, false, false, null), 0.016)
        vm.step(ai.ki_kompetenz_training_org.data.minigames3d.InputState(false, false, false, false, false, null), 0.016)
        // only one finish -> one addXp
        coVerify(exactly = 1) { gamification.addXp(any()) }
    }

    @Test
    fun set_lang_switches_statement_language() {
        val vm = vmWith(MasteryTracker(InMemoryPrefsMastery()), mockk(relaxed = true))
        vm.setLang("de")
        vm.setLang("en")
        // no crash
    }
}

/** Minimal SharedPreferences fake for the MasteryTracker. */
private class InMemoryPrefsMastery : android.content.SharedPreferences {
    private val data = mutableMapOf<String, Any?>()
    override fun getAll(): MutableMap<String, *> = data.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = data[key] as? MutableSet<String> ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): android.content.SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(l: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(l: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class Editor : android.content.SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor { if (key != null) pending[key] = value; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor { if (key != null) pending[key] = values; return this }
        override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor { if (key != null) pending[key] = value; return this }
        override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor { if (key != null) pending[key] = value; return this }
        override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor { if (key != null) pending[key] = value; return this }
        override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor { if (key != null) pending[key] = value; return this }
        override fun remove(key: String?): android.content.SharedPreferences.Editor { if (key != null) pending[key] = null; return this }
        override fun clear(): android.content.SharedPreferences.Editor { pending.clear(); return this }
        override fun commit(): Boolean { data.putAll(pending); return true }
        override fun apply() { data.putAll(pending) }
    }
}
