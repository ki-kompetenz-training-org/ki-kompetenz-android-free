/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native MiniGame3DScreen - will replace MiniGame3DScreen.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.ui.minigames3d

import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyAction
import ai.ki_kompetenz_training_org.data.minigames3d.Direction
import ai.ki_kompetenz_training_org.data.minigames3d.GameConfig
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.GameState
import ai.ki_kompetenz_training_org.data.minigames3d.LiteracyBank
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.*

/** Colors for the touch-native arena */
private object CNew {
    val bg = Color(0xFF0B1020)
    val grid = Color(0xFF1A2547)
    val gridMaj = Color(0xFF2A3A66)
    val ring = Color(0xFF4F8CFF)
    val player = Color(0xFF9BE7FF)
    val playerCore = Color(0xFF2BD4FF)
    val orb = Color(0xFF3B82F6)
    val orbGlow = Color(0xFF2563EB)
    val hz1 = Color(0xFFFF4D6D)
    val hz2 = Color(0xFFA855F7)
    val hz3 = Color(0xFFFB923C)
    val hz4 = Color(0xFFEC4899)
    val bullet = Color(0xFF7DF9FF)
    val goal = Color(0xFF22C55E)
    val goalGlow = Color(0xFF15803D)
    val green = Color(0xFF22C55E)
    val red = Color(0xFFEF4444)
    val white = Color(0xFFFFFFFF)
    val soft = Color(0xFFB0BBD6)
    val dim = Color(0xFF8E9BBB)
    val panel = Color(0xCC0E1428)
}

/**
 * Touch-native arena game screen.
 * 
 * Uses tap-to-classify and swipe-to-dash instead of joystick input.
 * Entities freeze when pendingDecision != null (freeze invariant).
 */
@Composable
fun MiniGame3DScreen(game: MiniGame, onBack: () -> Unit) {
    val app = KiKompetenzApp.from(LocalContext.current)
    val mode = game.threeMode ?: GameMode.ORB_HUNT
    val prefs = remember { 
        app.getSharedPreferences("kikompetenz_gamification", Context.MODE_PRIVATE) 
    }
    val dailyRepo = remember { 
        ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository(prefs) 
    }
    val vm: MiniGame3DViewModel = viewModel(key = game.id) { 
        MiniGame3DViewModel(mode, app.gamificationRepository, MasteryTracker(prefs), dailyRepo) 
    }
    vm.setLang(currentLang())
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { 
        var last = 0L 
        while (true) { 
            withFrameNanos { now -> 
                if (last != 0L) { 
                    val dt = (now - last) / 1_000_000_000.0 
                    vm.step(min(dt, 0.05)) 
                } 
                last = now 
            } 
        } 
    } 

    Box(modifier = Modifier.fillMaxSize().background(CNew.bg)) { 
        when (state.phase) { 
            ArenaPhase.COUNTDOWN -> StartOverlayNew(game, vm, onBack, mode) 
            ArenaPhase.PLAYING -> GameCanvasNew( 
                vm = vm, 
                mode = mode,
                decisionTimer = state.decisionTimer, 
                scanned = state.scannedText, scannedIsRisk = state.scannedIsRisk, 
                scannedDomain = state.scannedDomain, scannedExplanation = state.scannedExplanation, 
            ) 
            ArenaPhase.RESULT -> state.result?.let { r -> 
                ResultOverlayNew(r, vm, onBack, mode, state.hud.score) 
            } 
        } 
    } 
}

@Composable 
private fun StartOverlayNew(game: MiniGame, vm: MiniGame3DViewModel, onBack: () -> Unit, mode: GameMode) { 
    val lang = currentLang() 
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { 
        Column( 
            modifier = Modifier.fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.spacedBy(10.dp), 
        ) { 
            Text(game.emoji, fontSize = 56.sp) 
            Text(game.title(lang), style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.SemiBold, color = CNew.white, textAlign = TextAlign.Center) 
            Text(game.description(lang), style = MaterialTheme.typography.bodyMedium, 
                color = CNew.soft, textAlign = TextAlign.Center) 
            Text(howToNew(mode, lang), style = MaterialTheme.typography.bodySmall, 
                color = CNew.dim, textAlign = TextAlign.Center) 
            Spacer(modifier = Modifier.height(8.dp)) 
            Button(onClick = { vm.start() }, 
                colors = ButtonDefaults.buttonColors(containerColor = CNew.ring, contentColor = CNew.white)) { 
                Text(stringResource(R.string.games_start)) 
            } 
            TextButton(onClick = onBack) { 
                Text(stringResource(R.string.games_back), color = CNew.dim) 
            } 
        } 
    } 
}

