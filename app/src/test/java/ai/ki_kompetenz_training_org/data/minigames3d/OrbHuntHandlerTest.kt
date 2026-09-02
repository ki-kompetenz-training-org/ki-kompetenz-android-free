/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native OrbHuntHandler tests (JUnit4 + Truth).
 * Verifiziert Spawn-, Drift- und Entscheidungsverhalten des ORB_HUNT-Modus.
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

/**
 * Tests fuer [OrbHuntHandler].
 *
 * OrbHunt-Spielregeln (Quelle: OrbHuntHandler.kt / GameEngine.kt):
 * - createState mit Content spawnt via topUp() mindestens cfg.minChips Orbs,
 *   die via GameGeometry.randomSpawn() innerhalb der Arena liegen
 *   (cfg.arenaRadius = 15.0, bzw. hinterher clampToArena -> radius - chipRadius).
 * - Jeder Orb traegt ein LiteracyStatement (fact oder risk).
 * - step() bewegt Orbs auf einer Kreisbahn und klemmt sie in die Arena.
 * - onTap auf einen Orb eroeffnet eine PendingDecision mit Statement + Index.
 * - resolveDecision korrekt: Orb wird entfernt, +25 Punkte, Streak +1.
 * - resolveDecision falsch: -1 Health, Orb bleibt stehen, Streak zurueck auf 0.
 * - topUp() mit content==null darf nichts spawnen und muss sofort terminieren
 *   (Freeze-Guard-Regression 2026-09-01).
 */
class OrbHuntHandlerTest {

    private lateinit var content: LiteracyContentProvider
    private val rng: () -> Double = { 0.5 }
    private val tuning = TouchTuning.STANDARD

