package ai.ki_kompetenz_training_org.ui.forkids

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
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ki_kompetenz_training_org.data.forkids.KidsLesson
import ai.ki_kompetenz_training_org.data.forkids.KidsLessons
import ai.ki_kompetenz_training_org.data.forkids.CoppaNotice
import ai.ki_kompetenz_training_org.data.forkids.KidsQuiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsMenuScreen(onBack: () -> Unit, onOpenLesson: (KidsLesson) -> Unit) {
    var showCoppaNotice by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kids_title), fontWeight = FontWeight.Bold) },
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
            // COPPA Notice banner — primaryContainer for contrast
            Card(
                onClick = { showCoppaNotice = true },
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
                    Text("\uD83D\uDD12", fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.kids_coppa_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        stringResource(R.string.kids_coppa_text),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Mehr erfahren \u2192",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "\uD83E\uDD16 KI-Lernen für Kinder",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.kids_heading_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            KidsLessons.all.forEachIndexed { index, lesson ->
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
                if (index < KidsLessons.all.lastIndex) Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text(
                CoppaNotice.DELETION_HINT,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showCoppaNotice) {
        CoppaNoticeDialog(onDismiss = { showCoppaNotice = false })
    }
}

@Composable
fun CoppaNoticeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(CoppaNotice.HEADING) },
        text = { Text(CoppaNotice.BODY, lineHeight = 20.sp) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.kids_dismiss)) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsLessonScreen(lesson: KidsLesson, onBack: () -> Unit) {
    var completedSections by remember { mutableIntStateOf(0) }

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
                SectionCard(
                    section = section,
                    sectionNumber = index + 1,
                    totalSections = lesson.sections.size,
                    onComplete = { completedSections++ },
                )
                if (index < lesson.sections.lastIndex) Spacer(Modifier.height(12.dp))
            }

            if (completedSections >= lesson.sections.size) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("\uD83C\uDF89", fontSize = 32.sp)
                        Text(
                            stringResource(R.string.kids_done_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            stringResource(R.string.kids_done_text),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    section: ai.ki_kompetenz_training_org.data.forkids.KidsSection,
    sectionNumber: Int,
    totalSections: Int,
    onComplete: () -> Unit,
) {
    var answeredQuiz by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${sectionNumber}. ${section.title}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                section.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp,
            )

            if (section.funFact.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    section.funFact,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (section.quiz != null) {
                Spacer(Modifier.height(12.dp))
                KidsQuizCard(quiz = section.quiz, onAnswered = { answeredQuiz = true })
            }
        }
    }

    LaunchedEffect(answeredQuiz) {
        if (answeredQuiz) onComplete()
    }
}

@Composable
private fun KidsQuizCard(quiz: KidsQuiz, onAnswered: () -> Unit) {
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
                            onAnswered()
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
                    if (selectedOption == quiz.correctIndex) quiz.encouragement
                    else quiz.explanation,
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