/** Touch-native instructions */
private fun howToNew(mode: GameMode, lang: String): String = when (mode) { 
    GameMode.ORB_HUNT -> if (lang == "de") "Tippe Orbs an zum Classification. Fakt oder Risiko?" 
        else "Tap orbs to classify. Fact or risk?" 
    GameMode.MAZE_RUN -> if (lang == "de") "Wische zum Bewegen. Am Ziel: Fakt oder Risiko?" 
        else "Swipe to dash. At goal: fact or risk?" 
    GameMode.TRUTH_SNIPE -> if (lang == "de") "Wische zum Bewegen, Tippe zum Schiessen. Zerstore Fakes!" 
        else "Swipe to move, tap to shoot. Destroy fakes!" 
}

@Composable 
private fun GameCanvasNew( 
    vm: MiniGame3DViewModel, 
    mode: GameMode,
    decisionTimer: Double?, 
    scanned: String?, 
    scannedIsRisk: Boolean?, 
    scannedDomain: String?, 
    scannedExplanation: String?, 
) { 
    val game = vm.game 
    val hud by vm.state.collectAsState() 
    val isPendingDecision = game?.pendingDecision != null 

    Box(modifier = Modifier.fillMaxSize()) { 
        // Arena 
        Canvas( 
            modifier = Modifier.fillMaxSize().pointerInput(Unit) { 
                detectTapGestures( 
                    onTap = { offset -> 
                        // Only process taps if we have a game and no pending decision
                        // (freeze invariant)
                        if (!isPendingDecision && game != null) { 
                            // Hit test: find disk at tap location
                            val diskIndex = hitTestTap(game, offset, Size(size.width.toFloat(), size.height.toFloat())) 
                            if (diskIndex >= 0) { 
                                vm.onTapEntity(diskIndex) 
                            } 
                        } 
                    } 
                ) 
            }.pointerInput(Unit) { 
            } 
        ) { 
            drawArenaNew(game, mode) 
        } 

        // HUD 
        HUDNew(game, hud) 

        // Scanned statement card 
        if (scanned != null) { 
            StatementCardNew( 
                text = scanned, isRisk = scannedIsRisk == true, 
                domain = scannedDomain, explanation = scannedExplanation, 
                decisionTimer = decisionTimer, 
            ) 
        } 

        // Touch-native bottom controls 
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { 
            BottomControlsNew(mode, vm, isPendingDecision) 
        } 
    } 
}

/** 
 * Hit test: find which disk (collectible or hazard) was tapped.
 * Returns index or -1 if no hit. 
 */ 
private fun hitTestTap(game: GameState?, tapOffset: Offset, canvasSize: Size): Int {
    if (game == null) return -1
    val g = game
    val cfg = GameConfig.getModeConfig(g.mode) ?: return -1
    val w = canvasSize.width
    val h = canvasSize.height
    val arena = min(w, h) * 0.94f
    val scale = arena / (GameConfig.DEFAULT_ARENA_RADIUS * 2)
    val cx = w / 2f
    val cy = h / 2f

    val tapX = (tapOffset.x - cx) / scale
    val tapZ = (tapOffset.y - cy) / scale

    fun distSquared(x: Double, z: Double, tx: Double, tz: Double): Double {
        val dx = x - tx
        val dz = z - tz
        return dx * dx + dz * dz
    }

    val minDist = Double.POSITIVE_INFINITY
    var bestIndex = -1

    // Check collectibles
    for ((index, d) in game.collectibles.withIndex()) {
        val dist = distSquared(d.x, d.z, tapX.toDouble(), tapZ.toDouble())
        if (dist < (d.r * d.r) && dist < minDist) {
            return index
        }
    }

    return -1
}

