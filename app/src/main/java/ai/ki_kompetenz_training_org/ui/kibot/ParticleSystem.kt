package ai.ki_kompetenz_training_org.ui.kibot

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Animated XP progress ring wrapping KiBot.
 */
@Composable
fun ProgressRing(
    progress: Float, // 0..1
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "xpProgress",
    )

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val primaryGrad = Brush.sweepGradient(
        colors = listOf(color, color.copy(alpha = 0.5f), color),
    )

    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val dimen = size.minDimension - stroke
        val topLeft = Offset(stroke / 2, stroke / 2)

        // Track
        drawArc(
            color = surfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(dimen, dimen),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        // Progress arc
        if (animatedProgress > 0.01f) {
            drawArc(
                brush = primaryGrad,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(dimen, dimen),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * Lightweight particle data.
 */
data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float,
    val maxLife: Float,
    val emoji: String? = null,
    val color: Color = Color.White,
    val size: Float = 4f,
)

/**
 * Canvas-rendered particle effects for KiBot emotions.
 */
@Composable
fun ParticleCanvas(
    particles: List<Particle>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (alpha < 0.01f) return@forEach

            if (p.emoji != null) {
                // Emoji particles — draw as text
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size * alpha,
                    center = Offset(p.x, p.y),
                )
            } else {
                // Circle sparkle particles
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size * alpha,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}

/**
 * Spawns celebration particles (confetti — upward burst).
 */
fun spawnCelebrationParticles(count: Int = 20): List<Particle> {
    return (0 until count).map {
        val x = (Math.random() * 0.4f + 0.3f) // center cluster
        val vy = -(Math.random() * 3f + 1f)
        Particle(
            x = x.toFloat(),
            y = 0.5f,
            vx = (Math.random() - 0.5f).toFloat() * 1.5f,
            vy = vy.toFloat(),
            life = 1f,
            maxLife = (Math.random() * 0.5f + 0.5f).toFloat(),
            color = listOf(
                Color(0xFFFBBF24), Color(0xFF22D3EE), Color(0xFFA78BFA),
                Color(0xFF34D399), Color(0xFFF472B6),
            ).random(),
            size = (Math.random() * 4f + 2f).toFloat(),
        )
    }
}

/**
 * Spawns confused particles ("?" floating up).
 */
fun spawnConfusedParticles(count: Int = 5): List<Particle> {
    return (0 until count).map {
        val x = (Math.random() * 0.3f + 0.35f).toFloat()
        Particle(
            x = x,
            y = 0.3f,
            vx = (Math.random() - 0.5f).toFloat() * 0.5f,
            vy = -(Math.random() * 0.5f + 0.3f).toFloat(),
            life = 1f,
            maxLife = (Math.random() * 0.5f + 0.8f).toFloat(),
            emoji = "?",
            color = Color(0xFFFBBF24),
            size = 14f,
        )
    }
}

/**
 * Spawns Zzz particles for sleepy state.
 */
fun spawnSleepyParticles(count: Int = 3): List<Particle> {
    return (0 until count).map {
        Particle(
            x = 0.65f + it * 0.05f,
            y = 0.3f - it * 0.05f,
            vx = 0.2f,
            vy = -(Math.random() * 0.3f + 0.1f).toFloat(),
            life = 1f,
            maxLife = (Math.random() * 0.5f + 1f).toFloat(),
            emoji = "💤",
            color = Color.White,
            size = 12f,
        )
    }
}

/**
 * Updates particle positions and lifetimes. Returns updated list (removes dead particles).
 */
fun updateParticles(particles: List<Particle>, dt: Float): List<Particle> =
    particles.mapNotNull { p ->
        val newLife = p.life - dt
        if (newLife <= 0f) null
        else p.copy(
            x = p.x + p.vx * dt,
            y = p.y + p.vy * dt,
            vy = p.vy + 1.5f * dt, // gravity
            life = newLife,
        )
    }
