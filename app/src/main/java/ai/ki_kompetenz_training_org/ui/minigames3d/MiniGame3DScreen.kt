package ai.ki_kompetenz_training_org.ui.minigames3d

import ai.ki_kompetenz_training_org.KiKompetenzApp
import ai.ki_kompetenz_training_org.R
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import ai.ki_kompetenz_training_org.data.minigames3d.ClassifyAction
import ai.ki_kompetenz_training_org.data.minigames3d.EndReason
import ai.ki_kompetenz_training_org.data.minigames3d.GameConfig
import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import ai.ki_kompetenz_training_org.data.minigames3d.GameState
import ai.ki_kompetenz_training_org.data.minigames3d.InputState
import ai.ki_kompetenz_training_org.data.minigames3d.MasteryTracker
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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

private object C {
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

    var input by remember { mutableStateOf(InputState(false, false, false, false, false, null)) }
    var joystick by remember { mutableStateOf(Offset.Zero) }
    val isSnipe = mode == GameMode.TRUTH_SNIPE

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dt = (now - last) / 1_000_000_000.0
                    vm.step(input, min(dt, 0.05))
                }
                last = now
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(C.bg)) {
        when (state.phase) {
            ArenaPhase.COUNTDOWN -> StartOverlay(game, vm, onBack, mode)
            ArenaPhase.PLAYING -> GameCanvas(
                vm = vm, input = input, onInput = { input = it },
                joystick = joystick, onJoystick = { joystick = it },
                isSnipe = isSnipe,
                decisionTimer = state.decisionTimer,
                scanned = state.scannedText, scannedIsRisk = state.scannedIsRisk,
                scannedDomain = state.scannedDomain, scannedExplanation = state.scannedExplanation,
            )
            ArenaPhase.RESULT -> state.result?.let { r ->
                ResultOverlay(r, vm, onBack, mode, state.hud.score)
            }
        }
    }
}

@Composable
private fun StartOverlay(game: MiniGame, vm: MiniGame3DViewModel, onBack: () -> Unit, mode: GameMode) {
    val lang = currentLang()
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(game.emoji, fontSize = 56.sp)
            Text(game.title(lang), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold, color = C.white, textAlign = TextAlign.Center)
            Text(game.description(lang), style = MaterialTheme.typography.bodyMedium,
                color = C.soft, textAlign = TextAlign.Center)
            Text(howTo(mode, lang), style = MaterialTheme.typography.bodySmall,
                color = C.dim, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { vm.start() },
                colors = ButtonDefaults.buttonColors(containerColor = C.ring, contentColor = C.white)) {
                Text(stringResource(R.string.games_start))
            }
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.games_back), color = C.dim)
            }
        }
    }
}

private fun howTo(mode: GameMode, lang: String): String = when (mode) {
    GameMode.ORB_HUNT -> if (lang == "de") "Scanne Orbs, lies die KI-Aussage und entscheide: Fakt oder Risiko?"
        else "Scan orbs, read the AI statement, decide: fact or risk?"
    GameMode.MAZE_RUN -> if (lang == "de") "Steuere zum grunen Ziel. Am Ziel: Fakt oder Risiko?"
        else "Steer to the green goal. At the goal: fact or risk?"
    GameMode.TRUTH_SNIPE -> if (lang == "de") "Sammle Fakten, treffe keine - zerstore Falschmeldungen."
        else "Collect facts, hit none - destroy fakes."
}

@Composable
private fun GameCanvas(
    vm: MiniGame3DViewModel,
    input: InputState,
    onInput: (InputState) -> Unit,
    joystick: Offset,
    onJoystick: (Offset) -> Unit,
    isSnipe: Boolean,
    decisionTimer: Double?,
    scanned: String?,
    scannedIsRisk: Boolean?,
    scannedDomain: String?,
    scannedExplanation: String?,
) {
    val game = vm.game
    val hud by vm.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Arena (2D top-down projection of the simulation)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val nx = (joystick.x + dragAmount.x).coerceIn(-1f, 1f)
                            val ny = (joystick.y + dragAmount.y).coerceIn(-1f, 1f)
                            onJoystick(Offset(nx, ny))
                            onInput(InputState(ny < -0.15f, ny > 0.15f, nx < -0.15f, nx > 0.15f, false, null))
                        },
                        onDragEnd = {
                            onJoystick(Offset.Zero)
                            onInput(InputState(false, false, false, false, false, null))
                        },
                    )
                },
        ) {
            drawArena(game)
        }

        // HUD (top)
        HUD(game, hud)

        // Scanned statement card (learning moment)
        if (scanned != null) {
            StatementCard(
                text = scanned, isRisk = scannedIsRisk == true,
                domain = scannedDomain, explanation = scannedExplanation,
                decisionTimer = decisionTimer,
            )
        }

        // Bottom controls: classify buttons (orb/maze) or fire (snipe)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            BottomControls(isSnipe, onClassify = { action ->
                onInput(InputState(input.up, input.down, input.left, input.right, false, action))
            }, onFire = {
                onInput(InputState(input.up, input.down, input.left, input.right, true, null))
            })
        }
    }
}