private fun DrawScope.drawArenaNew(game: GameState?, mode: GameMode) { 
    if (game == null) return 
    val cfg = GameConfig.getModeConfig(game.mode) 
    val w = size.width 
    val h = size.height 
    val arena = min(w, h) * 0.94f 
    val scale = arena / (GameConfig.DEFAULT_ARENA_RADIUS * 2) 
    val cx = w / 2f 
    val cy = h / 2f 
    val lang = currentLang() 

    fun sx(x: Double) = (cx + x * scale).toFloat() 
    fun sz(z: Double) = (cy + z * scale).toFloat() 
    fun r(d: Double) = (d * scale).toFloat() 

    // Ambient glow 
    drawCircle( 
        brush = Brush.radialGradient( 
            colors = listOf(Color(0xFF1B2A54), CNew.bg), 
            center = Offset(cx, cy), 
            radius = arena * 0.75f, 
        ), 
        radius = arena, 
        center = Offset(cx, cy), 
    ) 

    // Grid 
    val steps = 9 
    for (i in -steps..steps) { 
        val off = cx + i * (arena / steps) 
        drawLine(CNew.grid, Offset(off, cy - arena / 2), Offset(off, cy + arena / 2), 1f) 
        drawLine(CNew.grid, Offset(cx - arena / 2, off), Offset(cx + arena / 2, off), 1f) 
    } 

    // Boundary ring 
    drawCircle(color = CNew.ring, radius = arena / 2, center = Offset(cx, cy), style = Stroke(3f)) 

    // Maze: draw cells
    if (mode == GameMode.MAZE_RUN && game.maze != null) {
        val maze = game.maze!!
        val cellSize = arena / max(maze.rows, maze.cols).toFloat()
        val mazeStartX = cx - (maze.cols * cellSize) / 2
        val mazeStartY = cy - (maze.rows * cellSize) / 2

        for (r in 0 until maze.rows) {
            for (c in 0 until maze.cols) {
                val x = mazeStartX + c * cellSize + cellSize / 2
                val y = mazeStartY + r * cellSize + cellSize / 2
                when (maze[r, c]) {
                    '#' -> {
                        // Wall
                        drawRoundRect(
                            color = Color(0xFF3A4A7A),
                            topLeft = Offset(x - cellSize/2, y - cellSize/2),
                            size = Size(cellSize, cellSize),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
                        )
                    }
                    'G' -> {
                        // Goal
                        drawCircle(color = CNew.goalGlow, radius = cellSize * 0.45f, center = Offset(x, y))
                        drawCircle(color = CNew.goal, radius = cellSize * 0.35f, center = Offset(x, y))
                    }
                    'S' -> {
                        // Start - draw player
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CNew.playerCore, CNew.player),
                                center = Offset(x, y),
                                radius = cellSize * 0.4f,
                            ),
                            radius = cellSize * 0.3f,
                            center = Offset(x, y),
                        )
                    }
                    '.' -> {
                        // Empty floor
                    }
                    'B' -> {
                        // Bonus
                        drawCircle(color = CNew.orbGlow.copy(alpha = 0.6f), radius = cellSize * 0.35f, center = Offset(x, y))
                        drawCircle(color = CNew.orb, radius = cellSize * 0.25f, center = Offset(x, y))
                    }
                }
            }
        }

        // Draw player at current cell
        val playerX = mazeStartX + game.playerCellCol * cellSize + cellSize / 2
        val playerY = mazeStartY + game.playerCellRow * cellSize + cellSize / 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CNew.playerCore, CNew.player),
                center = Offset(playerX, playerY),
                radius = cellSize * 0.4f,
            ),
            radius = cellSize * 0.3f,
            center = Offset(playerX, playerY),
        )
    } else {
        // OrbHunt/TruthSnipe: draw collectibles and player
        // Collectibles (orbs/facts - blue) 
        for (d in game.collectibles) { 
            val pos = Offset(sx(d.x), sz(d.z)) 
            drawCircle(color = CNew.orbGlow.copy(alpha = 0.45f), radius = r(d.r * 2.4), center = pos) 
            drawCircle(color = CNew.orb, radius = r(d.r), center = pos) 
            if (d.statement != null) { 
                drawCircle(color = Color.White, radius = r(d.r * 0.32), center = pos) 
            } 
        } 

        // Player (center of arena for OrbHunt) 
        drawCircle( 
            brush = Brush.radialGradient( 
                colors = listOf(CNew.playerCore, CNew.player), 
                center = Offset(cx, cy), 
                radius = r(1.4), 
            ), 
            radius = r(1.0), 
            center = Offset(cx, cy), 
        ) 
    }
}