    @Before
    fun setup() {
        content = object : LiteracyContentProvider {
            override fun randomFact(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Fakt-Text", "Fact text", "Test", false)
            override fun randomRisk(rng: () -> Double): LiteracyStatement =
                LiteracyStatement("Risiko-Text", "Risk text", "Test", true)
        }
    }

    private fun orbHuntState(): Pair<GameState, ModeConfig> {
        val cfg = GameConfig.getModeConfig(GameMode.ORB_HUNT)
        val s = GameEngine.createState(GameMode.ORB_HUNT, content, rng, tuning)
        return s to cfg
    }

    // ========== createState / Spawn ==========

    @Test
    fun createState_mitContent_spawntMindestensMinChipsOrbs() {
        val (s, cfg) = orbHuntState()
        assertThat(s.collectibles.size)
            .isAtLeast(cfg.minChips)
    }

    @Test
    fun createState_mitContent_alleOrbsLiegenInArenaGrenzen() {
        // Quelle: OrbHuntHandler.spawnOrb -> GameGeometry.randomSpawn(arenaRadius, ...)
        // liefert Punkte mit hypot(x,z) <= arenaRadius (15.0); die Arena erlaubt Center
        // bis radius - chipRadius. Es darf kein Orb ausserhalb liegen.
        val (s, cfg) = orbHuntState()
        assertThat(s.collectibles).isNotEmpty()
        for (orb in s.collectibles) {
            val dist = GameGeometry.dist2D(0.0, 0.0, orb.x, orb.z)
            assertWithMessage("Orb bei (${orb.x}, ${orb.z}) darf nicht ausserhalb der Arena liegen")
                .that(dist)
                .isAtMost(cfg.arenaRadius + 1e-9)
            assertThat(abs(orb.x)).isAtMost(cfg.arenaRadius + 1e-9)
            assertThat(abs(orb.z)).isAtMost(cfg.arenaRadius + 1e-9)
        }
    }

    @Test
    fun createState_mitContent_jederOrbHatNichtLeeresStatement() {
        val (s, _) = orbHuntState()
        assertThat(s.collectibles).isNotEmpty()
        for (orb in s.collectibles) {
            val stmt = orb.statement
            assertWithMessage("Orb bei (${orb.x}, ${orb.z}) braucht ein LiteracyStatement")
                .that(stmt)
                .isNotNull()
            assertThat(stmt!!.textDe).isNotEmpty()
            assertThat(stmt.textEn).isNotEmpty()
            assertThat(stmt.domain).isNotEmpty()
        }
    }

    // ========== step() Drift ==========

    @Test
    fun step_verschiebtOrbs_entlangDerKreisbahn() {
        // Quelle: OrbHuntHandler.step -> disk.x += cos(phase + age*0.5)*speed*dt etc.
        val (s, _) = orbHuntState()
        val before = s.collectibles.map { it.x to it.z }
        GameEngine.stepGame(s, content, tuning, rng, 0.5)
        for (i in s.collectibles.indices) {
            val orb = s.collectibles[i]
            val (ox, oz) = before[i]
            val moved = abs(orb.x - ox) + abs(orb.z - oz)
            assertWithMessage("Orb $i muss sich im Schritt bewegen")
                .that(moved)
                .isGreaterThan(0.0)
        }
    }

    @Test
    fun step_haeltOrbsDauerhaftInArenaGrenzen() {
        // Quelle: OrbHuntHandler.step klemmt via GameGeometry.clampToArena
        // auf radius - chipRadius; lang laufende Simulation darf keinen Orb verlieren.
        val (s, cfg) = orbHuntState()
        repeat(120) {
            GameEngine.stepGame(s, content, tuning, rng, 1.0)
        }
        assertThat(s.collectibles).isNotEmpty()
        for (orb in s.collectibles) {
            val dist = GameGeometry.dist2D(0.0, 0.0, orb.x, orb.z)
            assertWithMessage("Orb bei (${orb.x}, ${orb.z}) nach 120s ausserhalb der Arena")
                .that(dist)
                .isAtMost(cfg.arenaRadius + 1e-9)
        }
    }

    // ========== onTap ==========

    @Test
    fun onTap_existierenderOrb_setztPendingDecisionMitStatementUndIndex() {
        val (s, cfg) = orbHuntState()
        val orb = s.collectibles[0]
        OrbHuntHandler.onTap(s, cfg, 0, content, rng)

        val pd = s.pendingDecision
        assertThat(pd).isNotNull()
        assertThat(pd!!.diskIndex).isEqualTo(0)
        assertThat(pd.statement).isSameInstanceAs(orb.statement)
        assertThat(pd.isRisk).isEqualTo(orb.isRisk)
        assertThat(pd.timerMax).isEqualTo(cfg.decisionSeconds)
        assertWithMessage("Decision startet beim angefassten Orb")
            .that(GameGeometry.dist2D(pd.x, pd.z, orb.x, orb.z))
            .isWithin(1e-9).of(0.0)
    }

    @Test
    fun onTap_ungueltigerIndex_setztKeinPendingDecision() {
        val (s, cfg) = orbHuntState()
        OrbHuntHandler.onTap(s, cfg, 999, content, rng)
        OrbHuntHandler.onTap(s, cfg, -1, content, rng)
        assertThat(s.pendingDecision).isNull()
    }

    // ========== resolveDecision ==========

    @Test
    fun resolveDecision_korrekt_entferntOrbUndVergibtScore() {
        val (s, cfg) = orbHuntState()
        val sizeBefore = s.collectibles.size
        val scoreBefore = s.score
        val orb = s.collectibles[0]

        OrbHuntHandler.onTap(s, cfg, 0, content, rng)
        val correctAction = if (orb.isRisk) ClassifyAction.RISK else ClassifyAction.FACT
        GameEngine.resolveDecision(s, correctAction, content, rng, cfg, tuning)

        assertThat(s.pendingDecision).isNull()
        assertWithMessage("Korrekte Antwort entfernt den Orb")
            .that(s.collectibles.size)
            .isEqualTo(sizeBefore - 1)
        assertWithMessage("Korrekte Antwort vergibt CLASSIFY_POINTS")
            .that(s.score)
            .isEqualTo(scoreBefore + GameConfig.CLASSIFY_POINTS)
        assertThat(s.classifyStreak).isEqualTo(1)
        assertThat(s.justScored).isTrue()
    }

    @Test
    fun resolveDecision_falsch_ziehtHealthAbUndLaesstOrbStehen() {
        val (s, cfg) = orbHuntState()
        val sizeBefore = s.collectibles.size
        val healthBefore = s.health
        val orb = s.collectibles[0]

        OrbHuntHandler.onTap(s, cfg, 0, content, rng)
        val wrongAction = if (orb.isRisk) ClassifyAction.FACT else ClassifyAction.RISK
        GameEngine.resolveDecision(s, wrongAction, content, rng, cfg, tuning)

        assertThat(s.pendingDecision).isNull()
        assertWithMessage("Falsche Antwort kostet wrongPoints Health")
            .that(s.health)
            .isEqualTo(healthBefore - cfg.wrongPoints)
        assertWithMessage("Falsche Antwort entfernt den Orb NICHT (er bleibt stehen)")
            .that(s.collectibles.size)
            .isEqualTo(sizeBefore)
        assertThat(s.classifyStreak).isEqualTo(0)
        assertThat(s.justHit).isTrue()
    }

    // ========== topUp ==========

    @Test
    fun topUp_nachManuellemEntfernen_fuelltWiederAufMinChips() {
        // Nach Entfernen von Orbs (z. B. durch richtige Antworten) muss topUp
        // die Arena wieder auf minChips auffuellen.
        val (s, cfg) = orbHuntState()
        s.collectibles.removeAt(0)
        s.collectibles.removeAt(0)
        s.collectibles.removeAt(0)
        assertThat(s.collectibles.size).isLessThan(cfg.minChips)

        OrbHuntHandler.topUp(s, cfg, tuning, rng, content)

        assertThat(s.collectibles.size).isEqualTo(cfg.minChips)
    }

    /**
     * REGRESSION (Freeze-Bug 2026-09-01): topUp mit content==null rief frueher
     * spawnOrb auf, das sofort zurueckkehrte ohne Orb hinzuzufuegen -> die
     * while-Schleife lief endlos -> App-Freeze. Ohne Content muss topUp sofort
     * terminieren und nichts spawnen.
     */
    @Test(timeout = 5000)
    fun topUp_ohneContent_spawntNichtsUndTerminiertSofort() {
        val cfg = GameConfig.getModeConfig(GameMode.ORB_HUNT)
        val s = GameEngine.createState(GameMode.ORB_HUNT, null, rng, tuning)
        assertThat(s.collectibles).isEmpty()

        OrbHuntHandler.topUp(s, cfg, tuning, rng, null)
        OrbHuntHandler.topUp(s, cfg, tuning, rng, null)

        assertThat(s.collectibles).isEmpty()
    }
}
