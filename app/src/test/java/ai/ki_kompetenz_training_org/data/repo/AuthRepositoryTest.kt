/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.prefs.TokenStore
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Unit-Tests für [AuthRepository] — die Auth-State-Maschine hinter dem
 * WebView-Login (BUG-Report 2026-09-05: "Anmelden" → weißer Screen).
 *
 * Vertrag:
 * - eingeloggt ⇔ TokenStore hat eine Session (kkt_access-Cookie)
 * - 401-Interceptor: clearToken() + signalReAuth() → UI zeigt Login
 * - nach erneutem Login: resetReAuth() räumt das Flag auf
 */
class AuthRepositoryTest {

    private lateinit var tokenStore: TokenStore
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        tokenStore = mockk(relaxed = true)
        repository = AuthRepository(tokenStore)
    }

    // ── isLoggedIn ───────────────────────────────────────────────────────

    @Test
    fun `isLoggedIn - true wenn TokenStore Session meldet`() {
        every { tokenStore.hasSession() } returns true
        assertThat(repository.isLoggedIn()).isTrue()
    }

    @Test
    fun `isLoggedIn - false ohne Session (kkt_access fehlt)`() {
        every { tokenStore.hasSession() } returns false
        assertThat(repository.isLoggedIn()).isFalse()
    }

    // ── logout ───────────────────────────────────────────────────────────

    @Test
    fun `logout raeumt die Session im TokenStore auf`() {
        repository.logout()
        verify(exactly = 1) { tokenStore.clearSession() }
    }

    // ── 401-Zyklus ───────────────────────────────────────────────────────

    @Test
    fun `kein Re-Auth initial (frische Installation)`() {
        assertThat(repository.reauthRequired).isFalse()
    }

    @Test
    fun `signalReAuth setzt das Flag (401-Interceptor)`() {
        repository.signalReAuth()
        assertThat(repository.reauthRequired).isTrue()
    }

    @Test
    fun `clearToken raeumt Session auf (401-Pfad)`() {
        repository.clearToken()
        verify(exactly = 1) { tokenStore.clearSession() }
    }

    @Test
    fun `resetReAuth nach erneutem Login raeumt das Flag auf`() {
        repository.signalReAuth()
        assertThat(repository.reauthRequired).isTrue()

        repository.resetReAuth()
        assertThat(repository.reauthRequired).isFalse()
    }

    @Test
    fun `vollstaendiger 401-Zyklus - Flag an, Session weg, Flag wieder aus`() {
        every { tokenStore.hasSession() } returns true andThen false

        assertThat(repository.isLoggedIn()).isTrue()
        repository.signalReAuth()
        repository.clearToken()
        assertThat(repository.isLoggedIn()).isFalse()
        assertThat(repository.reauthRequired).isTrue()

        repository.resetReAuth()
        assertThat(repository.reauthRequired).isFalse()
    }
}