@Composable 
private fun HUDNew(game: GameState?, hud: ArenaUiStateNew) { 
    val g = game ?: return 
    Row( 
        modifier = Modifier.fillMaxWidth().padding(12.dp), 
        horizontalArrangement = Arrangement.SpaceBetween, 
    ) { 
        Surface(color = CNew.panel, shape = RoundedCornerShape(12.dp)) { 
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) { 
                Text("${stringResource(R.string.games_score)}: ${hud.hud.score}", color = CNew.white, 
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp) 
                Text("${stringResource(R.string.games_time)}: ${hud.hud.timeLeft}s", color = CNew.soft, fontSize = 13.sp) 
                Text("${stringResource(R.string.games_streak)}: ${hud.hud.streak}", color = CNew.ring, fontSize = 13.sp) 
            } 
        } 
        Surface(color = CNew.panel, shape = RoundedCornerShape(12.dp)) { 
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { 
                repeat(g.maxHealth) { i -> 
                    val on = i < hud.hud.health 
                    Text(if (on) "\u2764\uFE0F" else "\u2661", fontSize = 18.sp, 
                        color = if (on) CNew.red else CNew.dim) 
                } 
                Spacer(Modifier.width(8.dp)) 
                Text("${hud.hud.score}/${hud.hud.target}", color = CNew.soft, fontSize = 13.sp) 
            } 
        } 
    } 
}

@Composable 
private fun StatementCardNew( 
    text: String, isRisk: Boolean, domain: String?, 
    explanation: String?, decisionTimer: Double?, 
) { 
    val lang = currentLang() 
    val labelFakt = if (lang == "de") "Fakt" else "Fact" 
    val labelRisiko = if (lang == "de") "Risiko" else "Risk" 
    val desc = if (isRisk) labelRisiko else labelFakt 
    Column( 
        modifier = Modifier 
            .fillMaxWidth() 
            .padding(horizontal = 16.dp) 
            .padding(top = 74.dp, bottom = 8.dp) 
            .background(CNew.panel, RoundedCornerShape(14.dp)) 
            .border(1.dp, if (isRisk) CNew.red else CNew.green, RoundedCornerShape(14.dp)) 
            .padding(14.dp), 
    ) { 
        Row(verticalAlignment = Alignment.CenterVertically) { 
            Text(desc, color = if (isRisk) CNew.red else CNew.green, fontWeight = FontWeight.Bold) 
            domain?.let { 
                Spacer(Modifier.width(10.dp)) 
                Text(it, color = CNew.dim, fontSize = 12.sp) 
            } 
            decisionTimer?.let { 
                Spacer(Modifier.weight(1f)) 
                Text("${ceil(it).toInt()}", color = CNew.white, fontWeight = FontWeight.Bold) 
            } 
        } 
        Spacer(Modifier.height(6.dp)) 
        Text(text, color = CNew.white, style = MaterialTheme.typography.bodyMedium) 
        explanation?.let { 
            if (it.isNotBlank()) { 
                Spacer(Modifier.height(4.dp)) 
                Text(it, color = CNew.soft, fontSize = 12.sp) 
            } 
        } 
    } 
}

