package ai.ki_kompetenz_training_org.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import ai.ki_kompetenz_training_org.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.api.QuizQuestionDto
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.ui.common.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonDetailUiState(
    val lesson: LessonDetailDto? = null,
    val cachedBody: String? = null,
    val loading: Boolean = true,
    val error: UiError? = null,
    val isTestPassed: Boolean = false,  // Must pass test to complete
    val currentScore: Int = 0,
    val quizQuestions: List<QuizQuestionDto> = emptyList(),
    val showQuiz: Boolean = false,
)

class LessonDetailViewModel(
    private val slug: String,
    private val contentRepository: ContentRepository,
    private val premiumRepository: PremiumRepository,
    private val gamificationRepository: ai.ki_kompetenz_training_org.data.repo.GamificationRepository,
    private val coroutineDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(LessonDetailUiState())
    val state: StateFlow<LessonDetailUiState> = _state

    // Minimum score to pass the test (60%)
    private val passThreshold = 60

    // Map of lesson slugs to quiz questions (could be moved to API/database later)
    private val lessonQuizzes: Map<String, List<QuizQuestionDto>> = mapOf(
        "lesson-1" to listOf(
            QuizQuestionDto(
                id = "q1",
                question = "Was ist der Hauptunterschied zwischen klassischer Software und KI?",
                options = listOf(
                    "KI kann selbstständig aus Daten lernen",
                    "KI ist immer schneller",
                    "KI benötigt kein Internet",
                    "KI ist nur für große Unternehmen"
                ),
                correctAnswerIndex = 0,
                explanation = "KI-Systeme verbessern sich durch Erfahrung mit Daten, während klassische Software statische Regeln folgt.",
                points = 25
            ),
            QuizQuestionDto(
                id = "q2",
                question = "Welches ist KEIN Anwendungsfall von KI?",
                options = listOf(
                    "Spam-Erkennung in E-Mails",
                    "Personalisierte Produktempfehlungen",
                    "Automatische Übersetzung von Sprachen",
                    "Manuelles Sortieren von Dokumenten"
                ),
                correctAnswerIndex = 3,
                explanation = "Manuelles Sortieren erfordert menschliche Intelligenz, während die anderen Optionen alle durch KI automatisiert werden können.",
                points = 25
            ),
            QuizQuestionDto(
                id = "q3",
                question = "Welche Art von Daten wird für maschinelles Lernen typischerweise verwendet?",
                options = listOf(
                    "Unstrukturierte Rohdaten",
                    "Structureierte Daten mit Mustern",
                    "Zufällige Zahlen",
                    "Leere Tabellen"
                ),
                correctAnswerIndex = 1,
                explanation = "Maschinelles Lernen funktioniert am besten mit strukturierten Daten, die klare Muster und Beziehungen aufweisen.",
                points = 50
            )
        ),
        // Add more lesson quizzes here...
        "lesson-2" to listOf(
            QuizQuestionDto(
                id = "q1",
                question = "Welche der folgenden ist KEINE Hauptart von KI?",
                options = listOf(
                    "Schmale KI (Narrow AI)",
                    "Allgemeine KI (General AI)",
                    "Superintelligente KI",
                    "Passive KI"
                ),
                correctAnswerIndex = 3,
                explanation = "Passive KI ist kein standardmäßiger Begriff. Die drei Hauptkategorien sind Narrow AI, General AI und Superintelligent AI.",
                points = 33
            ),
            QuizQuestionDto(
                id = "q2",
                question = "Welche Art von KI wird heute am häufigsten in Unternehmen eingesetzt?",
                options = listOf(
                    "Schmale KI (Narrow AI)",
                    "Allgemeine KI (General AI)",
                    "Superintelligente KI",
                    "Alle gleich häufig"
                ),
                correctAnswerIndex = 0,
                explanation = "Schmale KI (Narrow AI) ist die am weitesten verbreitete Form, da sie für spezifische Aufgaben optimiert ist.",
                points = 33
            ),
            QuizQuestionDto(
                id = "q3",
                question = "Was kann General AI im Gegensatz zu Narrow AI?",
                options = listOf(
                    "Eine spezifische Aufgabe ausführen",
                    "Jede intellektuelle Aufgabe ausführen, die ein Mensch kann",
                    "Nur mathematische Berechnungen durchführen",
                    "Nur Bilder erkennen"
                ),
                correctAnswerIndex = 1,
                explanation = "General AI (Allgemeine KI) hat die Fähigkeit, jede intellektuelle Aufgabe zu bewältigen, für die ein Mensch fähig ist.",
                points = 34
            )
        ),
        // Lektion 13: Wirtschaftliche KI-Nutzung
        "lesson-13" to listOf(
            QuizQuestionDto(
                id = "q1",
                question = "Was ist die wirtschaftlichste Strategie für KI-Einsatz?",
                options = listOf(
                    "Nur LLMs für alle Aufgaben verwenden",
                    "Nur Workflows für alle Aufgaben verwenden",
                    "Hybrid: LLM generiert, Workflow skaliert",
                    "Gar keine KI verwenden"
                ),
                correctAnswerIndex = 2,
                explanation = "Die wirtschaftlichste Strategie ist Hybrid: Das LLM generiert kreative Lösungen, der Workflow validiert und skaliert sie zuverlässig.",
                points = 34
            ),
            QuizQuestionDto(
                id = "q2",
                question = "Was besagt die 80/20-Regel für KI-Automatisierung?",
                options = listOf(
                    "80% der Kosten kommen aus 20% der API-Calls",
                    "80% der Effizienz kommt aus 20% der Automatisierung",
                    "80% der Fehler kommen aus 20% des Codes",
                    "80% der Nutzer verwenden nur 20% der Funktionen"
                ),
                correctAnswerIndex = 1,
                explanation = "Die Pareto-Regel: 80% der Effizienzsteigerung kommt aus 20% der Automatisierung — den häufigsten, repetitivsten Aufgaben.",
                points = 33
            ),
            QuizQuestionDto(
                id = "q3",
                question = "Welches ist KEIN Anti-Pattern bei der KI-Nutzung?",
                options = listOf(
                    "Alles mit LLM lösen, auch deterministische Aufgaben",
                    "LLM-Ausgaben ungeprüft in Produktion geben",
                    "Workflow ohne Skalierung (manuell)",
                    "Hybrid: LLM generiert, Workflow validiert"
                ),
                correctAnswerIndex = 3,
                explanation = "Hybrid (LLM generiert, Workflow validiert) ist die BESTE Praxis, kein Anti-Pattern. Die anderen drei sind Anti-Patterns.",
                points = 33
            )
        )
    )

    init {
        viewModelScope.launch(coroutineDispatcher) {
            contentRepository.fetchLesson(slug, currentLang()).onSuccess { lesson ->
                val quizQuestions = lessonQuizzes[slug] ?: emptyList()
                _state.value = LessonDetailUiState(
                    lesson = lesson,
                    loading = false,
                    quizQuestions = quizQuestions
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    error = UiError.LESSON_LOAD,
                )
            }
        }
    }

    /**
     * Submit an answer to a quiz question.
     */
    fun submitAnswer(questionId: String, selectedOptionIndex: Int) {
        val currentState = _state.value
        val question = currentState.quizQuestions.find { it.id == questionId } ?: return

        val isCorrect = (selectedOptionIndex == question.correctAnswerIndex)
        val points = if (isCorrect) question.points else 0

        val currentScore = currentState.currentScore + points
        val totalPoints = currentState.quizQuestions.sumOf { it.points }

        // Check if test is passed
        val isTestPassed = (currentScore * 100 / totalPoints) >= passThreshold

        _state.value = currentState.copy(
            currentScore = currentScore,
            isTestPassed = isTestPassed
        )
    }

    /**
     * Start the quiz for this lesson.
     */
    fun startQuiz() {
        _state.value = _state.value.copy(
            showQuiz = true,
            currentScore = 0,
            isTestPassed = false
        )
    }

    /**
     * Mark lesson as completed (only allowed if test is passed).
     */
    fun markCompleted() {
        val currentState = _state.value
        if (!currentState.isTestPassed) {
            // Cannot complete without passing the test
            return
        }
        viewModelScope.launch {
            gamificationRepository.markLessonCompleted(slug)
        }
    }

    /**
     * Check if the lesson can be completed (test passed).
     */
    fun canCompleteLesson(): Boolean {
        return _state.value.isTestPassed
    }

    fun isPremium(): Boolean =
        premiumRepository.isPremiumLesson(_state.value.lesson?.lesson)
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun LessonDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onOpenPremium: () -> Unit,
) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val vm: LessonDetailViewModel = viewModel(key = slug) {
        LessonDetailViewModel(slug, app.contentRepository, app.premiumRepository, app.gamificationRepository)
    }
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.lesson?.title ?: "Lektion") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.lessons_back))
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(ai.ki_kompetenz_training_org.ui.common.uiErrorMessage(state.error!!))
            }
            state.lesson != null -> {
                if (vm.isPremium()) {
                    PremiumGate(
                        modifier = Modifier.padding(padding),
                        lessonNumber = state.lesson!!.lesson,
                        onOpenPremium = onOpenPremium,
                    )
                } else {
                    LessonBody(
                        modifier = Modifier.padding(padding),
                        lesson = state.lesson!!,
                        quizQuestions = state.quizQuestions,
                        isTestPassed = state.isTestPassed,
                        currentScore = state.currentScore,
                        showQuiz = state.showQuiz,
                        onStartQuiz = vm::startQuiz,
                        onSubmitAnswer = vm::submitAnswer,
                        onMarkCompleted = vm::markCompleted,
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonBody(
    modifier: Modifier,
    lesson: LessonDetailDto,
    quizQuestions: List<QuizQuestionDto>,
    isTestPassed: Boolean,
    currentScore: Int,
    showQuiz: Boolean,
    onStartQuiz: () -> Unit,
    onSubmitAnswer: (String, Int) -> Unit,
    onMarkCompleted: () -> Unit,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        // Lesson header
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            lesson.duration ?: "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            lesson.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        
        // Lesson content
        Text(
            renderMarkdown(lesson.body),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(20.dp))

        // Test section
        if (quizQuestions.isNotEmpty()) {
            QuizSection(
                questions = quizQuestions,
                isTestPassed = isTestPassed,
                currentScore = currentScore,
                showQuiz = showQuiz,
                onStartQuiz = onStartQuiz,
                onSubmitAnswer = onSubmitAnswer
            )
            Spacer(Modifier.height(16.dp))
        }

        // Complete lesson button (only enabled if test passed)
        Button(
            onClick = onMarkCompleted,
            enabled = isTestPassed,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            if (isTestPassed) {
                Text(
                    stringResource(R.string.lesson_mark_complete, ai.ki_kompetenz_training_org.data.repo.GamificationRules.xpPerCompletedLesson),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(stringResource(R.string.lesson_test_to_complete), fontWeight = FontWeight.Bold)
            }
        }
        
        if (!isTestPassed && quizQuestions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.lesson_quiz_tip),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QuizSection(
    questions: List<QuizQuestionDto>,
    isTestPassed: Boolean,
    currentScore: Int,
    showQuiz: Boolean,
    onStartQuiz: () -> Unit,
    onSubmitAnswer: (String, Int) -> Unit,
) {
    val totalPoints = questions.sumOf { it.points }
    val scorePercentage = if (totalPoints > 0) (currentScore * 100 / totalPoints) else 0

    Column {
        // Quiz header
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📝 Mini-Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.lesson_quiz_intro, questions.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!showQuiz) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onStartQuiz,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test starten", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Show quiz progress
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { scorePercentage / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.lesson_score_points, currentScore.toString(), totalPoints.toString(), scorePercentage.toString()),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (isTestPassed) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.lesson_test_passed),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // Show quiz questions
        if (showQuiz) {
            Spacer(Modifier.height(12.dp))
            questions.forEachIndexed { index, question ->
                QuizQuestionCard(
                    question = question,
                    questionNumber = index + 1,
                    onSubmitAnswer = { selectedIndex ->
                        onSubmitAnswer(question.id, selectedIndex)
                    }
                )
                if (index < questions.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionCard(
    question: QuizQuestionDto,
    questionNumber: Int,
    onSubmitAnswer: (Int) -> Unit,
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.lesson_question_n, questionNumber, question.question),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            
            // Options
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val isCorrect = selectedOption != null && index == question.correctAnswerIndex
                val isWrong = selectedOption != null && isSelected && !isCorrect

                val backgroundColor = when {
                    isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    isWrong -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            selectedOption = index
                            showFeedback = true
                            onSubmitAnswer(index)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = backgroundColor,
                            contentColor = when {
                                isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                                isWrong -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    ) {
                        Text(option, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                    
                    // Show feedback for correct/wrong answers
                    if (showFeedback && isCorrect) {
                        Text(
                            stringResource(R.string.quiz_correct) + (question.explanation ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    } else if (showFeedback && isWrong) {
                        Text(
                            stringResource(R.string.lesson_wrong_prefix) + (question.explanation ?: stringResource(R.string.lesson_correct_answer_is, question.options[question.correctAnswerIndex])),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Minimal Markdown → readable text (headers, bullets, bold). */
internal fun renderMarkdown(markdown: String): String {
    val lines = markdown.lines()
    val sb = StringBuilder()
    lines.forEach { raw ->
        val line = raw.trimEnd()
        when {
            line.startsWith("### ") -> { sb.append("\n").append(line.removePrefix("### ").trim()).append("\n") }
            line.startsWith("## ") -> { sb.append("\n").append(line.removePrefix("## ").trim()).append("\n") }
            line.startsWith("# ") -> { sb.append("\n").append(line.removePrefix("# ").trim()).append("\n") }
            line.startsWith("- ") || line.startsWith("* ") -> {
                sb.append("• ").append(stripInlineMd(line.removePrefix("- ").removePrefix("* ").trim())).append("\n")
            }
            line.startsWith("|") && line.contains("---") -> { /* table separator */ }
            line.startsWith("|") -> {
                sb.append(line.trim('|').split("|").joinToString(" · ").trim()).append("\n")
            }
            line.startsWith("```") -> { /* code fence */ }
            line.isBlank() -> sb.append("\n")
            else -> {
                sb.append(stripInlineMd(line)).append("\n")
            }
        }
    }
    return sb.toString().trim()
}

private fun stripInlineMd(line: String): String =
    line
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("`(.+?)`"), "$1")
        .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PremiumGate(modifier: Modifier, lessonNumber: Int?, onOpenPremium: () -> Unit) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.lesson_premium_gate_title), Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.lesson_premium_gate_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.common_premium_gate_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onOpenPremium,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
        ) {
            Icon(Icons.Default.Star, contentDescription = stringResource(R.string.lesson_premium_cta))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.lesson_premium_cta), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.premium_price_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}