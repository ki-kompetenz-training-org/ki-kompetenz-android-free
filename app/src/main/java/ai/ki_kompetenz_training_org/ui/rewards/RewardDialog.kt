package ai.ki_kompetenz_training_org.ui.rewards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.repo.Badges
import ai.ki_kompetenz_training_org.data.repo.RewardCenter
import ai.ki_kompetenz_training_org.data.repo.RewardEvent

/**
 * Shared reward celebration dialog (level-up / badge unlocked).
 *
 * - Shows the latest pending reward (latest wins, never stacked).
 * - Dismiss on tap or back; consume is immediate.
 * - Transient: no persistent UI state is created.
 *
 * Place at result moments (quiz result, minigame result, lesson completion)
 * and on home - never inside an active round.
 */
@Composable
fun RewardDialogHost(
    rewardCenter: RewardCenter,
    locale: String = currentLang(),
) {
    var current by remember { mutableStateOf<RewardEvent?>(null) }

    LaunchedEffect(rewardCenter) {
        rewardCenter.pending.collect { event ->
            if (event != null) current = event
        }
    }

    current?.let { event ->
        AlertDialog(
            onDismissRequest = {
                rewardCenter.consume()
                current = null
            },
            confirmButton = {
                TextButton(onClick = {
                    rewardCenter.consume()
                    current = null
                }) {
                    Text(stringResource(R.string.reward_dialog_ok), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    when (event) {
                        is RewardEvent.LevelUp -> stringResource(R.string.reward_levelup_title)
                        is RewardEvent.BadgeUnlocked -> stringResource(R.string.reward_badge_title)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (val e = event) {
                        is RewardEvent.LevelUp -> {
                            Text("\uD83C\uDF1F", style = MaterialTheme.typography.displayMedium)
                            Text(
                                "Level ${e.newLevel}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        is RewardEvent.BadgeUnlocked -> {
                            val badge = Badges.all(locale).firstOrNull { it.id == e.badgeId }
                            Text(badge?.emoji ?: "\uD83C\uDFC5", style = MaterialTheme.typography.displayMedium)
                            Text(
                                badge?.title ?: e.badgeId,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (!badge?.description.isNullOrBlank()) {
                                Text(
                                    badge?.description.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}
