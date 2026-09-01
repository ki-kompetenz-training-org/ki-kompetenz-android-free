/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchTuningTest {

    @Test fun standard_values() {
        with(TouchTuning.STANDARD) {
            assertEquals(1.0, speedMultiplier, 0.001)
            assertNull(decisionSeconds)
            assertEquals(1.0, spawnRateMultiplier, 0.001)
        }
    }

    @Test fun kids_fasterSpawning() {
        assertTrue(
            "KIDS spawnRate ${TouchTuning.KIDS.spawnRateMultiplier} > STANDARD 1.0",
            TouchTuning.KIDS.spawnRateMultiplier > 1.0
        )
    }

    @Test fun seniors_slowerSpeed() {
        assertTrue(
            "SENIORS speed ${TouchTuning.SENIORS.speedMultiplier} < STANDARD 1.0",
            TouchTuning.SENIORS.speedMultiplier < 1.0
        )
    }

    @Test fun seniors_longerDecisionTime() {
        val seniorsTime = TouchTuning.SENIORS.decisionSeconds
        assertNotNull("SENIORS decisionSeconds should not be null", seniorsTime)
        assertTrue("SENIORS time $seniorsTime > 10", seniorsTime!! > 10.0)
    }
}
