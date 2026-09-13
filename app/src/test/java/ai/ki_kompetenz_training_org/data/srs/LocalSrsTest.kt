package ai.ki_kompetenz_training_org.data.srs

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.repo.SrsQuality
import ai.ki_kompetenz_training_org.data.repo.SrsRepository
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.ui.srs.SrsPhase
import ai.ki_kompetenz_training_org.ui.srs.SrsViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests für den lokalen SM-2-Fallback ([LocalSrsAlgorithm], [LocalSrsDeck]).
 * LocalSrs.kt ist bewusst Android-frei, damit der Algorithmus rein in der
 * JVM lauffähig und deterministisch testbar ist (injizierte Uhr).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalSrsTest {

    private val now = 1_700_000_000_000L

    private fun card(interval: Int = 1, repetitions: Int = 0, easiness: Double = 2.5) =
        LocalSrsCard(
            id = "c1",
            question = "Frage?",
            answer = "Antwort",
            lessonId = "Grundlagen der KI",
            easiness = easiness,
            interval = interval,
            repetitions = repetitions,
            nextReview = 0L,
        )

    // ── SM-2: Intervallprogression ─────────────────────────────────────

    @Test
    fun `GOOD - Intervall waechst 1 dann 6 dann per easiness`() {
        var c = card()

        c = LocalSrsAlgorithm.update(c, SrsQuality.GOOD, now)
        assertThat(c.interval).isEqualTo(1)
        assertThat(c.repetitions).isEqualTo(1)
        assertThat(c.nextReview).isEqualTo(now + 86_400_000L)

        c = LocalSrsAlgorithm.update(c, SrsQuality.GOOD, now + 1)
        assertThat(c.interval).isEqualTo(6)
        assertThat(c.repetitions).isEqualTo(2)
        assertThat(c.nextReview).isEqualTo(now + 1 + 6 * 86_400_000L)

        // SM-2: interval = round(prevInterval * prevEasiness); easiness sinkt
        // bei GOOD um 0.14 (2.5 -> 2.36 -> 2.22) -> round(6 * 2.22) = 13
        c = LocalSrsAlgorithm.update(c, SrsQuality.GOOD, now + 2)
        assertThat(c.interval).isEqualTo(13)
        assertThat(c.repetitions).isEqualTo(3)
        assertThat(c.nextReview).isEqualTo(now + 2 + 13 * 86_400_000L)
    }

    @Test
    fun `GOOD - EASY und PERFECT progressieren schneller als GOOD`() {
        // EASY: repetitions 1 -> Intervall 1; delta = 0.1 - (1)*(0.08+1*0.02) = 0 -> easiness bleibt
        var easy = card()
        easy = LocalSrsAlgorithm.update(easy, SrsQuality.EASY, now)
        assertThat(easy.easiness).isWithin(0.0001).of(2.5)
        assertThat(easy.interval).isEqualTo(1)

        // PERFECT: delta = +0.1 -> easiness steigt auf 2.6
        var perfect = card()
        perfect = LocalSrsAlgorithm.update(perfect, SrsQuality.PERFECT, now)
        assertThat(perfect.easiness).isWithin(0.0001).of(2.6)
    }

    @Test
    fun `AGAIN - repetions reset auf 0 und Intervall 1`() {
        var c = card()
        c = LocalSrsAlgorithm.update(c, SrsQuality.GOOD, now)
        c = LocalSrsAlgorithm.update(c, SrsQuality.GOOD, now)
        assertThat(c.interval).isEqualTo(6)

        c = LocalSrsAlgorithm.update(c, SrsQuality.AGAIN, now)
        assertThat(c.repetitions).isEqualTo(0)
        assertThat(c.interval).isEqualTo(1)
        assertThat(c.nextReview).isEqualTo(now + 86_400_000L)
    }

    @Test
    fun `easiness - floort bei 1_3 und sinkt bei AGAIN`() {
        var c = card()
        // AGAIN: delta = 0.1 - 4*(0.08+4*0.02) = -0.54
        c = LocalSrsAlgorithm.update(c, SrsQuality.AGAIN, now)
        assertThat(c.easiness).isWithin(0.0001).of(1.96)

        repeat(10) { c = LocalSrsAlgorithm.update(c, SrsQuality.AGAIN, now + it) }
        assertThat(c.easiness).isEqualTo(1.3)
    }

    @Test
    fun `qualityToInt - bildet alle Stufen korrekt ab`() {
        assertThat(LocalSrsAlgorithm.qualityToInt(SrsQuality.AGAIN)).isEqualTo(1)
        assertThat(LocalSrsAlgorithm.qualityToInt(SrsQuality.HARD)).isEqualTo(2)
        assertThat(LocalSrsAlgorithm.qualityToInt(SrsQuality.GOOD)).isEqualTo(3)
        assertThat(LocalSrsAlgorithm.qualityToInt(SrsQuality.EASY)).isEqualTo(4)
        assertThat(LocalSrsAlgorithm.qualityToInt(SrsQuality.PERFECT)).isEqualTo(5)
    }

    // ── Deck ────────────────────────────────────────────────────────────

    @Test
    fun `getDueCards - nur Karten mit nextReview kleiner gleich now`() {
        val now = 1_000L
        val past = card().copy(id = "past", nextReview = 0L)
        val exactly = card().copy(id = "now", nextReview = now)
        val future = card().copy(id = "future", nextReview = now + 1)

        val due = LocalSrsDeck.getDueCards(now, listOf(past, exactly, future))

        assertThat(due.map { it.id }).containsExactly("past", "now").inOrder()
    }

    @Test
    fun `BUNDLED_CARDS - mindestens 20 Karten`() {
        assertThat(LocalSrsDeck.BUNDLED_CARDS.size).isAtLeast(20)
    }

    @Test
    fun `BUNDLED_CARDS - decken alle 9 Domaenen mit 2 bis 3 Karten ab`() {
        assertThat(LiteracyBank.DOMAINS).hasSize(9)
        val byLesson = LocalSrsDeck.BUNDLED_CARDS.groupBy { it.lessonId }
        assertThat(byLesson.keys).containsExactlyElementsIn(LiteracyBank.DOMAINS)

        LiteracyBank.DOMAINS.forEach { domain ->
            assertThat(byLesson[domain]?.size).isAtLeast(2)
            assertThat(byLesson[domain]?.size).isAtMost(3)
        }
    }

    @Test
    fun `BUNDLED_CARDS - ids eindeutig, Fragen und Antworten nicht leer`() {
        assertThat(LocalSrsDeck.BUNDLED_CARDS.map { it.id }.toSet().size)
            .isEqualTo(LocalSrsDeck.BUNDLED_CARDS.size)
        LocalSrsDeck.BUNDLED_CARDS.forEach {
            assertThat(it.question).isNotEmpty()
            assertThat(it.answer).isNotEmpty()
        }
    }

    @Test
    fun `reviewCard - delegiert an den Algorithmus`() {
        val c = card()
        val reviewed = LocalSrsDeck.reviewCard(c, SrsQuality.EASY, now)

        assertThat(reviewed.easiness).isEqualTo(LocalSrsAlgorithm.update(c, SrsQuality.EASY, now).easiness)
        assertThat(reviewed.nextReview).isEqualTo(now + 86_400_000L)
    }

    // ── Repository-Fallback ──────────────────────────────────────────────

    @Test
    fun `getDueCards - bei API-Fehler lokale Karten statt failure`() = runTest {
        val api = mockk<ApiService>()
        coEvery { api.getDueCards() } throws java.io.IOException("offline")
        val repo = SrsRepository(api)

        val result = repo.getDueCards()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(LocalSrsDeck.BUNDLED_CARDS.size)
    }

    @Test
    fun `getDueCards - localSrsEnabled=false gibt Fehler durch`() = runTest {
        val api = mockk<ApiService>()
        coEvery { api.getDueCards() } throws java.io.IOException("offline")
        val repo = SrsRepository(api)
        repo.localSrsEnabled = false

        assertThat(repo.getDueCards().isFailure).isTrue()
    }

    @Test
    fun `postReview - bei API-Fehler aktualisiert lokale Karte (erfolgreich)`() = runTest {
        val api = mockk<ApiService>()
        coEvery { api.postReview(any()) } throws java.io.IOException("offline")
        val repo = SrsRepository(api)
        val localId = LocalSrsDeck.BUNDLED_CARDS.first().id

        val result = repo.postReview(localId, quality = 3)

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `postReview - unbekannte Karte bleibt failure`() = runTest {
        val api = mockk<ApiService>()
        coEvery { api.postReview(any()) } throws java.io.IOException("offline")
        val repo = SrsRepository(api)

        assertThat(repo.postReview("server-card-42", quality = 3).isFailure).isTrue()
    }

    // ── ViewModel: offline Review ⇐ not logged in ────────────────────────

    private class LocalVmHarness {
        val dispatcher = UnconfinedTestDispatcher()
        val auth: AuthRepository = mockk()
        val srs: SrsRepository = mockk()
        val gamification: GamificationRepository = mockk(relaxed = true)

        init {
            Dispatchers.setMain(dispatcher)
        }
    }

    @Test
    fun `ViewModel - nicht angemeldet mit lokalem Fallback startet lokale Session`() = runTest {
        val h = LocalVmHarness()
        try {
            every { h.auth.isLoggedIn() } returns false
            every { h.srs.localSrsEnabled } returns true

            val vm = SrsViewModel(h.auth, h.srs, h.gamification)

            assertThat(vm.state.value.phase).isEqualTo(SrsPhase.REVIEW)
            assertThat(vm.state.value.isLocal).isTrue()
            assertThat(vm.state.value.localCards).hasSize(LocalSrsDeck.BUNDLED_CARDS.size)
            assertThat(vm.state.value.cards).hasSize(LocalSrsDeck.BUNDLED_CARDS.size)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `ViewModel - lokales Review ruft keine API und zieht weiter`() = runTest {
        val h = LocalVmHarness()
        try {
            every { h.auth.isLoggedIn() } returns false
            every { h.srs.localSrsEnabled } returns true

            val vm = SrsViewModel(h.auth, h.srs, h.gamification)
            val before = vm.state.value.currentIndex
            vm.revealAnswer()
            vm.rate(SrsQuality.GOOD.value)

            assertThat(vm.state.value.currentIndex).isEqualTo(before + 1)
            assertThat(vm.state.value.reviewsDone).isEqualTo(1)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `ViewModel - nicht angemeldet ohne lokalen Fallback zeigt NOT_LOGGED_IN`() = runTest {
        val h = LocalVmHarness()
        try {
            every { h.auth.isLoggedIn() } returns false
            every { h.srs.localSrsEnabled } returns false

            val vm = SrsViewModel(h.auth, h.srs, h.gamification)

            assertThat(vm.state.value.phase).isEqualTo(SrsPhase.NOT_LOGGED_IN)
            assertThat(vm.state.value.isLocal).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
