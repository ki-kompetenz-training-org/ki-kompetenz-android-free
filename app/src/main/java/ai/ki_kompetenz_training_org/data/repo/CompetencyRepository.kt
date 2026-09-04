package ai.ki_kompetenz_training_org.data.repo

import android.content.SharedPreferences
import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotDao
import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity
import ai.ki_kompetenz_training_org.data.minigames3d.CompetencyMath
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.WeekFields

/**
 * Persistiert woechentliche KI-Kompetenz-Snapshots (KIKI) ins Room-DB
 * `competency_snapshots` und verdrahtet den einmaligen XP-Belohnungs-Trigger
 * fuer Domaenen-Mastery ab Score >= 70.
 *
 * DSGVO-konform: lokal auf dem Geraet, kein Server-Sync (Muster GamificationRepository).
 */
class CompetencyRepository(
    private val snapshotDao: CompetencySnapshotDao,
    private val tracker: MasteryTracker,
    private val prefs: SharedPreferences,
    private val gamification: GamificationRepository? = null,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    companion object {
        /** Prefs-Key des Sets bereits belohnter Domaenen (Idempotenz des XP-Triggers). */
        const val KEY_REWARDED = "kiki_rewarded_domains"

        /** Domaenen-Score (0..100), ab dem 25 XP vergeben werden. */
        const val MASTERY_XP_THRESHOLD = 70

        /** Einmalige XP-Belohnung pro Domaene beim Ueberschreiten des Schwellwerts. */
        const val MASTERY_XP_REWARD = 25

        /** Anzahl der Snapshots, die [observeSnapshots] liefert (neueste zuerst). */
        const val SNAPSHOTS_SHOWN = 8
    }

    /**
     * Liest alle Domaenen-Mastery-Werte aus dem [MasteryTracker], berechnet den
     * KIKI via [CompetencyMath] und upsertet den Snapshot der aktuellen ISO-Woche.
     * Ein bestehender Snapshot derselben Woche wird aktualisiert und behaelt
     * seinen [CompetencySnapshotEntity.createdAt]. Danach laeuft der XP-Trigger.
     */
    suspend fun recordFromTracker(): CompetencySnapshotEntity {
        val scores = LiteracyBank.DOMAINS.map {
            CompetencyMath.domainScore(tracker.getMastery(it).m, tracker.getMastery(it).total)
        }
        val kiki = CompetencyMath.kiki(scores)
        val now = nowMs()
        val weekKey = Instant.ofEpochMilli(now)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .let { date ->
                val wf = WeekFields.ISO
                "%04d-W%02d".format(
                    date.get(wf.weekBasedYear()),
                    date.get(wf.weekOfWeekBasedYear()),
                )
            }

        // Bestehenden Snapshot der gleichen Woche suchen: createdAt beibehalten,
        // sonst createdAt = jetzt.
        val existing = snapshotDao.getRecent(30).firstOrNull { it.weekKey == weekKey }
        val snapshot = CompetencySnapshotEntity(
            weekKey = weekKey,
            kiki = kiki,
            perDomainJson = scores.joinToString(",", "[", "]"),
            createdAt = existing?.createdAt ?: now,
        )
        snapshotDao.upsert(snapshot)

        // XP-Trigger: einmalig 25 XP pro Domaene, sobald deren Score >= 70 ist.
        rewardMasteryXp(scores)

        return snapshot
    }

    /** Neueste [SNAPSHOTS_SHOWN] Snapshots, absteigend nach Woche. */
    fun observeSnapshots(): Flow<List<CompetencySnapshotEntity>> =
        snapshotDao.observeRecent(SNAPSHOTS_SHOWN)

    /** Der jüngste Snapshot (für Radar/KIKI-Karte), oder null wenn keine Daten. */
    fun observeLatest(): Flow<CompetencySnapshotEntity?> =
        snapshotDao.observeRecent(1).map { it.firstOrNull() }

    /** Einmaliger (nicht-reaktiver) Lesezugriff auf den jüngsten Snapshot. */
    suspend fun latestSnapshot(): CompetencySnapshotEntity? =
        snapshotDao.getRecent(1).firstOrNull()

    /** Einmalige XP-Belohnung pro Domaene ab [MASTERY_XP_THRESHOLD]; idempotent
     *  via [KEY_REWARDED]-Prefs-Set. Fuegt jede Domaene dem Set hinzu, IMMER als
     *  Kopie (toMutableSet), bevor es zurueckgeschrieben wird. */
    private suspend fun rewardMasteryXp(scores: List<Int>) {
        val rewarded = prefs.getStringSet(KEY_REWARDED, null) ?: emptySet()
        val newlyRewarded = mutableListOf<String>()
        for ((domain, score) in LiteracyBank.DOMAINS.zip(scores)) {
            if (score >= MASTERY_XP_THRESHOLD && domain !in rewarded) {
                gamification?.addXp(MASTERY_XP_REWARD)
                newlyRewarded.add(domain)
            }
        }
        if (newlyRewarded.isNotEmpty()) {
            val updated = rewarded.toMutableSet()
            updated.addAll(newlyRewarded)
            prefs.edit().putStringSet(KEY_REWARDED, updated).apply()
        }
    }
}
