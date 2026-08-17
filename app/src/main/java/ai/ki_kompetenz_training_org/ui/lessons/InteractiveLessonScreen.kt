package ai.ki_kompetenz_training_org.ui.lessons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.lessons.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules

// ── Locale helper ──────────────────────────────────────────────────────────

private fun localized(
    locale: String,
    de: String,
    en: String,
): String = if (locale == "en") en else de

private fun localized(
    locale: String,
    de: List<String>,
    en: List<String>,
): List<String> = if (locale == "en") en else de

// ── Main screen ───────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InteractiveLessonScreen(
    lesson: InteractiveLesson,
    locale: String = "de",
    onBack: () -> Unit,
    onMarkCompleted: (String) -> Unit,
    onOpenPremium: () -> Unit = {},
    completedSections: Set<Int> = emptySet(),
    quizScores: Map<Int, Int> = emptyMap(), // sectionIndex -> score (0-100)
) {
    var completedSectionsState by remember { mutableStateOf(completedSections.toMutableSet()) }
    var quizScoresState by remember { mutableStateOf(quizScores.toMutableMap()) }
    var allQuizzesPassed by remember {
        mutableStateOf(lesson.sections.indices.all { idx ->
            val sec = lesson.sections[idx]
            val hasQuiz = sec.blocks.any { it is ContentBlock.Quiz }
            !hasQuiz || (quizScoresState.getOrDefault(idx, 0) >= 60)
        })
    }
    // Track which blocks have been interacted with per section
    val interactedBlocks = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localized(locale, lesson.titleDe, lesson.titleEn)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
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
        ) {
            // ── Header ─────────────────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    localized(locale, lesson.titleDe, lesson.titleEn),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    localized(locale, lesson.descriptionDe, lesson.descriptionEn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))

                // Objectives
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            if (locale == "en") "🎯 Learning objectives" else "🎯 Lernziele",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        localized(locale, lesson.objectivesDe, lesson.objectivesEn).forEach { obj ->
                            Text(
                                "• $obj",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // ── Sections ──────────────────────────────────────────────
            lesson.sections.forEachIndexed { secIdx, section ->
                SectionBlock(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    section = section,
                    locale = locale,
                    sectionIndex = secIdx,
                    isCompleted = completedSectionsState.contains(secIdx),
                    quizScore = quizScoresState.getOrDefault(secIdx, -1),
                    interactedBlocks = interactedBlocks,
                    onSectionComplete = { completedSectionsState.add(secIdx) },
                    onQuizScore = { score ->
                        quizScoresState[secIdx] = score
                        // Recalculate
                        allQuizzesPassed = lesson.sections.indices.all { i ->
                            val s = lesson.sections[i]
                            val hasQuiz = s.blocks.any { it is ContentBlock.Quiz }
                            !hasQuiz || (quizScoresState.getOrDefault(i, 0) >= 60)
                        }
                    },
                )
            }

            // ── Complete button ────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onMarkCompleted(lesson.id) },
                enabled = allQuizzesPassed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
            ) {
                if (allQuizzesPassed) {
                    Text(
                        stringResource(R.string.lesson_mark_complete, GamificationRules.xpPerCompletedLesson),
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text("✅ Quizzes bestehen, um abzuschließen", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Section renderer ───────────────────────────────────────────────────────

@Composable
private fun SectionBlock(
    modifier: Modifier,
    section: LessonSection,
    locale: String,
    sectionIndex: Int,
    isCompleted: Boolean,
    quizScore: Int,
    interactedBlocks: MutableMap<Pair<Int, Int>, Boolean>,
    onSectionComplete: () -> Unit,
    onQuizScore: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Section header (tap to collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    localized(locale, section.titleDe, section.titleEn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle section",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isCompleted) {
                    Spacer(Modifier.width(8.dp))
                    Text("✅", fontSize = 18.sp)
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                // Render each block
                section.blocks.forEachIndexed { blockIdx, block ->
                    ContentBlockRenderer(
                        block = block,
                        locale = locale,
                        sectionIndex = sectionIndex,
                        blockIndex = blockIdx,
                        onInteracted = {
                            interactedBlocks[Pair(sectionIndex, blockIdx)] = true
                            // Mark section as complete if all blocks interacted
                            val totalBlocks = section.blocks.size
                            val interacted = section.blocks.indices.count { i ->
                                interactedBlocks[Pair(sectionIndex, i)] == true
                            }
                            if (interacted >= totalBlocks) onSectionComplete()
                        },
                        onQuizScore = onQuizScore,
                    )
                    if (blockIdx < section.blocks.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// ── Block renderer ────────────────────────────────────────────────────────

@Composable
private fun ContentBlockRenderer(
    block: ContentBlock,
    locale: String,
    sectionIndex: Int,
    blockIndex: Int,
    onInteracted: () -> Unit,
    onQuizScore: (Int) -> Unit,
) {
    when (block) {
        is ContentBlock.Text -> TextBlock(block, locale)
        is ContentBlock.Callout -> CalloutBlock(block, locale, onInteracted)
        is ContentBlock.KnowledgeCheck -> KnowledgeCheckBlock(block, locale)
        is ContentBlock.Classification -> ClassificationBlock(block, locale, onInteracted)
        is ContentBlock.Quiz -> QuizBlock(block, locale, onQuizScore)
        is ContentBlock.FillBlank -> FillBlankBlock(block, locale, onInteracted)
        is ContentBlock.TrueFalse -> TrueFalseBlock(block, locale, onInteracted)
        is ContentBlock.RiskThermometer -> RiskThermometerBlock(
            locale = locale,
            onInteracted = onInteracted,
        )
    }
}

// ── Text block ─────────────────────────────────────────────────────────────

@Composable
private fun TextBlock(block: ContentBlock.Text, locale: String) {
    val text = localized(locale, block.textDe, block.textEn)
    val config = LocalConfiguration.current

    Text(
        text = renderSimpleMarkdown(text),
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun renderSimpleMarkdown(text: String): String =
    text
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("`(.+?)`"), "$1")
        .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")

// ── Callout block ───────────────────────────────────────────────────────────

@Composable
private fun CalloutBlock(
    block: ContentBlock.Callout,
    locale: String,
    onInteracted: () -> Unit,
) {
    val containerColor = when (block.type) {
        CalloutType.TIP -> MaterialTheme.colorScheme.primaryContainer
        CalloutType.WARNING -> MaterialTheme.colorScheme.errorContainer
        CalloutType.EXAMPLE -> MaterialTheme.colorScheme.tertiaryContainer
        CalloutType.DEFINITION -> MaterialTheme.colorScheme.secondaryContainer
        CalloutType.LAW -> MaterialTheme.colorScheme.primaryContainer
    }
    val icon = when (block.type) {
        CalloutType.TIP -> "💡"
        CalloutType.WARNING -> "⚠️"
        CalloutType.EXAMPLE -> "📋"
        CalloutType.DEFINITION -> "📖"
        CalloutType.LAW -> "⚖️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteracted() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "$icon ${localized(locale, block.textDe, block.textEn)}",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
            )
        }
    }
}

// ── Knowledge check block ───────────────────────────────────────────────────

@Composable
private fun KnowledgeCheckBlock(block: ContentBlock.KnowledgeCheck, locale: String) {
    var revealed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                localized(locale, block.questionDe, block.questionEn),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (revealed) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    localized(locale, block.answerDe, block.answerEn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { revealed = true }) {
                    Text(
                        if (locale == "en") "Tap to reveal answer" else "Antwort anzeigen",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

// ── Classification block ────────────────────────────────────────────────────

@Composable
private fun ClassificationBlock(
    block: ContentBlock.Classification,
    locale: String,
    onInteracted: () -> Unit,
) {
    // All items flattened with state tracking
    val allItems = remember {
        block.categories.flatMap { cat ->
            cat.items.map { item -> Triple(cat, item, false) }
        }.toMutableStateList()
    }

    Text(
        localized(locale, block.instructionDe, block.instructionEn),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp),
    )

    // Categories as drop targets
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        block.categories.forEach { cat ->
            val catColor = if (cat.emoji == "🤖")
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.tertiaryContainer

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = catColor),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "${cat.emoji} ${localized(locale, cat.nameDe, cat.nameEn)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    // Show items assigned to this category
                    cat.items.forEach { item ->
                        Text(
                            "  ✅ ${localized(locale, item.textDe, item.textEn)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onInteracted) {
        Text(
            if (locale == "en") "✓ Got it" else "✓ Verstanden",
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Quiz block (multiple choice) ───────────────────────────────────────────

@Composable
private fun QuizBlock(
    block: ContentBlock.Quiz,
    locale: String,
    onQuizScore: (Int) -> Unit,
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    val answered = selectedIndex != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "📝 ${localized(locale, block.questionDe, block.questionEn)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            block.options.forEachIndexed { idx, opt ->
                val isSelected = selectedIndex == idx
                val isCorrect = opt.isCorrect
                val bgColor = when {
                    showFeedback && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    showFeedback && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    showFeedback && isCorrect -> MaterialTheme.colorScheme.primary
                    showFeedback && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !answered) {
                            selectedIndex = idx
                            showFeedback = true
                        },
                    color = bgColor,
                    border = BorderStroke(1.dp, borderColor),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Letter badge
                        Text(
                            "${('A' + idx)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .wrapContentSize(Alignment.Center),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            localized(locale, opt.textDe, opt.textEn),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Feedback
            if (showFeedback && answered) {
                Spacer(Modifier.height(4.dp))
                val correct = block.options[selectedIndex!!].isCorrect
                if (correct) {
                    Text(
                        "✅ ${localized(locale, block.explanationDe, block.explanationEn)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    val correctAnswer = block.options.indexOfFirst { it.isCorrect }
                    Text(
                        "❌ ${localized(locale, block.explanationDe, block.explanationEn)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(4.dp))
                onQuizScore(if (correct) 100 else 0)
            }
        }
    }
}

// ── Fill-in-the-blank block ─────────────────────────────────────────────────

@Composable
private fun FillBlankBlock(
    block: ContentBlock.FillBlank,
    locale: String,
    onInteracted: () -> Unit,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "🔤 ${localized(locale, block.sentenceDe, block.sentenceEn).replace("___", "______")}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))

            // Choices as chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                block.choices.forEachIndexed { idx, choice ->
                    val isSelected = selected == idx
                    val isCorrect = idx == block.correctIndex
                    val containerColor = when {
                        showFeedback && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        showFeedback && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (selected == null) {
                                selected = idx
                                showFeedback = true
                                onInteracted()
                            }
                        },
                        label = { Text(choice) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = containerColor),
                    )
                }
            }

            if (showFeedback) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (selected == block.correctIndex) "✅ ${localized(locale, block.explanationDe, block.explanationEn)}"
                    else "❌ ${localized(locale, block.explanationDe, block.explanationEn)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected == block.correctIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// ── True/False block ──────────────────────────────────────────────────────

@Composable
private fun TrueFalseBlock(
    block: ContentBlock.TrueFalse,
    locale: String,
    onInteracted: () -> Unit,
) {
    var answered by remember { mutableStateOf<Boolean?>(null) }
    var showFeedback by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "❓ ${localized(locale, block.statementDe, block.statementEn)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // True button
                val trueColor = when {
                    showFeedback && block.isTrue -> MaterialTheme.colorScheme.primaryContainer
                    showFeedback && answered == true && !block.isTrue -> MaterialTheme.colorScheme.errorContainer
                    answered == true -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                OutlinedButton(
                    onClick = {
                        answered = true
                        showFeedback = true
                        onInteracted()
                    },
                    enabled = answered == null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = trueColor),
                ) {
                    Text(if (locale == "en") "✅ True" else "✅ Wahr")
                }

                // False button
                val falseColor = when {
                    showFeedback && !block.isTrue -> MaterialTheme.colorScheme.primaryContainer
                    showFeedback && answered == false && block.isTrue -> MaterialTheme.colorScheme.errorContainer
                    answered == false -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                OutlinedButton(
                    onClick = {
                        answered = false
                        showFeedback = true
                        onInteracted()
                    },
                    enabled = answered == null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = falseColor),
                ) {
                    Text(if (locale == "en") "❌ False" else "❌ Falsch")
                }
            }

            if (showFeedback) {
                Spacer(Modifier.height(8.dp))
                val correct = answered == block.isTrue
                Text(
                    if (correct) "✅ ${localized(locale, block.explanationDe, block.explanationEn)}"
                    else "❌ ${localized(locale, block.explanationDe, block.explanationEn)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
