/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.SrsCardDto
import ai.ki_kompetenz_training_org.data.api.SrsDueResponseDto
import ai.ki_kompetenz_training_org.data.api.SrsReviewRequestDto
import ai.ki_kompetenz_training_org.data.api.SrsReviewResponseDto
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit-Tests für [SrsRepository] — der API-Wrapper hinter
 * "Karten wiederholen" (BUG-Report 2026-09-05: SRS-Login-Flow).
 *
 * Vertrag:
 * - getDueCards: extrahiert .cards und kapselt Netzwerkfehler als Result.failure
 * - postReview: success=true → Unit; success=false → Result.failure
 *   (das ist der einzige Ort, der eine "Review failed"-Semantik erzeugt)
 */
class SrsRepositoryTest {

    private lateinit var api: ApiService
    private lateinit var repository: SrsRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = SrsRepository(api)
    }

    // ── getDueCards ──────────────────────────────────────────────────────

    @Test
    fun `getDueCards - extrahiert die Karten aus der API-Antwort`() = runTest {
        val cards = listOf(
            SrsCardDto(id = "card-1", question = "Q1?", answer = "A1"),
            SrsCardDto(id = "card-2", question = "Q2?", answer = "A2"),
        )
        coEvery { api.getDueCards() } returns SrsDueResponseDto(cards = cards, count = 2)

        val result = repository.getDueCards()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(2)
        assertThat(result.getOrThrow()[0].id).isEqualTo("card-1")
    }

    @Test
    fun `getDueCards - leere Due-Liste ist ein Erfolg mit 0 Karten`() = runTest {
        coEvery { api.getDueCards() } returns SrsDueResponseDto(cards = emptyList(), count = 0)

        val result = repository.getDueCards()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `getDueCards - Netzwerkfehler wird zu Result_failure (kein Crash)`() = runTest {
        coEvery { api.getDueCards() } throws java.io.IOException("offline")

        val result = repository.getDueCards()

        assertThat(result.isFailure).isTrue()
    }

    // ── postReview ───────────────────────────────────────────────────────

    @Test
    fun `postReview - cardId und Qualitaet landen im Request`() = runTest {
        val slot = slot<SrsReviewRequestDto>()
        coEvery { api.postReview(capture(slot)) } returns SrsReviewResponseDto(success = true)

        val result = repository.postReview(cardId = "card-7", quality = 4)

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.cardId).isEqualTo("card-7")
        assertThat(slot.captured.quality).isEqualTo(4)
    }

    @Test
    fun `postReview - success=false liefert Result_failure (Review failed)`() = runTest {
        coEvery { api.postReview(any()) } returns SrsReviewResponseDto(success = false)

        val result = repository.postReview(cardId = "card-7", quality = 1)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `postReview - Netzwerkfehler wird zu Result_failure (kein Crash)`() = runTest {
        coEvery { api.postReview(any()) } throws java.io.IOException("offline")

        val result = repository.postReview(cardId = "card-7", quality = 3)

        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 1) { api.postReview(any()) }
    }
}
