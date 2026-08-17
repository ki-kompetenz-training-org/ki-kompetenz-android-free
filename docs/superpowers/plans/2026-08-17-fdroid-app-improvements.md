# F-Droid App Improvements — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform KI Quiz Free Edition from a flat list-of-cards app into a delightful, bottom-nav-driven experience with a 3D KiBot companion, proper i18n, error states, and accessibility.

**Architecture:** Persistent `NavigationBar` with 4 tabs (Home, Games, Learn, Profile). Home screen centered on KiBot — a procedurally-drawn 3D robot companion that grows with the user. All hardcoded strings extracted to resources. Connectivity observer for error states. Language toggle via DataStore.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Room, DataStore, sceneview (3D), Compose Canvas (particles/2D), Android Sensors (parallax), ConnectivityManager.

---

## File Map

### New Files
| File | Responsibility |
|------|---------------|
| `ui/navigation/BottomNavScreen.kt` | Main scaffold: TopAppBar + NavigationBar, routes to 4 tab composables |
| `ui/kibot/KiBotState.kt` | Growth stage enum, emotional state enum, KiBotState data class |
| `ui/kibot/KiBotModel.kt` | Procedural 2D robot drawn with Compose Canvas (body, head, eyes, antenna per stage) |
| `ui/kibot/KiBotScene.kt` | Orchestrator: KiBotModel + particles + XP ring + tap handler + parallax |
| `ui/kibot/ParticleSystem.kt` | Reusable Canvas particle animation (sparkles, confetti, "?", Zzz) |
| `ui/kibot/ProgressRing.kt` | Animated gradient arc showing XP progress to next level |
| `data/prefs/SettingsStore.kt` | DataStore-backed language preference (system/de/en) |
| `data/connectivity/ConnectivityObserver.kt` | Flow-based network state (online/offline) |
| `test/.../kibot/KiBotStateTest.kt` | Tests for state machine logic |
| `test/.../kibot/ParticleTest.kt` | Tests for particle system math |

### Modified Files
| File | Change |
|------|--------|
| `KiKompetenzApp.kt` | Add `settingsStore`, `connectivityObserver` to service locator |
| `MainActivity.kt` | Use `BottomNavScreen` instead of bare `KiKompetenzNavHost` |
| `KiKompetenzNavHost.kt` | Remove Scaffold from tab composables, keep Scaffolds on sub-screens |
| `HomeScreen.kt` | Full redesign: KiBot area, quick grid, slim summary, kids/seniors |
| `MiniGamesMenuScreen.kt` | Remove own Scaffold, extract ~8 hardcoded strings |
| `LessonsScreen.kt` | Remove own Scaffold |
| `GamificationScreen.kt` | Remove own Scaffold, add language toggle section |
| `QuizScreen.kt` | Extract ~15 hardcoded strings, add error state |
| `KidsScreen.kt` | Extract ~12 hardcoded strings |
| `HomeViewModel.kt` | Add lastCheckIn, offline flag, trigger KiBot state updates |
| `values/strings.xml` | Add ~40 new string entries |
| `values-en/strings.xml` | Add ~40 entries, fix 2 incorrect (premium_cta, premium_trust) |
| `res/values-de/strings.xml` | Add ~40 German string entries |

### Migrated Files
| From | To |
|------|-----|
| `test/.../de/kikompetenz/app/CoreLogicTest.kt` | `test/.../ai/ki_kompetenz_training_org/CoreLogicTest.kt` |
| `test/.../de/kikompetenz/app/GamificationRulesTest.kt` | `test/.../ai/ki_kompetenz_training_org/GamificationRulesTest.kt` |
| `test/.../de/kikompetenz/app/PropertyBasedTests.kt` | `test/.../ai/ki_kompetenz_training_org/PropertyBasedTests.kt` |
| `test/.../de/kikompetenz/app/SecurityTest.kt` | `test/.../ai/ki_kompetenz_training_org/SecurityTest.kt` |
| `test/.../de/kikompetenz/app/SrsSessionTest.kt` | `test/.../ai/ki_kompetenz_training_org/SrsSessionTest.kt` |
| `test/.../de/kikompetenz/app/api/ApiContractTest.kt` | `test/.../ai/ki_kompetenz_training_org/api/ApiContractTest.kt` |
| `test/.../de/kikompetenz/app/data/repo/ContentRepositoryTest.kt` | `test/.../ai/ki_kompetenz_training_org/data/repo/ContentRepositoryTest.kt` |
| `test/.../de/kikompetenz/app/data/repo/PremiumRepositoryTest.kt` | `test/.../ai/ki_kompetenz_training_org/data/repo/PremiumRepositoryTest.kt` |
| `test/.../de/kikompetenz/app/lesson/LessonIntegrationTest.kt` | `test/.../ai/ki_kompetenz_training_org/lesson/LessonIntegrationTest.kt` |
| `test/.../de/kikompetenz/app/ui/lessons/LessonDetailViewModelTest.kt` | `test/.../ai/ki_kompetenz_training_org/ui/lessons/LessonDetailViewModelTest.kt` |

---

## Execution Order

Tasks 1–4 are sequential (navigation → home → KiBot core → KiBot effects).
Tasks 5, 6, 10 can run **in parallel** (strings, language toggle, test migration).
Tasks 7, 8 can run **in parallel** after Task 1 (error states, accessibility).

