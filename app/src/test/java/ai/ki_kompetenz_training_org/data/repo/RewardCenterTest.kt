package ai.ki_kompetenz_training_org.data.repo

import android.content.SharedPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RewardCenterTest {

    @Test
    fun `emit sets pending event`() = runTest {
        val center = RewardCenter()
        center.emit(RewardEvent.LevelUp(2))
        assertEquals(RewardEvent.LevelUp(2), center.pending.first())
    }

    @Test
    fun `latest event wins`() = runTest {
        val center = RewardCenter()
        center.emit(RewardEvent.LevelUp(2))
        center.emit(RewardEvent.BadgeUnlocked("first_score"))
        assertEquals(RewardEvent.BadgeUnlocked("first_score"), center.pending.first())
    }

    @Test
    fun `consume clears pending`() = runTest {
        val center = RewardCenter()
        center.emit(RewardEvent.LevelUp(3))
        center.consume()
        assertNull(center.pending.first())
    }

    @Test
    fun `pending starts empty`() = runTest {
        assertNull(RewardCenter().pending.first())
    }
}

class LevelUpTrackerTest {

    @Test
    fun `first emission sets baseline without level-up`() {
        val tracker = LevelUpTracker()
        assertNull(tracker.onNext(300)) // level 3 as baseline
    }

    @Test
    fun `crossing a level fires exactly once`() {
        val tracker = LevelUpTracker()
        tracker.onNext(90) // level 1 (L2 starts at 100)
        val event = tracker.onNext(120) // level 2
        assertEquals(RewardEvent.LevelUp(2), event)
        // staying in level 2 does not fire again
        assertNull(tracker.onNext(150))
    }

    @Test
    fun `no fire within the same level`() {
        val tracker = LevelUpTracker()
        tracker.onNext(100) // level 2
        assertNull(tracker.onNext(180)) // still level 2 (L3 = 300)
    }

    @Test
    fun `multi level jump still fires once with the highest level`() {
        val tracker = LevelUpTracker()
        tracker.onNext(50) // level 1
        assertEquals(RewardEvent.LevelUp(3), tracker.onNext(350)) // straight to level 3
    }
}

class BadgeCelebrationTrackerTest {

    private class FakeEditor : SharedPreferences.Editor {
        val values = mutableMapOf<String, Any>()
        override fun putString(key: String?, value: String?) = apply { values[key!!] = value as Any }
        override fun putStringSet(key: String?, values: MutableSet<String>?) =
            apply { this.values[key!!] = values!!.toSet() }
        override fun putInt(key: String?, value: Int) = apply { values[key!!] = value }
        override fun putLong(key: String?, value: Long) = apply { values[key!!] = value }
        override fun putFloat(key: String?, value: Float) = apply { values[key!!] = value }
        override fun putBoolean(key: String?, value: Boolean) = apply { values[key!!] = value }
        override fun remove(key: String?) = apply { values.remove(key) }
        override fun clear() = apply { values.clear() }
        override fun commit() = true
        override fun apply() {}
    }

    private class FakePrefs(initial: Map<String, Any> = emptyMap()) : SharedPreferences {
        val store = initial.toMutableMap()
        override fun getAll(): Map<String, *> = store
        override fun getString(key: String?, defValue: String?) = store[key] as? String ?: defValue
        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?) =
            (store[key] as? Set<String>)?.toMutableSet() ?: defValues
        override fun getInt(key: String?, defValue: Int) = store[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long) = store[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float) = store[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean) = store[key] as? Boolean ?: defValue
        override fun contains(key: String?) = store.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor().apply { values.putAll(store) }
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    private fun prefsFrom(editor: FakeEditor): FakePrefs = FakePrefs(editor.values)

    @Test
    fun `new badge is celebrated exactly once`() {
        val fake = FakePrefs()
        val tracker = BadgeCelebrationTracker(fake)
        val events = tracker.onNext(listOf("first_score"))
        assertEquals(listOf(RewardEvent.BadgeUnlocked("first_score")), events)
    }

    @Test
    fun `already celebrated badge is not re-celebrated after restart`() {
        val first = FakePrefs()
        BadgeCelebrationTracker(first).onNext(listOf("first_score"))
        // simulate persisted set: emulate edit.apply by reading the editor output
        val editorValues = mutableMapOf<String, Any>("celebrated_badges" to setOf("first_score"))
        val second = FakePrefs(editorValues)
        val tracker2 = BadgeCelebrationTracker(second)
        assertEquals(emptyList<RewardEvent.BadgeUnlocked>(), tracker2.onNext(listOf("first_score")))
    }

    @Test
    fun `only unseen badges fire`() {
        val prefs = FakePrefs(mapOf("celebrated_badges" to setOf("first_score")))
        val tracker = BadgeCelebrationTracker(prefs)
        val events = tracker.onNext(listOf("first_score", "streak_3"))
        assertEquals(listOf(RewardEvent.BadgeUnlocked("streak_3")), events)
    }

    @Test
    fun `empty badge list produces no events`() {
        val tracker = BadgeCelebrationTracker(FakePrefs())
        assertEquals(emptyList<RewardEvent.BadgeUnlocked>(), tracker.onNext(emptyList()))
    }
}

class RewardFormatTest {

    @Test
    fun `xp gain is formatted with plus sign`() {
        assertEquals("+25 XP", RewardFormat.xpGain(25))
        assertEquals("+0 XP", RewardFormat.xpGain(0))
    }

    @Test
    fun `check-in animation duration respects remove-animations`() {
        assertEquals(0, RewardFormat.checkInAnimationMs(0f))
        assertEquals(320, RewardFormat.checkInAnimationMs(1f))
        assertEquals(160, RewardFormat.checkInAnimationMs(0.5f))
    }
}
