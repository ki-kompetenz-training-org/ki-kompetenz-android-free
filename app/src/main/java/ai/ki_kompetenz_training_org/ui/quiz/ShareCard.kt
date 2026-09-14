/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.api.KiScoreTierDto
import ai.ki_kompetenz_training_org.ui.theme.KiTokens

/**
 * Baut den geteilten Ergebnistext — rein und testbar (siehe QuizShareTextTest).
 * Reproduziert exakt die bisherige Ausgabe: gefüllter Prefix, Leerzeile, Link.
 */
object QuizShareText {
    const val LINK = "https://ki-kompetenz-training.org/ki-score"

    fun build(score: Int, emoji: String?, tierTitle: String?, prefix: String, link: String): String {
        val filled = prefix
            .replace("{score}", score.toString())
            .replace("{emoji}", emoji ?: "")
            .replace("{tier}", tierTitle ?: "")
        return if (filled.isBlank()) link else "$filled\n\n$link"
    }
}

/**
 * Share-Karte, die off-screen gerendert und als PNG geteilt wird:
 * grosser Score "X/100", Tier-Emoji + Titel, Brand-Zeile "KI-Kompetenz", Link.
 */
@Composable
fun ShareCard(state: QuizUiState, tier: KiScoreTierDto?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(KiTokens.CardRadiusLarge),
        color = Color(0xFF0A66C2),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.home_title),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(20.dp))
            Text(tier?.emoji ?: "\uD83E\uDD16", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "${state.percentScore}/100",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            if (tier?.title != null) {
                Spacer(Modifier.height(8.dp))
                Text(tier!!.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                QuizShareText.LINK,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
