package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotDao
import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity
import ai.ki_kompetenz_training_org.data.minigames3d.CompetencyMath
import ai.ki_kompetenz_training_org.data.minigames3d.InMemoryPrefs
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * TDD-Tests für [CompetencyRepository].
 *
 * Nutzt einen Fake-DAO (reine JVM, kein Room/Robolectric), ein InMemoryPrefs
 * für den MasteryTracker und eine injizierbare Uhr (`fixedNow`), damit alle
 * Wochen-/XP-Entscheidungen deterministisch sind.
 */
class CompetencyRepositoryTest {

    /** Fake-DAO: hält eine MutableList, upsert ersetzt per weekKey, getRecent/observeRecent
     *  sortieren absteigend nach weekKey und limitieren. */
    private class FakeSnapshotDao : CompetencySnapshotDao {
        val rows = mutableListOf<CompetencySnapshotEntity>()

        override suspend fun upsert(s: CompetencySnapshotEntity) {
            val idx = rows.indexOfFirst { it.weekKey == s.weekKey }
            if (idx >= 0) rows[idx] = s else rows.add(s)
        }

        override fun observeRecent(limit: Int): Flow<List<CompetencySnapshotEntity>> =
            flowOf(recent(limit))

        override suspend fun getRecent(limit: Int): List<CompetencySnapshotEntity> =
            recent(limit)

        private fun recent(limit: Int): List<CompetencySnapshotEntity> =
            rows.sortedByDescending { it.weekKey }.take(limit)
    }

    private lateinit var dao: FakeSnapshotDao
    private lateinit var prefs: InMemoryPrefs
    private lateinit var tracker: MasteryTracker
    private lateinit var gamification: GamificationRepository

    private var fixedNow = 1_000_000L

    @Before
    fun setUp() {
        fixedNow = 1_000_000L
        dao = FakeSnapshotDao()
        prefs = InMemoryPrefs()
        tracker = MasteryTracker(prefs) { fixedNow }
        gamification = mockk<GamificationRepository>(relaxed = true)
    }

    private fun repository(): CompetencyRepository =
        CompetencyRepository(
            snapshotDao = dao,
            tracker = tracker,
            prefs = prefs,
            gamification = gamification,
            nowMs = { fixedNow },
        )

    // ── Snapshots ───────────────────────────────────────────────────────────

    @Test
    fun first_snapshot_created() = runTest {
        val repo = repository()

        val snapshot = repo.recordFromTracker()

        assertThat(dao.rows).hasSize(1)
        assertThat(snapshot.weekKey).matches(Regex("""\d{4}-W\d{2}""").toPattern())
        assertThat(snapshot.kiki).isAtLeast(0)
        assertThat(snapshot.kiki).isAtMost(100)
        assertThat(snapshot.createdAt).isEqualTo(fixedNow)
        // perDomainJson = [score, score, ...] über alle 9 Domänen
        assertThat(snapshot.perDomainJson).startsWith("[")
        assertThat(snapshot.perDomainJson).endsWith("]")
        assertThat(snapshot.perDomainJson.split(",")).hasSize(
            ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank.DOMAINS.size
        )
    }

    @Test
    fun same_week_upsert_keeps_single_row_and_createdAt() = runTest {
        val repo = repository()

        val first = repo.recordFromTracker()
        val second = repo.recordFromTracker()

        assertThat(dao.rows).hasSize(1)
        assertThat(dao.rows.single().weekKey).isEqualTo(first.weekKey)
        assertThat(dao.rows.single().kiki).isEqualTo(second.kiki)
        // createdAt des ersten Snapshots wird beibehalten (nicht überschrieben)
        assertThat(dao.rows.single().createdAt).isEqualTo(first.createdAt)
        assertThat(second.weekKey).isEqualTo(first.weekKey)
    }

    @Test
    fun week_rollover_creates_new_row_and_keeps_old() = runTest {
        val repo = repository()

        val first = repo.recordFromTracker()
        fixedNow += 8L * 24 * 60 * 60 * 1000 // +8 Tage → neue ISO-Woche
        val second = repo.recordFromTracker()

        assertThat(dao.rows).hasSize(2)
        assertThat(second.weekKey).isNotEqualTo(first.weekKey)
        // Alter Snapshot bleibt unverändert erhalten
        val old = dao.rows.first { it.weekKey == first.weekKey }
        assertThat(old).isEqualTo(first)
        assertThat(old.createdAt).isEqualTo(first.createdAt)
    }

