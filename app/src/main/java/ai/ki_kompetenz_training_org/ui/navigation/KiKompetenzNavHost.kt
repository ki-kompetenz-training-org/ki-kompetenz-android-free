package ai.ki_kompetenz_training_org.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ai.ki_kompetenz_training_org.ui.auth.AuthScreen
import ai.ki_kompetenz_training_org.ui.forkids.KidsMenuScreen
import ai.ki_kompetenz_training_org.ui.forkids.KidsLessonScreen
import ai.ki_kompetenz_training_org.ui.forseniors.SeniorsMenuScreen
import ai.ki_kompetenz_training_org.ui.forseniors.SeniorsLessonScreen
import ai.ki_kompetenz_training_org.ui.gamification.GamificationScreen
import ai.ki_kompetenz_training_org.ui.home.HomeScreen
import ai.ki_kompetenz_training_org.ui.lessons.LessonDetailScreen
import ai.ki_kompetenz_training_org.ui.lessons.LessonsScreen
import ai.ki_kompetenz_training_org.ui.lessons.InteractiveLessonScreen
import ai.ki_kompetenz_training_org.data.lessons.Lesson1
import ai.ki_kompetenz_training_org.data.lessons.Lesson2
import ai.ki_kompetenz_training_org.data.lessons.Lesson3
import ai.ki_kompetenz_training_org.data.lessons.Lesson4
import ai.ki_kompetenz_training_org.data.lessons.Lesson5
import ai.ki_kompetenz_training_org.data.lessons.Lesson6
import ai.ki_kompetenz_training_org.data.lessons.Lesson7
import ai.ki_kompetenz_training_org.data.lessons.Lesson8
import ai.ki_kompetenz_training_org.data.lessons.Lesson9
import ai.ki_kompetenz_training_org.data.lessons.Lesson10
import ai.ki_kompetenz_training_org.data.lessons.Lesson11
import ai.ki_kompetenz_training_org.data.lessons.Lesson12
import ai.ki_kompetenz_training_org.data.lessons.Lesson13
import ai.ki_kompetenz_training_org.data.lessons.Lesson14
import ai.ki_kompetenz_training_org.ui.minigames.MiniGameScreen
import ai.ki_kompetenz_training_org.ui.minigames.MiniGamesMenuScreen
import ai.ki_kompetenz_training_org.ui.onboarding.OnboardingScreen
import ai.ki_kompetenz_training_org.ui.premium.PremiumScreen
import ai.ki_kompetenz_training_org.ui.quiz.QuizScreen
import ai.ki_kompetenz_training_org.ui.srs.SrsScreen
import ai.ki_kompetenz_training_org.ui.team.TeamScreen

import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGames

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val LESSONS = "lessons"
    const val LESSON = "lesson/{slug}"
    const val PREMIUM = "premium"
    const val TEAM = "team"
    const val AUTH = "auth"
    const val MINIGAMES = "minigames"
    const val MINIGAME = "minigame/{gameId}"
    const val GAMIFICATION = "gamification"
    const val SRS = "srs"
    const val FOR_KIDS = "forkids"
    const val FOR_KIDS_LESSON = "forkids/{lessonId}"
    const val FOR_SENIORS = "forseniors"
    const val FOR_SENIORS_LESSON = "forseniors/{lessonId}"

    fun lesson(slug: String) = "lesson/$slug"
    fun minigame(gameId: String) = "minigame/$gameId"
    fun kidsLesson(lessonId: String) = "forkids/$lessonId"
    fun seniorsLesson(lessonId: String) = "forseniors/$lessonId"
}

@Composable
fun KiKompetenzNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenQuiz = { navController.navigate(Routes.QUIZ) },
                onOpenLessons = { navController.navigate(Routes.LESSONS) },
                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                onOpenTeam = { navController.navigate(Routes.TEAM) },
                onLogin = { navController.navigate(Routes.AUTH) },
                onOpenMiniGames = { navController.navigate(Routes.MINIGAMES) },
                onOpenGamification = { navController.navigate(Routes.GAMIFICATION) },
                onOpenSrs = { navController.navigate(Routes.SRS) },
                onOpenForKids = { navController.navigate(Routes.FOR_KIDS) },
                onOpenForSeniors = { navController.navigate(Routes.FOR_SENIORS) },
            )
        }
        composable(Routes.QUIZ) {
            QuizScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.LESSONS) {
            LessonsScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { slug -> navController.navigate(Routes.lesson(slug)) },
                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
            )
        }
        composable(Routes.LESSON) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            // Interactive lessons (1-8) use rich content blocks
            val interactiveLessons = mapOf(
                "lesson-1" to Lesson1.lesson,
                "lesson-2" to Lesson2.lesson,
                "lesson-3" to Lesson3.lesson,
                "lesson-4" to Lesson4.lesson,
                "lesson-5" to Lesson5.lesson,
                "lesson-6" to Lesson6.lesson,
                "lesson-7" to Lesson7.lesson,
                "lesson-8" to Lesson8.lesson,
                "lesson-9" to Lesson9.lesson,
                "lesson-10" to Lesson10.lesson,
                "lesson-11" to Lesson11.lesson,
                "lesson-12" to Lesson12.lesson,
                "lesson-13" to Lesson13.lesson,
                "lesson-14" to Lesson14.lesson,
            )
            val interactiveLesson = interactiveLessons[slug]
            if (interactiveLesson != null) {
                InteractiveLessonScreen(
                    lesson = interactiveLesson,
                    onBack = { navController.popBackStack() },
                    onMarkCompleted = { navController.popBackStack() },
                )
            } else {
                LessonDetailScreen(
                    slug = slug,
                    onBack = { navController.popBackStack() },
                    onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                )
            }
        }
        composable(Routes.PREMIUM) {
            PremiumScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TEAM) {
            TeamScreen(
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Routes.AUTH) },
            )
        }
        composable(Routes.AUTH) {
            AuthScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MINIGAMES) {
            MiniGamesMenuScreen(
                onBack = { navController.popBackStack() },
                onOpenGame = { game -> navController.navigate(Routes.minigame(game.id)) },
                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
            )
        }
        composable(Routes.MINIGAME) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val game = MiniGames.byId(gameId)
            if (game != null) {
                MiniGameScreen(game = game, onBack = { navController.popBackStack() })
            } else {
                navController.popBackStack()
            }
        }
        composable(Routes.GAMIFICATION) {
            GamificationScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SRS) {
            SrsScreen(
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Routes.AUTH) },
            )
        }
        composable(Routes.FOR_KIDS) {
            KidsMenuScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { lesson -> navController.navigate(Routes.kidsLesson(lesson.id)) },
            )
        }
        composable(Routes.FOR_KIDS_LESSON) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val lesson = ai.ki_kompetenz_training_org.data.forkids.KidsLessons.all.firstOrNull { it.id == lessonId }
            if (lesson != null) {
                KidsLessonScreen(lesson = lesson, onBack = { navController.popBackStack() })
            } else {
                navController.popBackStack()
            }
        }
        composable(Routes.FOR_SENIORS) {
            SeniorsMenuScreen(
                onBack = { navController.popBackStack() },
                onOpenLesson = { lesson -> navController.navigate(Routes.seniorsLesson(lesson.id)) },
            )
        }
        composable(Routes.FOR_SENIORS_LESSON) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val lesson = ai.ki_kompetenz_training_org.data.forseniors.SeniorsLessons.all.firstOrNull { it.id == lessonId }
            if (lesson != null) {
                SeniorsLessonScreen(lesson = lesson, onBack = { navController.popBackStack() })
            } else {
                navController.popBackStack()
            }
        }
    }
}