private fun DrawScope.drawArena(game: GameState?) {
    if (game == null) return
    val cfg = GameConfig.MODES[game.mode]!!
    val w = size.width
    val h = size.height
    val arena = min(w, h) * 0.94f
    val scale = arena / (cfg.arenaRadius * 2)
    val cx = w / 2f
    val cy = h / 2f
    val lang = currentLang()

    fun sx(x: Double) = (cx + x * scale).toFloat()
    fun sz(z: Double) = (cy + z * scale).toFloat()
    fun r(d: Double) = (d * scale).toFloat()

    // ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1B2A54), C.bg),
            center = Offset(cx, cy),
            radius = arena * 0.75f,
        ),
        radius = arena,
        center = Offset(cx, cy),
    )
    // grid
    val steps = 9
    for (i in -steps..steps) {
        val off = cx + i * (arena / steps)
        drawLine(C.grid, Offset(off, cy - arena / 2), Offset(off, cy + arena / 2), 1f)
        drawLine(C.grid, Offset(cx - arena / 2, off), Offset(cx + arena / 2, off), 1f)
    }
    // boundary ring
    drawCircle(color = C.ring, radius = arena / 2, center = Offset(cx, cy), style = Stroke(3f))

    // walls (maze)
    for (wl in game.walls) {
        val x0 = sx(wl.x - wl.w); val y0 = sz(wl.z - wl.d)
        val x1 = sx(wl.x + wl.w); val y1 = sz(wl.z + wl.d)
        drawRoundRect(color = Color(0xFF3A4A7A), topLeft = Offset(min(x0, x1), min(y0, y1)),
            size = Size(abs(x1 - x0), abs(y1 - y0)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
    }

    // goal
    game.goal?.let { g ->
        drawCircle(color = C.goalGlow, radius = r(GameConfig.GOAL_RADIUS * 2.2), center = Offset(sx(g.x), sz(g.z)))
        drawCircle(color = C.goal, radius = r(GameConfig.GOAL_RADIUS), center = Offset(sx(g.x), sz(g.z)))
    }

    // collectibles (orbs/facts - blue)
    for (d in game.collectibles) {
        val pos = Offset(sx(d.x), sz(d.z))
        drawCircle(color = C.orbGlow.copy(alpha = 0.45f), radius = r(d.r * 2.4), center = pos)
        drawCircle(color = C.orb, radius = r(d.r), center = pos)
        if (d.statement != null) {
            drawCircle(color = Color.White, radius = r(d.r * 0.32), center = pos)
        }
    }

    // hazards (risks - red/purple)
    for (d in game.hazards) {
        val col = when ((d.kind and 3)) { 0 -> C.hz1; 1 -> C.hz2; 2 -> C.hz3; else -> C.hz4 }
        val pos = Offset(sx(d.x), sz(d.z))
        drawCircle(color = col.copy(alpha = 0.4f), radius = r(d.r * 2.4), center = pos)
        drawCircle(color = col, radius = r(d.r), center = pos)
    }

    // bullets
    for (b in game.bullets) {
        drawCircle(color = C.bullet, radius = r(GameConfig.BULLET_RADIUS), center = Offset(sx(b.x), sz(b.z)))
    }

    // player
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(C.playerCore, C.player),
            center = Offset(sx(game.player.x), sz(game.player.z)),
            radius = r(game.player.let { GameConfig.MODES[game.mode]!!.playerRadius * 2 }),
        ),
        radius = r(GameConfig.MODES[game.mode]!!.playerRadius * 1.4),
        center = Offset(sx(game.player.x), sz(game.player.z)),
    )
    // heading indicator
    val px = sx(game.player.x); val pz = sz(game.player.z)
    val ang = game.player.dir
    drawLine(C.white, Offset(px, pz), Offset(px + cos(ang).toFloat() * 24f, pz + sin(ang).toFloat() * 24f), 4f)
}
@Composable
private fun HUD(game: GameState?, hud: ArenaUiState) {
    val g = game ?: return
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(color = C.panel, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("${stringResource(R.string.games_score)}: ${hud.hud.score}", color = C.white,
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("${stringResource(R.string.games_time)}: ${hud.hud.timeLeft}s", color = C.soft, fontSize = 13.sp)
                Text("${stringResource(R.string.games_streak)}: ${hud.hud.streak}", color = C.ring, fontSize = 13.sp)
            }
        }
        Surface(color = C.panel, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(g.maxHealth) { i ->
                    val on = i < hud.hud.health
                    Text(if (on) "\u2764\uFE0F" else "\u2661", fontSize = 18.sp,
                        color = if (on) C.red else C.dim)
                }
                Spacer(Modifier.width(8.dp))
                Text("${hud.hud.score}/${hud.hud.target}", color = C.soft, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatementCard(
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
            .background(C.panel, RoundedCornerShape(14.dp))
            .border(1.dp, if (isRisk) C.red else C.green, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(desc, color = if (isRisk) C.red else C.green, fontWeight = FontWeight.Bold)
            domain?.let {
                Spacer(Modifier.width(10.dp))
                Text(it, color = C.dim, fontSize = 12.sp)
            }
            decisionTimer?.let {
                Spacer(Modifier.weight(1f))
                Text("${ceil(it).toInt()}", color = C.white, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(text, color = C.white, style = MaterialTheme.typography.bodyMedium)
        explanation?.let {
            if (it.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(it, color = C.soft, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BottomControls(
    isSnipe: Boolean,
    onClassify: (ClassifyAction) -> Unit,
    onFire: () -> Unit,
) {
    val lang = currentLang()
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (isSnipe) {
            Button(
                onClick = onFire,
                modifier = Modifier.size(width = 96.dp, height = 64.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = C.bullet, contentColor = C.bg),
            ) {
                Text(if (lang == "de") "FEUER" else "FIRE", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = { onClassify(ClassifyAction.FACT) },
                modifier = Modifier.size(width = 110.dp, height = 64.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = C.green, contentColor = C.white),
            ) {
                Text(if (lang == "de") "FAKT" else "FACT", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onClassify(ClassifyAction.RISK) },
                modifier = Modifier.size(width = 110.dp, height = 64.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = C.red, contentColor = C.white),
            ) {
                Text(if (lang == "de") "RISIKO" else "RISK", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResultOverlay(
    result: ArenaResult,
    vm: MiniGame3DViewModel,
    onBack: () -> Unit,
    mode: GameMode,
    finalScore: Int,
) {
    val lang = currentLang()
    Box(modifier = Modifier.fillMaxSize().background(C.bg.copy(alpha = 0.96f)).padding(24.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (result.won) "\uD83C\uDF89" else "\uD83D\uDCA1", fontSize = 56.sp)
            Text(
                if (result.won) stringResource(R.string.games_arena_won)
                else stringResource(R.string.games_arena_lost),
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = C.white,
            )
            Text(
                stringResource(R.string.games_arena_xp, finalScore, result.target, result.earnedXp),
                color = C.soft, style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.games_arena_classified, result.correct, result.total),
                color = C.ring, fontSize = 14.sp,
            )

            // Individualized learning feedback: weak domains
            if (result.weakDomains.isNotEmpty()) {
                Surface(color = C.panel, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.games_arena_review),
                            fontWeight = FontWeight.SemiBold, color = C.white,
                        )
                        Spacer(Modifier.height(4.dp))
                        result.weakDomains.take(3).forEach { d ->
                            Text("\u2022 $d", color = C.soft, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.start() },
                    colors = ButtonDefaults.buttonColors(containerColor = C.ring, contentColor = C.white)) {
                    Text(stringResource(R.string.games_retry))
                }
                OutlinedButton(onClick = onBack) {
                    Text(stringResource(R.string.games_back))
                }
            }
        }
    }
}