@Composable 
private fun BottomControlsNew( 
    mode: GameMode, 
    vm: MiniGame3DViewModel, 
    isPendingDecision: Boolean, 
) { 
    val lang = currentLang() 
    val isSnipe = mode == GameMode.TRUTH_SNIPE 
    
    // Show classify buttons only when there's a pending decision
    // For touch-native: tap triggers classify UI, buttons appear to confirm
    if (isPendingDecision) { 
        Row( 
            modifier = Modifier.fillMaxWidth().padding(20.dp), 
            horizontalArrangement = Arrangement.SpaceEvenly, 
        ) { 
            Button( 
                onClick = { vm.onClassify(ClassifyAction.FACT) }, 
                modifier = Modifier.size(width = 110.dp, height = 64.dp), 
                shape = RoundedCornerShape(18.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = CNew.green, contentColor = CNew.white), 
            ) { 
                Text(if (lang == "de") "FAKT" else "FACT", fontWeight = FontWeight.Bold) 
            } 
            Button( 
                onClick = { vm.onClassify(ClassifyAction.RISK) }, 
                modifier = Modifier.size(width = 110.dp, height = 64.dp), 
                shape = RoundedCornerShape(18.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = CNew.red, contentColor = CNew.white), 
            ) { 
                Text(if (lang == "de") "RISIKO" else "RISK", fontWeight = FontWeight.Bold) 
            } 
        } 
    } else if (isSnipe) { 
        // Snipe mode: fire button
        Row( 
            modifier = Modifier.fillMaxWidth().padding(20.dp), 
            horizontalArrangement = Arrangement.Center, 
        ) { 
            Button( 
                onClick = { /* Fire - will be implemented */ }, 
                modifier = Modifier.size(width = 96.dp, height = 64.dp), 
                shape = RoundedCornerShape(18.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = CNew.bullet, contentColor = CNew.bg), 
            ) { 
                Text(if (lang == "de") "FEUER" else "FIRE", fontWeight = FontWeight.Bold) 
            } 
        } 
    }
}

@Composable 
private fun ResultOverlayNew( 
    result: ArenaResultNew, 
    vm: MiniGame3DViewModel, 
    onBack: () -> Unit, 
    mode: GameMode, 
    finalScore: Int, 
) { 
    val lang = currentLang() 
    Box(modifier = Modifier.fillMaxSize().background(CNew.bg.copy(alpha = 0.96f)).padding(24.dp), 
        contentAlignment = Alignment.Center) { 
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { 
            Text(if (result.won) "\uD83C\uDF89" else "\uD83D\uDCA1", fontSize = 56.sp) 
            Text( 
                if (result.won) stringResource(R.string.games_arena_won) 
                else stringResource(R.string.games_arena_lost), 
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = CNew.white, 
            ) 
            Text( 
                stringResource(R.string.games_arena_xp, finalScore, result.target, result.earnedXp), 
                color = CNew.soft, style = MaterialTheme.typography.bodyMedium, 
            ) 
            Text( 
                stringResource(R.string.games_arena_classified, result.correct, result.total), 
                color = CNew.ring, fontSize = 14.sp, 
            ) 

            if (result.weakDomains.isNotEmpty()) { 
                Surface(color = CNew.panel, shape = RoundedCornerShape(14.dp)) { 
                    Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { 
                        Text( 
                            stringResource(R.string.games_arena_review), 
                            fontWeight = FontWeight.SemiBold, color = CNew.white, 
                        ) 
                        Spacer(Modifier.height(4.dp)) 
                        result.weakDomains.take(3).forEach { d -> 
                            Text("\u2022 $d", color = CNew.soft, fontSize = 13.sp) 
                        } 
                    } 
                } 
            } 

            Spacer(Modifier.height(8.dp)) 
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                Button(onClick = { vm.start() }, 
                    colors = ButtonDefaults.buttonColors(containerColor = CNew.ring, contentColor = CNew.white)) { 
                    Text(stringResource(R.string.games_retry)) 
                } 
                OutlinedButton(onClick = onBack) { 
                    Text(stringResource(R.string.games_back)) 
                } 
            } 
        } 
    } 
}
