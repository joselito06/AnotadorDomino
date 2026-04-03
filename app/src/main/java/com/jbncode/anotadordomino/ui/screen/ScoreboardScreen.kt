package com.jbncode.anotadordomino.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.domain.model.WinType
import com.jbncode.anotadordomino.ui.ScoreboardUiState
import com.jbncode.anotadordomino.ui.viewmodel.HandLogUi
import com.jbncode.anotadordomino.ui.viewmodel.ParticipantScoreUi
import com.jbncode.anotadordomino.ui.viewmodel.ScoreboardViewModel
import com.jbncode.anotadordomino.ui.theme.kineticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(
    gameId: Int,
    onMatchFinished: () -> Unit = {},
    onLeave: () -> Unit = {},
    viewModel: ScoreboardViewModel = hiltViewModel()
) {
    LaunchedEffect(gameId) { viewModel.init(gameId) }

    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    val actionLoading  by viewModel.isActionLoading.collectAsStateWithLifecycle()
    val showLeaveDialog by viewModel.showLeaveDialog.collectAsStateWithLifecycle()

    var showAdd  by remember { mutableStateOf(false) }
    var showUndo by remember { mutableStateOf(false) }

    // Interceptar el botón Back del sistema
    BackHandler {
        viewModel.onBackPressed()
    }

    when (val state = uiState) {

        is ScoreboardUiState.Loading -> {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.kineticColors.neonGreen)
            }
        }

        is ScoreboardUiState.Error -> {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center) {
                Text(state.message,
                    color     = MaterialTheme.colorScheme.error,
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(24.dp))
            }
        }

        is ScoreboardUiState.Active -> {

            LaunchedEffect(state.isFinished) {
                if (state.isFinished) onMatchFinished()
            }

            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

                Column(Modifier.fillMaxSize()) {

                    ScoreboardTopBar(onBackClick = { viewModel.onBackPressed() })

                    // Score cards
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.participants.forEach { p ->
                            ParticipantScoreCard(p, Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Log header
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("LOG OF HANDS",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium)
                        Text("${state.handLog.size} ROUNDS",
                            color = MaterialTheme.kineticColors.cyanAccent,
                            style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Rounds list
                    if (state.handLog.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hands played yet.\nTap ADD POINTS to start.",
                                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style     = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier            = Modifier.weight(1f),
                            contentPadding      = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.handLog, key = { it.roundScoreId }) { hand ->
                                HandLogRow(hand)
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }

                    // Action buttons
                    ScoreboardActions(
                        canUndo     = state.handLog.isNotEmpty(),
                        isLoading   = actionLoading,
                        onUndo      = { showUndo = true },
                        onAddPoints = { showAdd  = true },
                        modifier    = Modifier.padding(
                            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp
                        )
                    )
                }

                // Add Points sheet
                if (showAdd) {
                    AddPointsSheet(
                        participants = state.participants,
                        isLoading    = actionLoading,
                        onDismiss    = { showAdd = false },
                        onConfirm    = { winnerId, points, isCapicua ->
                            viewModel.addRoundScore(winnerId, points, isCapicua)
                            showAdd = false
                        }
                    )
                }

                // Undo confirm dialog
                if (showUndo && state.handLog.isNotEmpty()) {
                    UndoConfirmDialog(
                        lastHand  = state.handLog.first(),
                        onDismiss = { showUndo = false },
                        onConfirm = {
                            viewModel.undoLastRound()
                            showUndo = false
                        }
                    )
                }

                // ── Leave game dialog ─────────────────────────────────────
                if (showLeaveDialog) {
                    LeaveGameDialog(
                        onDismiss = { viewModel.dismissLeaveDialog() },
                        onConfirm = { viewModel.confirmLeave(onLeave) }
                    )
                }
            }
        }
    }
}

// ── Top Bar con botón Back ─────────────────────────────────────────────────────