    @Test
    fun observeSnapshots_returns_recent_descending() = runTest {
        val repo = repository()
        val weekKeys = mutableListOf<String>()
        repeat(10) {
            val s = repo.recordFromTracker()
            weekKeys += s.weekKey
            fixedNow += 7L * 24 * 60 * 60 * 1000 // +7 Tage → nächste ISO-Woche
        }

        val snapshots = repo.observeSnapshots().first()

        assertThat(snapshots).hasSize(CompetencyRepository.SNAPSHOTS_SHOWN) // 8
        val keys = snapshots.map { it.weekKey }
        // nur die neuesten 8 Wochen (die beiden ältesten fehlen)
        assertThat(keys.toSet()).hasSize(CompetencyRepository.SNAPSHOTS_SHOWN)
        assertThat(keys).doesNotContain(weekKeys.first())
        assertThat(keys).doesNotContain(weekKeys[1])
        // absteigend nach weekKey sortiert (neueste Woche zuerst)
        assertThat(keys).isEqualTo(keys.sortedDescending())
        assertThat(keys.first()).isEqualTo(weekKeys.last())
    }

    // ── XP-Trigger ──────────────────────────────────────────────────────────

    @Test
    fun xp_awarded_once_at_threshold() = runTest {
        val repo = repository()
        repeat(25) { tracker.recordResult("Grundlagen der KI", true) }
        val score = CompetencyMath.domainScore(
            tracker.getMastery("Grundlagen der KI").m,
            tracker.getMastery("Grundlagen der KI").total,
        )
        assertThat(score).isAtLeast(CompetencyRepository.MASTERY_XP_THRESHOLD)

        repo.recordFromTracker()

        coVerify(exactly = 1) { gamification.addXp(CompetencyRepository.MASTERY_XP_REWARD) }
        val rewarded =
            prefs.getStringSet(CompetencyRepository.KEY_REWARDED, mutableSetOf()) ?: emptySet()
        assertThat(rewarded).contains("Grundlagen der KI")
    }

    @Test
    fun xp_not_repeated_on_second_record() = runTest {
        val repo = repository()
        repeat(25) { tracker.recordResult("Grundlagen der KI", true) }

        repo.recordFromTracker()
        repo.recordFromTracker()

        // Belohnung nur einmal insgesamt
        coVerify(exactly = 1) { gamification.addXp(CompetencyRepository.MASTERY_XP_REWARD) }
    }

    // ── observeLatest (Radar/KIKI-Karte) ───────────────────────────────────

    @Test
    fun observeLatest_returns_newest_snapshot_or_null() = runTest {
        val repo = repository()

        // Keine Daten -> null
        assertThat(repo.observeLatest().first()).isNull()

        val first = repo.recordFromTracker()
        assertThat(repo.observeLatest().first()).isEqualTo(first)

        fixedNow += 8L * 24 * 60 * 60 * 1000 // neue Woche
        val second = repo.recordFromTracker()
        assertThat(repo.observeLatest().first()).isEqualTo(second)
        assertThat(dao.rows).hasSize(2)
    }

    @Test
    fun xp_below_threshold_awards_nothing() = runTest {
        val repo = repository()
        tracker.recordResult("Grundlagen der KI", true) // nur 1 Antwort → Score < 70
        val score = CompetencyMath.domainScore(
            tracker.getMastery("Grundlagen der KI").m,
            tracker.getMastery("Grundlagen der KI").total,
        )
        assertThat(score).isLessThan(CompetencyRepository.MASTERY_XP_THRESHOLD)

        repo.recordFromTracker()

        coVerify(exactly = 0) { gamification.addXp(any()) }
        val rewarded =
            prefs.getStringSet(CompetencyRepository.KEY_REWARDED, mutableSetOf()) ?: emptySet()
        assertThat(rewarded).isEmpty()
    }
}
