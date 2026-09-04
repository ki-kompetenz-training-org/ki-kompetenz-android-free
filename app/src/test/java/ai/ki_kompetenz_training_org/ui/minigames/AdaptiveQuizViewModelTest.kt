/*
 * Copyright 2026 Tobias Weiss
 * Adaptive retrieval-practice ViewModel tests.
 * Scientific basis: Testing Effect (Roediger & Karpicke 2006),
 * adaptive interleaving (Bjork), immediate feedback (Hattie).
 */
package ai.ki_kompetenz_training_org.ui.minigames

import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.Difficulty
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGameKind
import ai.ki_kompetenz_training_org.data.minigames3d.CompetencyMath
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import ai.ki_kompetenz_training_org.data.repo.CompetencyRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import android.content.SharedPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class AdaptiveQuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var gamification: GamificationRepository
    private lateinit var dailyRepo: DailyChallengeRepository
    private lateinit var competencyRepo: CompetencyRepository
    private lateinit var prefs: FakePrefs

    private val game = MiniGame(
        id = "orb_hunt", emoji = "侦探",
        titleDe = "KI-Detektiv", titleEn = "AI Detective",
        descriptionDe = "Alle", descriptionEn = "All",
        rounds = emptyList(),
        kind = MiniGameKind.ADAPTIVE_QUIZ,
        difficulty = Difficulty.BEGINNER,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        gamification = mockk(relaxed = true)
        coEvery { gamification.onMiniGameFinished(any(), any(), any()) } returns Unit
        coEvery { gamification.addXp(any()) } returns Unit
        dailyRepo = mockk(relaxed = true)
        every { dailyRepo.getTodayChallenge(any(), any()) } returns null
        competencyRepo = mockk(relaxed = true)
        prefs = FakePrefs()
    }

    private fun vm(filter: List<String>? = null, seed: Long = 42) = AdaptiveQuizViewModel(
        game = game.copy(domainFilter = filter),
        gamification = gamification,
        mastery = MasteryTracker(prefs),
        dailyChallengeRepository = dailyRepo,
        competencyRepository = competencyRepo,
        rng = Random(seed),
    )

    private fun answerAll(vm: AdaptiveQuizViewModel, correct: Boolean) {
        val n = vm.state.value.rounds.size
        repeat(n) {
            val s = vm.state.value
            val round = s.rounds[s.currentIndex]
            vm.selectAnswer(if (correct) round.isRisk else !round.isRisk)
            vm.next()
        }
    }

    @Test
    fun `session has 10 unique rounds with consistent risk flags`() {
        val vm = vm()
        val s = vm.state.value
        assertEquals(AdaptiveQuizViewModel.SESSION_SIZE, s.rounds.size)
        assertEquals(s.rounds.size, s.rounds.map { it.statement.textDe }.toSet().size)
        s.rounds.forEach { round ->
            assertTrue(LiteracyBank.DOMAINS.contains(round.domain))
            assertEquals(round.statement.isRisk, round.isRisk)
        }
    }

    @Test
    fun `domain filter restricts rounds to allowed domain`() {
        val vm = vm(filter = listOf("Grundlagen der KI"))
        val s = vm.state.value
        assertTrue(s.rounds.isNotEmpty())
        s.rounds.forEach { round ->
            assertEquals("Grundlagen der KI", round.domain)
            val pool = LiteracyBank.facts(round.domain) + LiteracyBank.risks(round.domain)
            assertTrue(pool.any { it.textDe == round.statement.textDe })
        }
    }

    @Test
    fun `correct answers are tracked`() {
        val vm = vm()
        repeat(3) {
            val round = vm.state.value.rounds[vm.state.value.currentIndex]
            vm.selectAnswer(round.isRisk)
            vm.next()
        }
        val s = vm.state.value
        assertEquals(3, s.answers.size)
        assertTrue(s.answers.all { it })
    }

    @Test
    fun `second answer on same round is ignored`() {
        val vm = vm()
        vm.selectAnswer(true)
        vm.selectAnswer(false)
        assertEquals(1, vm.state.value.answers.size)
        assertEquals(true, vm.state.value.selectedAnswer)
    }

    @Test
    fun `next without answer does not advance`() {
        val vm = vm()
        vm.next()
        assertEquals(0, vm.state.value.currentIndex)
    }

    @Test
    fun `perfect session finishes with xp and repository call`() {
        val vm = vm()
        answerAll(vm, correct = true)
        val s = vm.state.value
        assertEquals(AdaptivePhase.RESULT, s.phase)
        assertEquals(10, s.answers.size)
        assertTrue(s.answers.all { it })
        assertEquals(40, s.earnedXp)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { gamification.onMiniGameFinished(10, 10, gameId = "orb_hunt") }
    }

    @Test
    fun `mastery is recorded on finish`() {
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        val tracker = MasteryTracker(prefs)
        assertEquals(1, tracker.totalGames())
        val touched = prefs.store.keys.count { it.startsWith("mg3d_mastery_") }
        assertTrue(touched > 0)
    }

    @Test
    fun `all wrong session yields zero xp and all domains weak`() {
        LiteracyBank.DOMAINS.forEach { d -> prefs.store["mg3d_mastery_$d"] = "0/3" }
        val vm = vm()
        answerAll(vm, correct = false)
        val s = vm.state.value
        assertEquals(AdaptivePhase.RESULT, s.phase)
        assertEquals(0, s.earnedXp)
        assertEquals(LiteracyBank.DOMAINS.size, s.weakDomains.size)
    }

    @Test
    fun `restart creates fresh playing session`() {
        val vm = vm()
        answerAll(vm, correct = true)
        assertEquals(AdaptivePhase.RESULT, vm.state.value.phase)
        vm.restart()
        val s = vm.state.value
        assertEquals(AdaptivePhase.PLAYING, s.phase)
        assertEquals(AdaptiveQuizViewModel.SESSION_SIZE, s.rounds.size)
        assertEquals(0, s.currentIndex)
        assertEquals(0, s.answers.size)
    }

    @Test
    fun `daily challenge completion awards bonus xp`() {
        every { dailyRepo.getTodayChallenge(any(), any()) } returns game
        every { dailyRepo.isCompletedToday(any()) } returns false
        every { dailyRepo.completeChallenge(any(), any()) } returns 50
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(50, vm.state.value.dailyChallengeBonusXp)
        coVerify(exactly = 1) { gamification.addXp(50) }
        verify(exactly = 1) { dailyRepo.completeChallenge(any(), true) }
    }

    @Test
    fun `non challenge game gives no daily bonus`() {
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, vm.state.value.dailyChallengeBonusXp)
        coVerify(exactly = 0) { gamification.addXp(any()) }
        verify(exactly = 0) { dailyRepo.completeChallenge(any(), any()) }
    }

    @Test
    fun pickDomain_weights_forgotten_over_mastered() {
        val masteredDomain = LiteracyBank.DOMAINS[0]
        val forgottenDomain = LiteracyBank.DOMAINS[1]
        val now = 1_700_000_000_000L
        prefs.store["mg3d_mastery_$masteredDomain"] = CompetencyMath.encodeV2(1.0, 20, now)
        prefs.store["mg3d_mastery_$forgottenDomain"] =
            CompetencyMath.encodeV2(1.0, 20, now - 28L * 24 * 3600 * 1000)
        val vm = AdaptiveQuizViewModel(
            game = game,
            gamification = gamification,
            mastery = MasteryTracker(prefs, nowMs = { now }),
            dailyChallengeRepository = dailyRepo,
            rng = Random(7),
        )
        var masteredCount = 0
        var forgottenCount = 0
        repeat(200) {
            when (vm.pickDomain(listOf(masteredDomain, forgottenDomain))) {
                forgottenDomain -> forgottenCount++
                else -> masteredCount++
            }
        }
        // m_eff(forgotten) = 1.0 * 2^(-28/14) = 0.25 -> weight = 2.6 - 2.3*0.25 = 2.025
        // m_eff(mastered) = 1.0 -> weight = 2.6 - 2.3*1.0 = 0.3 (~6.75 : 1)
        assertTrue("forgotten expected far more draws, got $forgottenCount", forgottenCount > 150)
        assertTrue("mastered expected far fewer draws, got $masteredCount", masteredCount < 50)
    }

    @Test
    fun kiki_in_result_state() {
        val vm = vm()
        answerAll(vm, correct = true)
        val s = vm.state.value
        assertEquals(AdaptivePhase.RESULT, s.phase)
        assertTrue("kiki expected > 0, got ${s.kiki}", s.kiki > 0)
        assertEquals(LiteracyBank.DOMAINS.size, s.domainScores.size)
        assertTrue("at least one domain score expected > 0", s.domainScores.any { it.score > 0 })
    }

    @Test
    fun kiki_zero_for_fresh_prefs() {
        val vm = vm()
        assertEquals(0, vm.state.value.kiki)
        assertTrue(vm.state.value.domainScores.isEmpty())
    }

    @Test
    fun `competency snapshot recorded on finish`() {
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { competencyRepo.recordFromTracker() }
    }

    @Test
    fun `kiki delta from previous snapshot reaches result state`() {
        coEvery { competencyRepo.latestSnapshot() } returns
            ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity("2026-W35", 54, "[]", 0L)
        coEvery { competencyRepo.recordFromTracker() } returns
            ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity("2026-W36", 61, "[]", 0L)
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(54, vm.state.value.previousKiki)
    }

    @Test
    fun `no previous snapshot keeps previousKiki null`() {
        coEvery { competencyRepo.latestSnapshot() } returns null
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(null, vm.state.value.previousKiki)
    }

    @Test
    fun `competency repo failure does not block quiz`() {
        coEvery { competencyRepo.recordFromTracker() } throws RuntimeException("DB error")
        val vm = vm()
        answerAll(vm, correct = true)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(AdaptivePhase.RESULT, vm.state.value.phase)
        coVerify(exactly = 1) { gamification.onMiniGameFinished(any(), any(), any()) }
    }
}

/** In-memory SharedPreferences with write-through editor for unit tests. */
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
    override fun edit(): SharedPreferences.Editor = FakeEditor(store)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
}

private class FakeEditor(private val store: MutableMap<String, Any>) : SharedPreferences.Editor {
    override fun putString(key: String?, value: String?) = apply { if (key != null) { if (value == null) store.remove(key) else store[key] = value } }
    override fun putStringSet(key: String?, values: MutableSet<String>?) = apply { if (key != null) { if (values == null) store.remove(key) else store[key] = values } }
    override fun putInt(key: String?, value: Int) = apply { if (key != null) store[key] = value }
    override fun putLong(key: String?, value: Long) = apply { if (key != null) store[key] = value }
    override fun putFloat(key: String?, value: Float) = apply { if (key != null) store[key] = value }
    override fun putBoolean(key: String?, value: Boolean) = apply { if (key != null) store[key] = value }
    override fun remove(key: String?) = apply { store.remove(key) }
    override fun clear() = apply { store.clear() }
    override fun commit() = true
    override fun apply() {}
}