@Composable
private fun ScoreboardTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Botón back visible (también capturado por BackHandler)
        Icon(Icons.Default.ArrowBack, "Back",
            tint     = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp).clickable(onClick = onBackClick))
        Text("DOMINO KINETIC",
            color = MaterialTheme.kineticColors.cyanAccent,
            style = MaterialTheme.typography.titleLarge)
        Icon(Icons.Default.Settings, "Settings",
            tint     = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp))
    }
}

// ── Leave Game Dialog ──────────────────────────────────────────────────────────

@Composable
private fun LeaveGameDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = MaterialTheme.kineticColors

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Icono
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.neonGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ExitToApp, null,
                        tint     = colors.neonGreen,
                        modifier = Modifier.size(32.dp))
                }

                Spacer(Modifier.height(20.dp))

                Text("Leave the match?",
                    color     = MaterialTheme.colorScheme.onSurface,
                    style     = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(10.dp))

                // Badge informativo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("GAME WILL BE PAUSED",
                        color = colors.cyanAccent,
                        style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.height(16.dp))

                Text("Your progress is saved. You can resume this match anytime from the home screen.",
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(28.dp))

                // Botón confirmar salir
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, colors.neonGreen, RoundedCornerShape(14.dp))
                        .clickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ExitToApp, null,
                            tint     = colors.neonGreen,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SAVE & LEAVE",
                            color = colors.neonGreen,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Cancelar
                Text("KEEP PLAYING",
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    style    = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 20.dp, vertical = 8.dp))
            }
        }
    }
}

// ── Participant Score Card ─────────────────────────────────────────────────────