Recommended parallel batches:
- **Batch A** (sequential core): Tasks 1 → 2 → 3 → 4
- **Batch B** (parallel, starts anytime): Tasks 5, 6, 10
- **Batch C** (after Batch A task 1): Tasks 7, 8

---

### Task 1: Bottom Navigation Scaffold

**Files:**
- Create: `ui/navigation/BottomNavScreen.kt`
- Modify: `MainActivity.kt` (swap `KiKompetenzNavHost` → `BottomNavScreen`)
- Modify: `KiKompetenzNavHost.kt` (extract inner content from tab screens)

**Context:** The current app starts at HOME with every screen having its own `Scaffold`. We need a single outer `Scaffold` with a `NavigationBar` for the 4 tab destinations. Sub-destinations (Quiz, Lesson detail, etc.) keep their own `Scaffold` with back navigation.

- [ ] **Step 1: Create BottomNavScreen.kt**

Create `app/src/main/java/ai/ki_kompetenz_training_org/ui/navigation/BottomNavScreen.kt`:
```kotlin
package ai.ki_kompetenz_training_org.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.ki_kompetenz_training_org.ui.gamification.GamificationScreen
import ai.ki_kompetenz_training_org.ui.home.HomeScreen
import ai.ki_kompetenz_training_org.ui.lessons.LessonsScreen
import ai.ki_kompetenz_training_org.ui.minigames.MiniGamesMenuScreen

data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val BOTTOM_TABS = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Default.Home),
    BottomTab(Routes.MINIGAMES, "Games", Icons.Default.SportsEsports),
    BottomTab(Routes.LESSONS, "Learn", Icons.Default.MenuBook),
    BottomTab(Routes.GAMIFICATION, "Profile", Icons.Default.Person),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if current destination is a tab (show bar) or a sub-screen (hide bar)
    val isTabDestination = BOTTOM_TABS.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            if (isTabDestination) {
                NavigationBar {
                    BOTTOM_TABS.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        KiKompetenzNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
```

- [ ] **Step 2: Update KiKompetenzNavHost to accept modifier**

Modify `KiKompetenzNavHost.kt` — add `modifier: Modifier = Modifier` parameter, pass it to `NavHost`:
```kotlin
@Composable
fun KiKompetenzNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
    ) { /* ...existing composables unchanged... */ }
}
```

- [ ] **Step 3: Remove Scaffold from 4 tab screens**

For each of these files, remove the outer `Scaffold { padding -> Column(modifier = Modifier.padding(padding)...) }` wrapper. Keep only the inner content `Column`. The tab screens are: `HomeScreen`, `MiniGamesMenuScreen`, `LessonsScreen`, `GamificationScreen`. Sub-screens (QuizScreen, LessonDetailScreen, etc.) keep their Scaffold.

In `HomeScreen.kt`: Remove `Scaffold(topBar = {...})` block. Keep the inner `Column` content. Pass padding as parameter instead.

In `MiniGamesMenuScreen.kt`: Remove `Scaffold(topBar = {...})`. Keep `LazyColumn` content.

In `LessonsScreen.kt`: Same pattern.

In `GamificationScreen.kt`: Same pattern.

- [ ] **Step 4: Update MainActivity.kt**

```kotlin
setContent {
    KiKompetenzTheme {
        BottomNavScreen()  // was: KiKompetenzNavHost()
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A && git commit -m "feat: add bottom navigation with 4 tabs (Home, Games, Learn, Profile)"
```

---

### Task 2: Slimmed Home Screen

**Files:**
- Modify: `ui/home/HomeScreen.kt` (full rewrite of content layout)
- Modify: `ui/home/HomeViewModel.kt` (add lastCheckIn for KiBot state)

**Context:** Replace the 11-card list with: KiBot container area, gamification summary bar, 2×2 quick-action grid, compact kids/seniors row, subtle premium chip.

- [ ] **Step 1: Update HomeUiState with lastCheckIn**

In `HomeViewModel.kt`, add `lastCheckInDay: String? = null` to `HomeUiState`. Populate from `gamificationRepository.observe()`:
```kotlin
lastCheckInDay = entity?.lastCheckInDay,
```

- [ ] **Step 2: Rewrite HomeScreen content**

Replace the entire content inside HomeScreen. New layout:
1. **KiBot placeholder** — `Box` with placeholder text "🤖 KiBot" (Task 3 will replace with real 3D)
2. **Gamification bar** — Row: Level, XP progress, streak flame, check-in button
3. **Quick-action grid** — `LazyVerticalGrid(2 columns)` with 4 cards: KI-Score, Mini-Games, Lessons, SRS
4. **"More" row** — Horizontal `Row` with 2 compact cards: Kids (👶), Seniors (👴)
5. **Premium chip** — `AssistChip` (existing pattern, only if not premium)
6. **DSGVO footer** — existing text

Each grid card: icon, title, subtitle, arrow — same `FeatureCard` pattern but in a grid.

- [ ] **Step 3: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. Visual: Home screen shows 4-tab bottom nav, KiBot placeholder, 2×2 grid.

- [ ] **Step 4: Commit**

```
git add -A && git commit -m "feat: slim home screen with quick-action grid, gamification summary, KiBot placeholder"
```

---

### Task 3: KiBot State Machine & 2D Model

**Files:**
- Create: `ui/kibot/KiBotState.kt`
- Create: `ui/kibot/KiBotModel.kt`
- Create: `test/java/ai/ki_kompetenz_training_org/ui/kibot/KiBotStateTest.kt`
- Modify: `ui/home/HomeScreen.kt` (wire KiBotScene into placeholder)

