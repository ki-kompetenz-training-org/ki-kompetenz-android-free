/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.quiz

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Ende-zu-Ende-Integration: QuizViewModel + ECHTES ContentRepository mit
 * einer API, die HTTP 500 antwortet (als [HttpException], wie Retrofit es
 * im Gerät wirft — der echte Wire-500 ist in ContentRepositoryTest
 * abgedeckt, hier geht es um die VM-Kette).
 *
 * Das ist der JVM-Beweis für den BUG-Report 2026-09-05 ("Quiz konnte nicht
 * geladen werden") und dessen Fix: Der gebundene Offline-Pool
 * ([ai.ki_kompetenz_training_org.data.api.KiScoreFallback]) muss die komplette
 * Kette füllen — Repository → ViewModel → INTRO mit 10 Fragen — ohne dass
 * die API erreichbar ist. Das Device-Experiment (WLAN aus, KI-Score öffnet)
 * wird hier auf Unit-Ebene reproduziert.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelFallbackIntegrationTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val db: AppDatabase = mockk(relaxed = true)
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** ApiService, der exakt wie Retrofit bei HTTP 500 wirft. */
    private fun failingApi(): ApiService {
        val api: ApiService = mockk()
        coEvery { api.getKiScoreData() } throws HttpException(
            Response.error<Any>(500, "Internal Server Error".toResponseBody(null))
        )
        return api
    }

    private fun viewModel(api: ApiService = failingApi()) =
        QuizViewModel(ContentRepository(api, db), db, gamificationRepository)

    @Test
    fun `API 500 beim Init - VM zeigt INTRO mit 10 Fragen statt ERROR (Offline-Vertrag)`() = runTest {
        val vm = viewModel()

        val s = vm.state.value
        assertThat(s.phase).isEqualTo(QuizPhase.INTRO)
        assertThat(s.error).isNull()
        assertThat(s.questions).hasSize(10)
        assertThat(s.tiers).hasSize(5)
    }

    @Test
    fun `Fallback-Fragen sind vollstaendig spielbar (4 Optionen, korrigierbar)`() = runTest {
        val vm = viewModel()

        vm.state.value.questions.forEach { q ->
            assertThat(q.options).hasSize(4)
            assertThat(q.correct).isAtLeast(0)
            assertThat(q.correct).isLessThan(4)
            assertThat(q.text).isNotEmpty()
        }
    }

    @Test
    fun `Share-Prefix kommt aus dem Fallback (Platzhalter fuer Ergebnis-Screen)`() = runTest {
        val vm = viewModel()

        val prefix = vm.state.value.sharePrefix
        assertThat(prefix).contains("{score}")
        assertThat(prefix).contains("{tier}")
    }

    @Test
    fun `zweiter load() mit erneut 500 bleibt im INTRO (Retry kann nicht kaputtgehen)`() = runTest {
        val vm = viewModel()
        assertThat(vm.state.value.phase).isEqualTo(QuizPhase.INTRO)

        vm.load()

        assertThat(vm.state.value.phase).isEqualTo(QuizPhase.INTRO)
        assertThat(vm.state.value.questions).hasSize(10)
    }
}
