package ai.ki_kompetenz_training_org.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ai.ki_kompetenz_training_org.ui.theme.KiTokens

/**
 * Skeleton placeholders shown during first load (loading && empty).
 * Deliberately static (no shimmer loop) to keep the app calm and battery-friendly.
 */
@Composable
private fun SkeletonBox(modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** Placeholder for one lesson/home card row. */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(
            Modifier
                .size(44.dp)
                .clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            SkeletonBox(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp),
            )
            Spacer(Modifier.height(6.dp))
            SkeletonBox(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(11.dp),
            )
        }
    }
}

/** A stack of [rows] skeleton cards. */
@Composable
fun SkeletonList(rows: Int = 5, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KiTokens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(KiTokens.CardGap),
    ) {
        repeat(rows.coerceAtLeast(1)) { SkeletonCard() }
    }
}
