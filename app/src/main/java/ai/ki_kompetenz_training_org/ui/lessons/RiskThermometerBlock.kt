package ai.ki_kompetenz_training_org.ui.lessons

import androidx.compose.animation.core.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ki_kompetenz_training_org.R

/**
 * Interactive 3D-style Risk Thermometer for EU AI Act.
 * User taps risk levels to explore each tier with animated transitions.
 * Pure Compose — no actual 3D engine needed for this visualization.
 */
data class RiskLevel(
    val labelDe: String,
    val labelEn: String,
    val emoji: String,
    val color: Color,
    val descriptionDe: String,
    val descriptionEn: String,
    val examplesDe: List<String>,
    val examplesEn: List<String>,
    val obligationsDe: String,
    val obligationsEn: String,
)

object EuAiActRiskLevels {
    val levels = listOf(
        RiskLevel(
            labelDe = "Minimales Risiko",
            labelEn = "Minimal Risk",
            emoji = "🟢",
            color = Color(0xFF22C55E),
            descriptionDe = "KI mit minimaler Auswirkung auf Menschen. Keine regulierten Pflichten.",
            descriptionEn = "AI with minimal impact on people. No regulatory obligations.",
            examplesDe = listOf("KI-gestützte Videospiele", "Spam-Filter", "Autokorrektur"),
            examplesEn = listOf("AI-powered video games", "Spam filter", "Autocorrect"),
            obligationsDe = "Keine — frei nutzbar",
            obligationsEn = "None — free to use",
        ),
        RiskLevel(
            labelDe = "Geringes Risiko",
            labelEn = "Low Risk",
            emoji = "🟡",
            color = Color(0xFFEAB308),
            descriptionDe = "KI interagiert mit Menschen, aber mit begrenzter Auswirkung. Transparenz ist Pflicht.",
            descriptionEn = "AI interacts with people but with limited impact. Transparency is required.",
            examplesDe = listOf("Chatbots", "KI-Generierte Inhalte", "Deepfake-Erkennung"),
            examplesEn = listOf("Chatbots", "AI-generated content", "Deepfake detection"),
            obligationsDe = "Transparenzpflicht: Nutzer müssen wissen, dass sie mit KI interagieren",
            obligationsEn = "Transparency obligation: Users must know they're interacting with AI",
        ),
        RiskLevel(
            labelDe = "Hohes Risiko ⚠️",
            labelEn = "High Risk ⚠️",
            emoji = "🟠",
            color = Color(0xFFF97316),
            descriptionDe = "KI mit erheblichen Auswirkungen auf Menschen in kritischen Bereichen. Strenge Pflichten.",
            descriptionEn = "AI with significant impact on people in critical areas. Strict obligations.",
            examplesDe = listOf(
                "HR-Bewerbungssoftware",
                "Medizinische Diagnostik-KI",
                "Kreditwürdigkeitsprüfung",
                "Bildungsbewertung",
                "Strafverfolgung",
            ),
            examplesEn = listOf(
                "HR recruitment software",
                "Medical diagnostic AI",
                "Creditworthiness assessment",
                "Education grading",
                "Law enforcement",
            ),
            obligationsDe = "• Risikomanagement\n• Datensatz-Dokumentation\n• Transparenz\n• Menschliche Aufsicht\n• Accuracy, Robustheit, Cybersicherheit\n• Registrierung EU-Datenbank",
            obligationsEn = "• Risk management\n• Dataset documentation\n• Transparency\n• Human oversight\n• Accuracy, robustness, cybersecurity\n• EU database registration",
        ),
        RiskLevel(
            labelDe = "Unannehmbares Risiko 🚫",
            labelEn = "Unacceptable Risk 🚫",
            emoji = "🔴",
            color = Color(0xFFEF4444),
            descriptionDe = "KI-Systeme, die Grundrechte verletzen. Komplett VERBOTEN.",
            descriptionEn = "AI systems that violate fundamental rights. Completely BANNED.",
            examplesDe = listOf(
                "Social Scoring (China-Style)",
                "Manipulative Unterkünstliche Intelligenz",
                "Live-Gesichtserkennung (öffentlich, Echtzeit)",
                "Vorhersage von Kriminalität",
            ),
            examplesEn = listOf(
                "Social scoring (China-style)",
                "Manipulative subliminal AI",
                "Real-time remote biometric ID (public)",
                "Predictive crime",
            ),
            obligationsDe = "❌ VERBOTEN — Darf nicht betrieben werden\n• Strafen bis zu €35 Mio. oder 7% des Weltumsatzes",
            obligationsEn = "❌ BANNED — Must not be operated\n• Fines up to €35M or 7% of global turnover",
        ),
    )
}