**Context:** KiBot is a procedurally-drawn robot using Compose `Canvas`. State derived from gamification data (level, streak, lastCheckIn). This task creates the state machine and the static 2D model drawing; Task 4 adds animations/particles.

- [ ] **Step 1: Write failing tests for KiBotState**

Create `test/java/ai/ki_kompetenz_training_org/ui/kibot/KiBotStateTest.kt`:
```kotlin
package ai.ki_kompetenz_training_org.ui.kibot

import org.junit.Test
import kotlin.test.assertEquals

class KiBotStateTest {
    @Test fun `growth stage is Neonate for levels 1-2`() {
        assertEquals(GrowthStage.NEONATE, GrowthStage.forLevel(1))
        assertEquals(GrowthStage.NEONATE, GrowthStage.forLevel(2))
    }
    @Test fun `growth stage is Learner for levels 3-5`() {
        assertEquals(GrowthStage.LEARNER, GrowthStage.forLevel(3))
        assertEquals(GrowthStage.LEARNER, GrowthStage.forLevel(5))
    }
    @Test fun `growth stage is Thinker for levels 6-9`() {
        assertEquals(GrowthStage.THINKER, GrowthStage.forLevel(6))
        assertEquals(GrowthStage.THINKER, GrowthStage.forLevel(9))
    }
    @Test fun `growth stage is Expert for levels 10-14`() {
        assertEquals(GrowthStage.EXPERT, GrowthStage.forLevel(10))
        assertEquals(GrowthStage.EXPERT, GrowthStage.forLevel(14))
    }
    @Test fun `emotional baseline is sleepy when last check-in 2+ days ago`() {
        assertEquals(EmotionalState.SLEEPY, EmotionalState.baseline(streak = 0, daysSinceCheckIn = 3, checkedInToday = false))
    }
    @Test fun `emotional baseline is idle when checked in today`() {
        assertEquals(EmotionalState.IDLE, EmotionalState.baseline(streak = 5, daysSinceCheckIn = 0, checkedInToday = true))
    }
    @Test fun `KiBotState derives from gamification data`() {
        val state = KiBotState.from(level = 4, xp = 200, xpIntoLevel = 100, xpNeeded = 300, streak = 3, daysSinceCheckIn = 0, checkedInToday = true)
        assertEquals(GrowthStage.LEARNER, state.growthStage)
        assertEquals(EmotionalState.IDLE, state.emotionalBaseline)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "ai.ki_kompetenz_training_org.ui.kibot.KiBotStateTest" -i`
