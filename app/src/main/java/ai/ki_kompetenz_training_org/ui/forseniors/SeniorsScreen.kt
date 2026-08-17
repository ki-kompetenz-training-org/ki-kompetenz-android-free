package ai.ki_kompetenz_training_org.ui.forseniors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ki_kompetenz_training_org.data.forseniors.SeniorsLesson
import ai.ki_kompetenz_training_org.data.forseniors.SeniorsLessons
import ai.ki_kompetenz_training_org.data.forseniors.SeniorsQuiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorsMenuScreen(onBack: () -> Unit, onOpenLesson: (SeniorsLesson) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Für Senioren", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                "KI verstehen — einfach & praktisch",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Schritt für Schritt KI im Alltag verstehen: Passwörter, Phishing, KI-Telefone, Deepfakes und mehr.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            SeniorsLessons.all.forEachIndexed { index, lesson ->
                Card(
                    onClick = { onOpenLesson(lesson) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(lesson.emoji, fontSize = 32.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${index + 1}. ${lesson.title}",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                lesson.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                        Text(
                            "\u2192",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (index < SeniorsLessons.all.lastIndex) Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Summary card — uses theme container colors for guaranteed contrast
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Ihre 6 Alltags-Regeln",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "\uD83D\uDD12 Starke Passwörter + 2FA\n" +
                            "\uD83C\uDFA3 Phishing: Nicht klicken, nicht antworten\n" +
                            "\uD83D\uDED2 Online kaufen nur mit Käuferschutz\n" +
                            "\uD83D\uDCDE Am Telefon nie Daten nennen\n" +
                            "\uD83D\uDDBC\uFE0F Deepfakes bei vertrauenswürdigen Quellen prüfen\n" +
                            "\uD83D\uDCAC Chatbots: Keine persönlichen Daten",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorsLessonScreen(lesson: SeniorsLesson, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${lesson.emoji} ${lesson.title}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            lesson.sections.forEachIndexed { index, section ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${section.emoji} ${section.title}",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            section.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp,
                        )

                        // Key takeaway — uses tertiaryContainer for guaranteed contrast
                        Spacer(Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                section.keyTakeaway,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        // Quiz — uses secondaryContainer for guaranteed contrast
                        if (section.quiz != null) {
                            Spacer(Modifier.height(12.dp))
                            SeniorsQuizCard(quiz = section.quiz)
                        }
                    }
                }
                if (index < lesson.sections.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SeniorsQuizCard(quiz: SeniorsQuiz) {
    var selectedOption by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "\u2753 Quiz",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(6.dp))
            Text(quiz.question, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            quiz.options.forEachIndexed { i, option ->
                val isRevealed = selectedOption >= 0
                val isCorrect = isRevealed && i == quiz.correctIndex
                val isWrong = isRevealed && selectedOption == i && i != quiz.correctIndex

                val bgColor = when {
                    isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    isWrong -> MaterialTheme.colorScheme.errorContainer
                    isRevealed -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface
                }
                val textColor = when {
                    isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                    isWrong -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Card(
                    onClick = {
                        if (!showResult) {
                            selectedOption = i
                            showResult = true
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = bgColor,
                        contentColor = textColor,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                ) {
                    Text(
                        option,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (showResult) {
                Spacer(Modifier.height(6.dp))
                Text(
                    quiz.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedOption == quiz.correctIndex)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