@Composable
fun RiskThermometerBlock(
    locale: String,
    onInteracted: () -> Unit,
) {
    var selectedLevel by remember { mutableStateOf(-1) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Title
        Text(
            if (locale == "en") "🌡️ EU AI Act Risk Thermometer"
            else "🌡️ EU AI Act Risiko-Thermometer",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            if (locale == "en") "Tap a risk level to explore"
            else "Tippe auf eine Risikostufe zum Erkunden",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        // Thermometer visualization
        // FIX (BUG 2026-09-01): Der Glow ist jetzt als Overlay innerhalb
        // eines BoxWithConstraints positioniert (relativer Segmentmittelpunkt
        // via ThermometerMath.glowCenterFraction × REAL gemessene Höhe).
        // Vorher: Flow-Child der Column mit harter 260dp-Annahme → falsche
        // Position + 56dp Layout-Verschiebung bei Auswahl.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Thermometer bar (left)
            BoxWithConstraints(
                modifier = Modifier
                    .width(48.dp)
                    .heightIn(min = 260.dp),
            ) {
                val barHeightPx = maxHeight
                EuAiActRiskLevels.levels.forEachIndexed { index, level ->
                    val isSelected = selectedLevel == index

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeightPx / EuAiActRiskLevels.levels.size)
                            .align(Alignment.TopStart)
                            .offset(y = barHeightPx / EuAiActRiskLevels.levels.size * index)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 0.dp,
                                shape = CircleShape,
                            )
                            .clip(CircleShape)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            color = level.color,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Text(
                                    level.emoji,
                                    fontSize = if (isSelected) 24.sp else 18.sp,
                                )
                            }
                        }
                    }
                }

                // Selection indicator (3D-style glow) — Overlay, kein Flow-Child:
                if (selectedLevel >= 0) {
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "glow",
                    )
                    val glowFraction = ThermometerMath.glowCenterFraction(
                        selectedLevel = selectedLevel,
                        totalLevels = EuAiActRiskLevels.levels.size,
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = barHeightPx * glowFraction - 28.dp)
                            .size(56.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = EuAiActRiskLevels.levels[selectedLevel].color.copy(alpha = glowAlpha),
                            shape = CircleShape,
                        ) {}
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            // Detail panel (right)
            if (selectedLevel >= 0) {
                val level = EuAiActRiskLevels.levels[selectedLevel]
                RiskLevelDetail(
                    level = level,
                    locale = locale,
                    modifier = Modifier.weight(1f),
                    onDismiss = {
                        selectedLevel = -1
                        onInteracted()
                    },
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "👈",
                        fontSize = 32.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (locale == "en") "Tap a level\nto see details"
                        else stringResource(R.string.risk_tap_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskLevelDetail(
    level: RiskLevel,
    locale: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val title = if (locale == "en") level.labelEn else level.labelDe
    val desc = if (locale == "en") level.descriptionEn else level.descriptionDe
    val examples = if (locale == "en") level.examplesEn else level.examplesDe
    val obligations = if (locale == "en") level.obligationsEn else level.obligationsDe

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = level.color.copy(alpha = 0.1f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            level.color.copy(alpha = 0.3f),
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(level.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = level.color,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss, contentPadding = PaddingValues(0.dp)) {
                    Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Examples — FIX (BUG 2026-09-01): war vorher in beiden Locales deutsch
            Text(
                ThermometerMath.examplesLabel(locale),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            examples.forEach { ex ->
                Text("• $ex", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            // Obligations — FIX (BUG 2026-09-01): sprachabhängig via ThermometerMath
            Text(
                ThermometerMath.obligationsLabel(locale),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            obligations.lines().forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
