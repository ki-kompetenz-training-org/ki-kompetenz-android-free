/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.srs

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Persistenz des lokalen SM-2-Decks (Offline-Fallback): Zustand als JSON
 * roundtripen und beim Laden ueber die gebuendelten Karten stuelpen.
 * Hintergrund: Offline-Reviews wurden bisher verworfen — jede Karte blieb
 * ewig faellig. Der Fallback verdient echten SM-2-Fortschritt.
 */
class LocalSrsPersistenceTest {

    private val card = LocalSrsCard(
        id = "local-basics-1", question = "q", answer = "a", lessonId = "lesson-1",
        easiness = 2.3, interval = 6, repetitions = 2, nextReview = 1_760_000_000_000L,
    )

    @Test
    fun `serialize zu deserialize ist identisch`() {
        val json = LocalSrsDeck.serializeState(mapOf("local-basics-1" to card))
        val back = LocalSrsDeck.deserializeState(json)
        assertThat(back).containsExactly("local-basics-1", card)
    }

    @Test
    fun `mergeState ersetzt gebundelte Karte per id und ignoriert Unbekannte`() {
        val bundled = LocalSrsDeck.BUNDLED_CARDS
        val merged = LocalSrsDeck.mergeState(mapOf("local-basics-1" to card, "gibts-nicht" to card))
        assertThat(merged).hasSize(bundled.size)
        assertThat(merged.first { it.id == "local-basics-1" }).isEqualTo(card)
        // unberuehrte Karte bleibt Bundled-Default
        val untouched = bundled.first { it.id != "local-basics-1" }
        assertThat(merged.first { it.id == untouched.id }).isEqualTo(untouched)
    }

    @Test
    fun `defektes oder leeres JSON liefert leeren Zustand statt Crash`() {
        assertThat(LocalSrsDeck.deserializeState("{ kaputt")).isEmpty()
        assertThat(LocalSrsDeck.deserializeState("")).isEmpty()
    }
}