Expected: FAIL (classes don't exist)

- [ ] **Step 3: Implement KiBotState.kt**

Create `ui/kibot/KiBotState.kt`:
```kotlin
package ai.ki_kompetenz_training_org.ui.kibot

enum class GrowthStage {
    NEONATE,   // Level 1-2
    LEARNER,   // Level 3-5
    THINKER,   // Level 6-9
    EXPERT,    // Level 10-14
    ;

    companion object {
        fun forLevel(level: Int): GrowthStage = when (level) {
            in 1..2 -> NEONATE
            in 3..5 -> LEARNER
            in 6..9 -> THINKER
            else -> EXPERT
        }
    }
}

enum class EmotionalState {
    IDLE,        // Default: gentle bobbing
    HAPPY,       // Check-in, correct answer
    CELEBRATING, // Streak milestone, perfect game
    CONFUSED,    // Wrong answer, API error
    SLEEPY,      // Not checked in 2+ days
    THRILLED,    // Premium unlocked, big milestone
    ;

    companion object {
        fun baseline(streak: Int, daysSinceCheckIn: Int, checkedInToday: Boolean): EmotionalState =
            when {
                daysSinceCheckIn >= 2 && !checkedInToday -> SLEEPY
                checkedInToday -> IDLE
                else -> IDLE
            }
    }
}

data class KiBotState(
    val growthStage: GrowthStage,
    val emotionalBaseline: EmotionalState,
    val xp: Int,
    val xpIntoLevel: Int,
    val xpNeeded: Int,
    val level: Int,
    val streak: Int,
    val checkedInToday: Boolean,
) {
    companion object {
        fun from(
            level: Int,
            xp: Int,
            xpIntoLevel: Int,
            xpNeeded: Int,
            streak: Int,
            daysSinceCheckIn: Int,
            checkedInToday: Boolean,
        ): KiBotState = KiBotState(
            growthStage = GrowthStage.forLevel(level),
            emotionalBaseline = EmotionalState.baseline(streak, daysSinceCheckIn, checkedInToday),
            xp = xp,
            xpIntoLevel = xpIntoLevel,
            xpNeeded = xpNeeded,
            level = level,
            streak = streak,
            checkedInToday = checkedInToday,
        )
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "ai.ki_kompetenz_training_org.ui.kibot.KiBotStateTest" -i`
Expected: ALL PASS

- [ ] **Step 5: Implement KiBotModel.kt (procedural 2D drawing)**

Create `ui/kibot/KiBotModel.kt` — draws a cute robot with Compose `Canvas`:
- **NEONATE**: Small rounded body, single antenna, simple circle eyes with glow
- **LEARNER**: Larger body, two antennae, small arms
- **THINKER**: Body + translucent dome (brain), brighter eye glow
- **EXPERT**: Full body with small boosters at bottom, orbital ring detail

Use `drawRoundRect`, `drawCircle`, `drawLine` (Canvas API). Brand colors from `Theme.kt` (`BrandPrimary = 0xFF4F46E5`).

```kotlin
package ai.ki_kompetenz_training_org.ui.kibot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun KiBotModel(
    stage: GrowthStage,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2
        val cy = size.height / 2
        // Scale factor based on stage
        val scale = when (stage) {
            GrowthStage.NEONATE -> 0.5f
            GrowthStage.LEARNER -> 0.7f
            GrowthStage.THINKER -> 0.85f
            GrowthStage.EXPERT -> 1.0f
        }
        val bodyW = 80.dp.toPx() * scale
        val bodyH = 100.dp.toPx() * scale
        val headR = 40.dp.toPx() * scale
        val eyeR = 6.dp.toPx() * scale

        // Body
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx - bodyW / 2, cy - bodyH / 4),
            size = Size(bodyW, bodyH),
            cornerRadius = 20.dp.toPx() * scale,
        )
        // Head
        drawCircle(
            color = primary,
            radius = headR,
            center = Offset(cx, cy - bodyH / 2),
        )
        // Eyes
        drawCircle(
            color = Color(0xFF22D3EE),  // cyan glow
            radius = eyeR,
            center = Offset(cx - 14.dp.toPx() * scale, cy - bodyH / 2 - 4.dp.toPx()),
        )
        drawCircle(
            color = Color(0xFF22D3EE),
            radius = eyeR,
            center = Offset(cx + 14.dp.toPx() * scale, cy - bodyH / 2 - 4.dp.toPx()),
        )

        // Stage-specific details
        when (stage) {
            GrowthStage.NEONATE -> {
                // Single antenna
                drawLine(
                    color = primary,
                    start = Offset(cx, cy - bodyH / 2 - headR),
                    end = Offset(cx, cy - bodyH / 2 - headR - 20.dp.toPx() * scale),
                    strokeWidth = 3.dp.toPx() * scale,
                )
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 5.dp.toPx() * scale,
                    center = Offset(cx, cy - bodyH / 2 - headR - 24.dp.toPx() * scale),
                )
            }
            GrowthStage.LEARNER -> {
                // Two antennae + arms
                drawLine(color = primary, start = Offset(cx - 10.dp.toPx(), cy - bodyH / 2 - headR), end = Offset(cx - 15.dp.toPx(), cy - bodyH / 2 - headR - 22.dp.toPx() * scale), strokeWidth = 2.dp.toPx())
                drawCircle(color = Color(0xFFFBBF24), radius = 4.dp.toPx(), center = Offset(cx - 15.dp.toPx(), cy - bodyH / 2 - headR - 25.dp.toPx() * scale))
                drawLine(color = primary, start = Offset(cx + 10.dp.toPx(), cy - bodyH / 2 - headR), end = Offset(cx + 15.dp.toPx(), cy - bodyH / 2 - headR - 22.dp.toPx() * scale), strokeWidth = 2.dp.toPx())
                drawCircle(color = Color(0xFFFBBF24), radius = 4.dp.toPx(), center = Offset(cx + 15.dp.toPx(), cy - bodyH / 2 - headR - 25.dp.toPx() * scale))
                // Small arms
                drawRoundRect(color = surface, topLeft = Offset(cx - bodyW / 2 - 12.dp.toPx() * scale, cy), size = Size(10.dp.toPx() * scale, 30.dp.toPx() * scale), cornerRadius = 4.dp.toPx())
                drawRoundRect(color = surface, topLeft = Offset(cx + bodyW / 2 + 2.dp.toPx() * scale, cy), size = Size(10.dp.toPx() * scale, 30.dp.toPx() * scale), cornerRadius = 4.dp.toPx())
            }
            GrowthStage.THINKER -> {
                // Brain dome
                drawCircle(color = primary.copy(alpha = 0.3f), radius = headR * 1.15f, center = Offset(cx, cy - bodyH / 2), style = Stroke(2.dp.toPx() * scale))
                // Brighter eye glow
                drawCircle(color = Color(0xFF22D3EE).copy(alpha = 0.3f), radius = eyeR * 2, center = Offset(cx - 14.dp.toPx() * scale, cy - bodyH / 2 - 4.dp.toPx()))
                drawCircle(color = Color(0xFF22D3EE).copy(alpha = 0.3f), radius = eyeR * 2, center = Offset(cx + 14.dp.toPx() * scale, cy - bodyH / 2 - 4.dp.toPx()))
            }
            GrowthStage.EXPERT -> {
                // Boosters
                drawRoundRect(color = Color(0xFFF97316), topLeft = Offset(cx - bodyW / 3 - 6.dp.toPx(), cy + bodyH / 2 - 5.dp.toPx()), size = Size(12.dp.toPx(), 20.dp.toPx()), cornerRadius = 3.dp.toPx())
                drawRoundRect(color = Color(0xFFF97316), topLeft = Offset(cx + bodyW / 3 - 6.dp.toPx(), cy + bodyH / 2 - 5.dp.toPx()), size = Size(12.dp.toPx(), 20.dp.toPx()), cornerRadius = 3.dp.toPx())
                // Orbital ring
                drawCircle(color = primary.copy(alpha = 0.2f), radius = bodyW * 0.7f, center = Offset(cx, cy), style = Stroke(2.dp.toPx()))
            }
        }
    }
}
```

- [ ] **Step 6: Wire KiBotModel into HomeScreen placeholder**

In `HomeScreen.kt`, replace the `Box` placeholder with:
```kotlin
KiBotModel(
    stage = state.kiBotState.growthStage,
    modifier = Modifier
        .size(180.dp)
        .clip(RoundedCornerShape(16.dp)),
)
```
Add `kiBotState` to `HomeUiState`, compute it in `HomeViewModel`.

- [ ] **Step 7: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. KiBot robot visible on home screen.

- [ ] **Step 8: Commit**

```
git add -A && git commit -m "feat: KiBot state machine and procedural 2D robot model (4 growth stages)"
```

---

### Task 4: KiBot Animations, Particles & Parallax

**Files:**
- Create: `ui/kibot/ParticleSystem.kt`
- Create: `ui/kibot/ProgressRing.kt`
- Create: `ui/kibot/KiBotScene.kt`
- Create: `test/java/ai/ki_kompetenz_training_org/ui/kibot/ParticleTest.kt`
- Modify: `ui/home/HomeScreen.kt` (swap `KiBotModel` → `KiBotScene`)

**Context:** Add life to KiBot: idle bobbing animation, particle effects for emotions, XP progress ring, and subtle tilt parallax. All done with Compose Canvas/graphics — no new dependencies.

- [ ] **Step 1: Implement ProgressRing.kt**

Create `ui/kibot/ProgressRing.kt` — animated arc showing XP progress:
```kotlin
@Composable
fun ProgressRing(
    progress: Float,  // 0..1
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "xpProgress",
    )
    Canvas(modifier = modifier) {
        val stroke = strokeWidth.toPx()
        val arcDimen = size.minDimension - stroke
        val topLeft = Offset(stroke / 2, stroke / 2)
        // Background track
        drawArc(
            color = MaterialTheme.colorScheme.surfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(arcDimen, arcDimen),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        // Progress arc
        drawArc(
            brush = Brush.sweepGradient(listOf(color, color.copy(alpha = 0.6f))),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(arcDimen, arcDimen),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}
```

- [ ] **Step 2: Implement ParticleSystem.kt**

Create `ui/kibot/ParticleSystem.kt` — lightweight Canvas particle spawner:
```kotlin
data class Particle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val life: Float, val maxLife: Float,
    val emoji: String? = null,  // null = circle particle
    val color: Color = Color.White,
    val size: Float = 4f,
)

@Composable
fun ParticleCanvas(
    particles: List<Particle>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.emoji != null) {
                // Emoji particles drawn as text at scale
                drawText(
                    textMeasurer = rememberTextMeasurer(),
                    text = p.emoji,
                    topLeft = Offset(p.x, p.y),
                    alpha = alpha,
                    size = androidx.compose.ui.text.TextSize(p.size.sp.toPx()),
                )
            } else {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size * alpha,
                    center = Offset(p.x, p.y),
                )
            }
        }
    }
}
```

- [ ] **Step 3: Write tests for particle math**

Create `test/java/ai/ki_kompetenz_training_org/ui/kibot/ParticleTest.kt`:
```kotlin
class ParticleTest {
    @Test fun `particle alpha decreases as life decreases`() {
        val p = Particle(x = 0f, y = 0f, vx = 1f, vy = -1f, life = 0.5f, maxLife = 1f)
        val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
        assertEquals(0.5f, alpha, 0.01f)
    }
    @Test fun `dead particle has zero alpha`() {
        val p = Particle(x = 0f, y = 0f, vx = 0f, vy = 0f, life = 0f, maxLife = 1f)
        assertEquals(0f, (p.life / p.maxLife).coerceIn(0f, 1f))
    }
}
```

Run: `./gradlew :app:testDebugUnitTest --tests "ai.ki_kompetenz_training_org.ui.kibot.ParticleTest" -i`
Expected: PASS

- [ ] **Step 4: Implement KiBotScene.kt (orchestrator)**

Create `ui/kibot/KiBotScene.kt`:
- Wraps `KiBotModel` with `rememberInfiniteTransition` for idle bobbing (Y-axis sinusoidal offset)
- Wraps with `ProgressRing` showing XP
- Spawns particles on emotion triggers (celebration = confetti upward, confused = "?" particles, sleepy = "💤")
- Reads accelerometer for tilt parallax via `SensorManager`
- Handles tap → `HAPPY` bounce animation (short `animateDpAsState` Y offset)

- [ ] **Step 5: Wire KiBotScene into HomeScreen**

In `HomeScreen.kt`, replace `KiBotModel` with `KiBotScene(kiBotState = state.kiBotState)`.

- [ ] **Step 6: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. KiBot bobs, progress ring animates.

- [ ] **Step 7: Commit**

```
git add -A && git commit -m "feat: KiBot particles, progress ring, idle bobbing, tap reaction, tilt parallax"
```

---

### Task 5: String Extraction & i18n Fix

**Files:**
- Modify: `res/values/strings.xml` (~40 new entries)
- Modify: `res/values-en/strings.xml` (~40 new entries, 2 fixes)
- Modify: `res/values-de/strings.xml` (~40 new entries)
- Modify: `ui/minigames/MiniGamesMenuScreen.kt`
- Modify: `ui/quiz/QuizScreen.kt`
- Modify: `ui/forkids/KidsScreen.kt`
- Modify: `ui/home/HomeScreen.kt`
- Modify: `ui/gamification/GamificationScreen.kt`

**Context:** Extract all hardcoded strings to `strings.xml` resources. Fix English `premium_cta` and `premium_trust` that incorrectly describe Stripe/subscription flow for the free edition.

- [ ] **Step 1: Add all new string resources to values/strings.xml**

Add these entries (German as default):
```xml
<!-- KiBot -->
<string name="kibot_level">Dein KI-Begleiter · Level %1$d</string>
<string name="kibot_hello_title">Hallo! Ich bin KiBot.</string>
<string name="kibot_hello_body">Ich lerne KI-Kompetenz — und du trainierst mich dabei! Je mehr du lernst, desto mehr wachse ich.</string>
<string name="kibot_hello_cta">Los geht\'s!</string>
<string name="kibot_sleepy_hint">KiBot schläft… Tägliches Check-in weckt es auf!</string>

<!-- Home quick actions -->
<string name="home_quick_quiz">KI-Score</string>
<string name="home_quick_quiz_desc">Teste dein Wissen</string>
<string name="home_quick_games">Mini-Spiele</string>
<string name="home_quick_games_desc">8 kostenlose Spiele</string>
<string name="home_quick_lessons">Lektionen</string>
<string name="home_quick_lessons_desc">EU AI Act lernen</string>
<string name="home_quick_srs">Wiederholen</string>
<string name="home_quick_srs_desc">Spaced Repetition</string>

<!-- Home more section -->
<string name="home_more_kids">Für Kinder</string>
<string name="home_more_kids_desc">KI spielerisch entdecken</string>
<string name="home_more_seniors">Für Senioren</string>
<string name="home_more_seniors_desc">Passwörter, Phishing, Deepfakes</string>

<!-- MiniGames menu extracted -->
<string name="games_menu_subtitle">Kurzweilige Lernspiele — jede Runde bringt XP und einen Lernmoment.</string>
<string name="games_premium_header">⭐ Premium-Spiele</string>
<string name="games_premium_subtitle">Fortgeschrittene Deep-Dives mit höheren XP-Belohnungen.</string>
<string name="difficulty_beginner">Anfänger</string>
<string name="difficulty_intermediate">Fortgeschritten</string>
<string name="difficulty_expert">Experte</string>

<!-- Quiz extracted -->
<string name="quiz_intro_title">KI-Score</string>
<string name="quiz_intro_subtitle">Wie KI-fit bist du?</string>
<string name="quiz_intro_description">Teste dein Wissen zu Künstlicher Intelligenz, EU AI Act und KI im Arbeitsalltag. Am Ende erhältst du deinen persönlichen KI-Score zum Teilen!</string>
<string name="quiz_next_question">Nächste Frage</string>
<string name="quiz_my_result">Mein Ergebnis</string>
<string name="quiz_stats_correct">Richtig</string>
<string name="quiz_stats_points">Punkte</string>
<string name="quiz_stats_max_combo">Max Combo</string>
<string name="quiz_stats_lives_left">Leben übrig</string>

<!-- Kids extracted -->
<string name="kids_title">ForKids</string>
<string name="kids_coppa_title">Für Eltern — Datenschutz-Info</string>
<string name="kids_coppa_text">Keine Daten werden gesendet. Alles bleibt lokal.</string>
<string name="kids_coppa_more">Mehr erfahren →</string>
<string name="kids_heading">🤖 KI-Lernen für Kinder</string>
<string name="kids_heading_desc">Spielerisch KI verstehen — COPPA-konform, lokal gespeichert, keine Datenweitergabe.</string>
<string name="kids_done_title">Geschafft!</string>
<string name="kids_done_text">Du hast diese Lektion abgeschlossen!</string>
<string name="kids_dismiss">Verstanden</string>

<!-- Seniors -->
<string name="seniors_title">Für Senioren</string>

<!-- Profile expanded -->
<string name="profile_streak_label">Tages-Serie</string>
<string name="profile_no_streak">Noch keine Serie</string>

<!-- Settings / Language -->
<string name="settings_language">Sprache</string>
<string name="settings_language_system">Systemstandard</string>
<string name="settings_language_de">Deutsch</string>
<string name="settings_language_en">English</string>

<!-- Error states -->
<string name="error_offline_title">Keine Verbindung</string>
<string name="error_offline_message">KI-Score braucht eine Internetverbindung zum Laden.</string>
<string name="error_offline_retry">Erneut versuchen</string>
<string name="error_offline_fallback">Mini-Spiele offline spielen →</string>
<string name="error_generic_title">Etwas ist schiefgelaufen</string>
<string name="error_generic_retry">Erneut versuchen</string>

<!-- Premium (FIXED) -->
<string name="premium_cta">Premium in der Google-Play-App entdecken</string>
<string name="premium_trust">Diese Open-Source-Edition enthält 8 kostenlose Spiele. Alle 16 Spiele + 14 Lektionen: Google-Play-Version</string>
```

- [ ] **Step 2: Add English translations to values-en/strings.xml**

Same keys with English values. Fix the 2 broken entries:
```xml
<string name="premium_cta">Discover Premium in Google Play</string>
<string name="premium_trust">This open-source edition contains 8 free games. All 16 games + 14 lessons: Google Play version</string>
```

- [ ] **Step 3: Replace all hardcoded strings in Kotlin files**

In each file, find hardcoded German strings and replace with `stringResource(R.string.xxx)`:
- `MiniGamesMenuScreen.kt`: Replace "Kurzweilige Lernspiele…", difficulty labels "Anfänger"/"Fortgeschritten"/"Experte", premium header/subtitle
- `QuizScreen.kt`: Replace "Nächste Frage", "Mein Ergebnis", "Richtig", "Punkte", "Max Combo", "Leben übrig", intro description
- `KidsScreen.kt`: Replace "ForKids", "Für Eltern…", "Verstanden", "Geschafft!", "KI-Lernen für Kinder"
- `HomeScreen.kt`: Replace quick action titles/descriptions, kids/seniors text
- `GamificationScreen.kt`: Replace "Tages-Serie", "Noch keine Serie"

- [ ] **Step 4: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```
git add -A && git commit -m "fix: extract all hardcoded strings to resources, fix English premium_cta and premium_trust"
```

---

### Task 6: Language Toggle

**Files:**
- Create: `data/prefs/SettingsStore.kt`
- Modify: `KiKompetenzApp.kt` (add `settingsStore`)
- Modify: `data/minigames/MiniGames.kt` (replace `currentLang()` to read from `SettingsStore`)
- Modify: `ui/gamification/GamificationScreen.kt` (add language toggle UI)

**Context:** Create a `SettingsStore` backed by `DataStore` that persists language preference. Add a language picker in the Profile tab. Make `currentLang()` app-controlled instead of system-only.

- [ ] **Step 1: Create SettingsStore.kt**

```kotlin
package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "kikompetenz_settings")

class SettingsStore(private val context: Context) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        const val LANG_SYSTEM = "system"
        const val LANG_DE = "de"
        const val LANG_EN = "en"
    }

    val language: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: LANG_SYSTEM
    }

    suspend fun setLanguage(lang: String) {
        context.settingsDataStore.edit { it[LANGUAGE_KEY] = lang }
    }

    /** Effective language: app setting or system default. */
    fun effectiveLanguage(appLang: String?): String {
        val l = if (appLang == LANG_SYSTEM || appLang == null) Locale.getDefault().language else appLang
        return if (l == "de") "de" else "en"
    }
}
```

- [ ] **Step 2: Add SettingsStore to KiKompetenzApp**

In `KiKompetenzApp.kt`:
```kotlin
lateinit var settingsStore: SettingsStore
    private set

override fun onCreate() {
    // ...
    settingsStore = SettingsStore(this)
}
```

- [ ] **Step 3: Replace currentLang() to use SettingsStore**

In `MiniGames.kt`, change `currentLang()` to accept a `String` parameter (the effective language from `SettingsStore`). Update all call sites in `MiniGamesMenuScreen.kt`, `MiniGameScreen.kt` to pass the language from a `StateFlow` observed from `settingsStore.language`.

- [ ] **Step 4: Add language toggle UI in GamificationScreen**

Add a `LanguageSection` composable below the DSGVO note:
```kotlin
@Composable
private fun LanguageSection(currentLang: String, onLanguageChange: (String) -> Unit) {
    val options = listOf(
        Triple("system", stringResource(R.string.settings_language_system), "📱"),
        Triple("de", stringResource(R.string.settings_language_de), "🇩🇪"),
        Triple("en", stringResource(R.string.settings_language_en), "🇬🇧"),
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            options.forEach { (key, label, emoji) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .then(if (currentLang == key) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier)
                        .clickable { onLanguageChange(key) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(emoji)
                    Spacer(Modifier.width(10.dp))
                    Text(label, modifier = Modifier.weight(1f))
                    if (currentLang == key) Text("✓", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A && git commit -m "feat: add language toggle (system/de/en) in Profile tab via DataStore"
```

---

### Task 7: Error States & Offline

**Files:**
- Create: `data/connectivity/ConnectivityObserver.kt`
- Modify: `KiKompetenzApp.kt` (add `connectivityObserver`)
- Modify: `ui/quiz/QuizViewModel.kt` (handle offline/error)
- Modify: `ui/quiz/QuizScreen.kt` (show error UI)

**Context:** Quiz questions are fetched from the API. If the network is unavailable, the user currently sees an infinite spinner. Add a `ConnectivityObserver` and show an error state with retry.

- [ ] **Step 1: Create ConnectivityObserver.kt**

```kotlin
package ai.ki_kompetenz_training_org.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ConnectivityObserver(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        // Emit current state
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        trySend(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
```

- [ ] **Step 2: Add to KiKompetenzApp**

```kotlin
lateinit var connectivityObserver: ConnectivityObserver
    private set

override fun onCreate() {
    // ...
    connectivityObserver = ConnectivityObserver(this)
}
```

- [ ] **Step 3: Add error state to QuizViewModel**

In `QuizViewModel`, catch network errors in `start()`:
```kotlin
fun start() {
    viewModelScope.launch {
        _state.value = _state.value.copy(phase = QuizPhase.LOADING)
        contentRepository.fetchKiScoreQuestions()
            .onSuccess { questions ->
                if (questions.isEmpty()) {
                    _state.value = _state.value.copy(phase = QuizPhase.INTRO, questions = emptyList())
                } else {
                    _state.value = _state.value.copy(phase = QuizPhase.INTRO, questions = questions)
                }
            }
            .onFailure {
                _state.value = _state.value.copy(phase = QuizPhase.ERROR, errorMessage = it.message)
            }
    }
}
```

- [ ] **Step 4: Add error UI to QuizScreen**

In `QuizScreen.kt`, add a new case in the `when (state.phase)` block:
```kotlin
QuizPhase.ERROR -> ErrorContent(
    modifier = Modifier.padding(padding),
    onRetry = vm::start,
    onFallback = onBack,  // or navigate to MiniGames
)
```

`ErrorContent` composable: Card with error icon, message, retry button.

- [ ] **Step 5: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A && git commit -m "feat: add ConnectivityObserver and error/offline state for Quiz screen"
```

---

### Task 8: Accessibility

**Files:**
- Modify: `ui/kibot/KiBotScene.kt` (add semantics)
- Modify: `ui/quiz/QuizScreen.kt` (add option semantics)
- Modify: `ui/home/HomeScreen.kt` (add grid item semantics)
- Modify: `ui/forkids/KidsScreen.kt` (replace hardcoded "Zurück")

**Context:** Add `semantics` blocks for screen reader support. KiBot needs a content description. Quiz options need labels. Kids/Seniors screens use hardcoded "Zurück" strings.

- [ ] **Step 1: Add semantics to KiBotScene**

```kotlin
Modifier.semantics {
    contentDescription = "Dein KI-Begleiter, Level $level. ${stateDescription}"
    role = Role.Image
}
```

- [ ] **Step 2: Add semantics to quiz options**

Each option button:
```kotlin
Modifier.semantics {
    contentDescription = "Option ${('A'.code + index).toChar()}: $option"
}
```

- [ ] **Step 3: Replace hardcoded "Zurück" in Kids/Seniors**

Find all instances of `contentDescription = "Zurück"` and replace with `stringResource(R.string.nav_back)`.

- [ ] **Step 4: Add semantics to home quick-action grid**

Each grid card:
```kotlin
Modifier.semantics {
    contentDescription = "$title. $subtitle"
    role = Role.Button
}
```

- [ ] **Step 5: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
git add -A && git commit -m "a11y: add content descriptions to KiBot, quiz options, grid items; fix hardcoded back strings"
```

---

### Task 9: Test Package Migration

**Files:**
- Move: 10 test files from `de/kikompetenz/app/` → `ai/ki_kompetenz_training_org/`
- Delete: empty `de/kikompetenz/app/` directories

**Context:** Tests still live in the old package `de.kikompetenz.app` after the rename. Move them to match the current package.

- [ ] **Step 1: Move test files**

For each of the 10 files, move to the correct package path and update the `package` declaration:
```bash
mkdir -p app/src/test/java/ai/ki_kompetenz_training_org/{api,data/repo,lesson,ui/lessons}
mv app/src/test/java/de/kikompetenz/app/CoreLogicTest.kt app/src/test/java/ai/ki_kompetenz_training_org/
mv app/src/test/java/de/kikompetenz/app/GamificationRulesTest.kt app/src/test/java/ai/ki_kompetenz_training_org/
mv app/src/test/java/de/kikompetenz/app/PropertyBasedTests.kt app/src/test/java/ai/ki_kompetenz_training_org/
mv app/src/test/java/de/kikompetenz/app/SecurityTest.kt app/src/test/java/ai/ki_kompetenz_training_org/
mv app/src/test/java/de/kikompetenz/app/SrsSessionTest.kt app/src/test/java/ai/ki_kompetenz_training_org/
mv app/src/test/java/de/kikompetenz/app/api/ApiContractTest.kt app/src/test/java/ai/ki_kompetenz_training_org/api/
mv app/src/test/java/de/kikompetenz/app/data/repo/ContentRepositoryTest.kt app/src/test/java/ai/ki_kompetenz_training_org/data/repo/
mv app/src/test/java/de/kikompetenz/app/data/repo/PremiumRepositoryTest.kt app/src/test/java/ai/ki_kompetenz_training_org/data/repo/
mv app/src/test/java/de/kikompetenz/app/lesson/LessonIntegrationTest.kt app/src/test/java/ai/ki_kompetenz_training_org/lesson/
mv app/src/test/java/de/kikompetenz/app/ui/lessons/LessonDetailViewModelTest.kt app/src/test/java/ai/ki_kompetenz_training_org/ui/lessons/
rm -rf app/src/test/java/de
```

- [ ] **Step 2: Fix package declarations in moved files**

Each file: change `package de.kikompetenz.app` → `package ai.ki_kompetenz_training_org` (and sub-packages accordingly).

- [ ] **Step 3: Fix import paths in moved files**

Update imports referencing old package to new package.

- [ ] **Step 4: Run tests**

Run: `./gradlew :app:testDebugUnitTest`
Expected: All tests pass (same count as before migration)

- [ ] **Step 5: Commit**

```
git add -A && git commit -m "refactor: migrate 10 test files from de.kikompetenz.app to ai.ki_kompetenz_training_org"
```

---

## Self-Review Checklist

**Spec coverage:**
- [x] Bottom nav (Task 1)
- [x] Home redesign (Task 2)
- [x] KiBot state machine (Task 3)
- [x] KiBot animations/particles/parallax (Task 4)
- [x] String extraction & English fixes (Task 5)
- [x] Language toggle (Task 6)
- [x] Error states & offline (Task 7)
- [x] Accessibility (Task 8)
- [x] Test migration (Task 9)
- [x] Dark mode: KiBot uses MaterialTheme colors (Task 3–4), no separate dark mode task needed
- [x] Procedural 2D model approach (no external .glb needed)

**Placeholder scan:** No TBDs, no "add appropriate error handling", no "similar to Task N".

**Type consistency:** `GrowthStage`, `EmotionalState`, `KiBotState` defined in Task 3, used consistently in Tasks 3, 4, and HomeScreen wiring.
