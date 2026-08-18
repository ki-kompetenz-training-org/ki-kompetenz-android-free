package ai.ki_kompetenz_training_org.ui.kibot

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * KiBot scene orchestrator.
 *
 * Composes: KiBotModel + ProgressRing + Particles + Idle bobbing animation + tap reaction.
 *
 * Task 4 scope: idle bobbing, particles, progress ring, tap bounce.
 * Parallax (accelerometer) deferred — requires SensorManager runtime.
 */
@Composable
fun KiBotScene(
    state: KiBotState,
    onTap: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    // ── Idle bobbing animation ──
    val infiniteTransition = rememberInfiniteTransition(label = "kibotBob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val bobDp = sin(bobOffset * Math.PI.toFloat()) * 4f // ±4dp gentle bob

    // ── Tap bounce ──
    var tapBounce by remember { mutableFloatStateOf(0f) }
    val tapBounceAnim by animateFloatAsState(
        targetValue = tapBounce,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
        label = "tapBounce",
    )

    // ── Particles ──
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    LaunchedEffect(state.emotionalBaseline) {
        // Spawn initial particles based on state
        when (state.emotionalBaseline) {
            EmotionalState.CELEBRATING -> particles = spawnCelebrationParticles(20)
            EmotionalState.CONFUSED -> particles = spawnConfusedParticles(5)
            EmotionalState.SLEEPY -> particles = spawnSleepyParticles(3)
            else -> particles = emptyList()
        }
    }
    // Update particles over time
    LaunchedEffect(Unit) {
        while (true) {
            delay(50) // 20fps particle update
            particles = updateParticles(particles, 0.05f)
        }
    }

    // ── XP progress ──
    val xpProgress = if (state.xpNeeded > 0) {
        state.xpIntoLevel.toFloat() / state.xpNeeded
    } else 1f

    // ── Compose layers ──
    BoxWithConstraints(
        modifier = modifier
            .semantics {
                contentDescription = "Dein KI-Begleiter, Level ${state.level}"
            }
            .clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val ringSize = maxWidth.coerceAtLeast(maxHeight)

        // Progress ring (outer layer)
        ProgressRing(
            progress = xpProgress,
            modifier = Modifier.size(ringSize),
        )

        // Particles (middle layer)
        ParticleCanvas(
            particles = particles,
            modifier = Modifier.fillMaxSize(),
        )

        // KiBot model (inner layer, bobbing)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .offset {
                    val yOffset = (bobDp + tapBounceAnim * -8f)
                    IntOffset(0, (yOffset * density.density).toInt())
                }
                .graphicsLayer {
                    // Slight scale pulse on tap
                    scaleX = 1f + tapBounceAnim * 0.05f
                    scaleY = 1f - tapBounceAnim * 0.05f
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        tapBounce = 1f
                        // Spawn happy particles on tap
                        if (particles.isEmpty()) {
                            particles = spawnCelebrationParticles(8)
                        }
                        onTap()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            KiBotModel(stage = state.growthStage)
        }
    }
}

}
