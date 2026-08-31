package ai.ki_kompetenz_training_org.ui.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.ui.theme.AudienceMode
import androidx.compose.material3.FilterChip

@Composable
fun LanguageSection(
    currentLang: String,
    onLanguageChange: (String) -> Unit,
) {
    data class LangOption(val key: String, val label: String, val emoji: String)

    val options = listOf(
        LangOption("system", stringResource(R.string.settings_language_system), "\uD83D\uDCF1"),
        LangOption("de", stringResource(R.string.settings_language_de), "\uD83C\uDDE9\uD83C\uDDEA"),
        LangOption("en", stringResource(R.string.settings_language_en), "\uD83C\uDDEC\uD83C\uDDE7"),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            options.forEach { opt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLanguageChange(opt.key) }
                        .then(if (currentLang == opt.key) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(opt.emoji)
                    Spacer(Modifier.width(10.dp))
                    Text(opt.label, modifier = Modifier.weight(1f))
                    if (currentLang == opt.key) Text("\u2713", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}


/**
 * Audience mode selector (standard / kids / seniors).
 * Immediate recomposition: MainActivity observes the same pref and re-applies
 * density + card layout without an app restart.
 */
@Composable
fun AudienceModeSection(
    currentMode: AudienceMode,
    onModeChange: (AudienceMode) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.audience_mode_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                data class ModeOption(val mode: AudienceMode, val labelRes: Int, val emoji: String)
                val options = listOf(
                    ModeOption(AudienceMode.STANDARD, R.string.audience_mode_standard, "👥"),
                    ModeOption(AudienceMode.KIDS, R.string.audience_mode_kids, "👶"),
                    ModeOption(AudienceMode.SENIORS, R.string.audience_mode_seniors, "👴"),
                )
                for (option in options) {
                    FilterChip(
                        selected = currentMode == option.mode,
                        onClick = { onModeChange(option.mode) },
                        label = { Text(stringResource(option.labelRes)) },
                        leadingIcon = { Text(option.emoji) },
                    )
                }
            }
        }
    }
}
