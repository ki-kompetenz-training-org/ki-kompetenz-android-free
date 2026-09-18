/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Parser fuer den F-Droid-Repo-Index (index-v1.json): liefert den hoechsten
 * versionCode + versionName eines Pakets. Netzlos, reine Funktion — der
 * Update-Check selbst ist best-effort (null bei jedem Fehler).
 */
class UpdateCheckTest {

    private fun index(vararg entries: String, pkg: String = "ai.ki_kompetenz_training_org.free") =
        """{"packages":{"$pkg":[${entries.joinToString(",")}],"other":[]},"repo":{"name":"x"}}"""

    @Test
    fun `liefert hoechsten versionCode und dessen versionName`() {
        val json = index(
            """{"versionCode":28,"versionName":"1.7.0"}""",
            """{"versionCode":30,"versionName":"1.7.2"}""",
            """{"versionCode":26,"versionName":"1.6.0"}""",
        )
        val latest = UpdateCheck.parseLatestVersion(json, "ai.ki_kompetenz_training_org.free")
        assertThat(latest).isEqualTo(30 to "1.7.2")
    }

    @Test
    fun `unbekanntes Paket liefert null`() {
        val json = index("""{"versionCode":30,"versionName":"1.7.2"}""")
        assertThat(UpdateCheck.parseLatestVersion(json, "ai.kompetenz.anders")).isNull()
    }

    @Test
    fun `defektes JSON liefert null statt Crash`() {
        assertThat(UpdateCheck.parseLatestVersion("{not json", "pkg")).isNull()
        assertThat(UpdateCheck.parseLatestVersion("""{"packages":{}}""", "pkg")).isNull()
        assertThat(UpdateCheck.parseLatestVersion("""{"packages":{"pkg":[]}}""", "pkg")).isNull()
    }
}