@Composable
private fun ParticipantScoreCard(p: ParticipantScoreUi, modifier: Modifier = Modifier) {
    val colors   = MaterialTheme.kineticColors
    val progress = (p.totalScore.toFloat() / p.targetScore).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (p.isLeading) Modifier.border(
                    2.dp,
                    Brush.verticalGradient(listOf(colors.cyanAccent, colors.cyanAccent.copy(alpha = 0.3f))),
                    RoundedCornerShape(20.dp)
                ) else Modifier
            )
    ) {
        if (p.isLeading) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(progress)
                    .align(Alignment.BottomStart)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, colors.neonGreen)))
            )
        }
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically) {
                Text(p.name.uppercase(),
                    color = if (p.isLeading) colors.cyanAccent
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium)
                if (p.isLeading)
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors.neonGreen))
            }
            Spacer(Modifier.height(8.dp))
            Text(p.totalScore.toString(),
                color = if (p.isLeading) Color.White
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold, fontSize = 64.sp))
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color      = if (p.isLeading) colors.neonGreen
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.height(4.dp))
            Text("GOAL: ${p.targetScore}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ── Hand Log Row ───────────────────────────────────────────────────────────────

@Composable
private fun HandLogRow(hand: HandLogUi) {
    val colors   = MaterialTheme.kineticColors
    val isActive = hand.winType != WinType.BLOCKED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (isActive) Modifier.border(2.dp, colors.neonGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp)) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("R${hand.roundNumber}",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(24.dp))
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                .background(if (isActive) colors.neonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (hand.winType) {
                    WinType.CAPICUA -> Icons.Default.Star
                    WinType.BLOCKED -> Icons.Default.Block
                    else            -> Icons.Default.Person
                },
                contentDescription = null,
                tint = when (hand.winType) {
                    WinType.CAPICUA -> colors.neonGreen
                    WinType.BLOCKED -> MaterialTheme.colorScheme.onSurfaceVariant
                    else            -> colors.cyanAccent
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(hand.winnerName, color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium)
            if (hand.winType != WinType.NORMAL) {
                Text(hand.winType.name,
                    color = if (hand.winType == WinType.CAPICUA) colors.neonGreen
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Text("+${hand.pointsScored}",
            color = if (isActive) colors.neonGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp))
    }
}

// ── Scoreboard Actions ─────────────────────────────────────────────────────────

@Composable
private fun ScoreboardActions(
    canUndo: Boolean, isLoading: Boolean,
    onUndo: () -> Unit, onAddPoints: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    val pulse  = rememberInfiniteTransition(label = "glow")
    val glow   by pulse.animateFloat(0.5f, 1f, label = "glow",
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.weight(0.42f).height(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (canUndo) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .clickable(enabled = canUndo && !isLoading, onClick = onUndo),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Undo, "Undo",
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("UNDO",
                    color = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelLarge)
            }
        }
        Box(
            modifier = Modifier.weight(0.58f).height(58.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp),
                    ambientColor = colors.neonGreen.copy(alpha = glow * 0.4f),
                    spotColor    = colors.neonGreen.copy(alpha = glow))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.neonGreen)
                .clickable(enabled = !isLoading, onClick = onAddPoints),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircleOutline, "Add Points",
                    tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("ADD POINTS", color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Add Points Sheet ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPointsSheet(
    participants: List<ParticipantScoreUi>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (winnerId: Int, points: Int, isCapicua: Boolean) -> Unit
) {
    val colors      = MaterialTheme.kineticColors
    var selectedIdx by remember { mutableIntStateOf(0) }
    var input       by remember { mutableStateOf("") }
    var isCapicua   by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = null,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AWARD POINTS TO:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CAPICUA",
                        color = if (isCapicua) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(6.dp))
                    Switch(checked = isCapicua, onCheckedChange = { isCapicua = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedTrackColor = colors.cyanAccent,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                participants.forEachIndexed { i, p ->
                    val sel = i == selectedIdx
                    Box(
                        modifier = Modifier.weight(1f).height(96.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .then(if (sel) Modifier.border(2.dp, colors.cyanAccent, RoundedCornerShape(18.dp)) else Modifier)
                            .clickable { selectedIdx = i },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Group, null,
                                tint = if (sel) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(p.name.uppercase(),
                                color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
                            Text("${p.totalScore} pts",
                                color = if (sel) colors.neonGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ENTERING HAND VALUE", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(if (input.isEmpty()) "0" else input,
                            color = if (isCapicua) colors.cyanAccent else colors.neonGreen,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 52.sp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isCapicua) "pts ×2" else "pts",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            NumPad(onDigit = { d -> if (input.length < 4) input += d }, onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) })
            Spacer(Modifier.height(20.dp))
            val canConfirm = input.isNotEmpty() && input != "0" && !isLoading
            Box(
                modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (canConfirm) colors.neonGreen else colors.neonGreen.copy(alpha = 0.3f))
                    .clickable(enabled = canConfirm) {
                        onConfirm(participants[selectedIdx].participantId, input.toIntOrNull() ?: 0, isCapicua)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.background, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("CONFIRM POINTS", color = MaterialTheme.colorScheme.background, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ── Num Pad ────────────────────────────────────────────────────────────────────

@Composable
private fun NumPad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val rows = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("DEL","0",""))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    if (key.isEmpty()) { Spacer(Modifier.weight(1f).height(64.dp)); return@forEach }
                    Box(
                        modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (key == "DEL") MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { if (key == "DEL") onDelete() else onDigit(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") Icon(Icons.Default.Backspace, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                        else Text(key, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal))
                    }
                }
            }
        }
    }
}

// ── Undo Dialog ────────────────────────────────────────────────────────────────

@Composable
private fun UndoConfirmDialog(lastHand: HandLogUi, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth(0.88f).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(28.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("Undo the last hand?", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text("(+${lastHand.pointsScored} FOR ${lastHand.winnerName.uppercase()})", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(16.dp))
                Text("This action will revert the score to the previous state. This cannot be undone once confirmed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(14.dp)).clickable(onClick = onConfirm), contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Undo, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("CONFIRM UNDO", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onDismiss).padding(horizontal = 20.dp, vertical = 8.dp))
            }
        }
    }
}