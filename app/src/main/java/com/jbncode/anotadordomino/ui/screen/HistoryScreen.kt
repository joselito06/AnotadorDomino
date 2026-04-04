package com.jbncode.anotadordomino.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.ui.components.KineticTopBar
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.HistoryUiState
import com.jbncode.anotadordomino.ui.viewmodel.HistoryViewModel
import com.jbncode.anotadordomino.ui.viewmodel.MatchHistoryUi
import com.jbncode.anotadordomino.ui.viewmodel.ParticipantHistoryUi

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    onSettingsClick: () -> Unit = {},
    onResumeGame: (Int) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors  = MaterialTheme.kineticColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        KineticTopBar(onSettingsClick = onSettingsClick)

        // Header con título y contador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Column {
                Text(
                    text  = "Match History",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(colors.neonGreen)
                )
            }
            if (uiState is HistoryUiState.Success) {
                val count = (uiState as HistoryUiState.Success).matches.size
                Text(
                    text  = "$count SESSIONS",
                    color = colors.neonGreen,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Contenido según estado
        when (val state = uiState) {

            is HistoryUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.neonGreen)
                }
            }

            is HistoryUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SportsScore,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text      = "No matches yet",
                            color     = MaterialTheme.colorScheme.onSurface,
                            style     = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text      = "Start a new game and your match history will appear here.",
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            style     = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is HistoryUiState.Success -> {
                LazyColumn(
                    contentPadding      = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 90.dp        // espacio para el BottomBar
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.matches,
                        key   = { it.gameId }
                    ) { match ->
                        MatchHistoryCard(
                            match    = match,
                            onResume = { onResumeGame(match.gameId) }
                        )
                    }
                }
            }
        }
    }
}

// ── Match History Card ─────────────────────────────────────────────────────────

@Composable
private fun MatchHistoryCard(match: MatchHistoryUi, onResume: () -> Unit) {
    val colors   = MaterialTheme.kineticColors
    val isActive = match.status == GameStatus.ACTIVE || match.status == GameStatus.PAUSED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (isActive) Modifier.border(
                    1.dp,
                    colors.cyanAccent.copy(alpha = 0.5f),
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .padding(16.dp)
    ) {
        Column {

            // ── Header ────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text  = "${match.modality.name} • ${match.dateLabel}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Game #${match.gameId}",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Badge de estado
                StatusBadge(status = match.status, isActive = isActive)
            }

            Spacer(Modifier.height(16.dp))

            // ── Scores según modalidad ────────────────────────────────────
            when (match.modality) {
                GameModality.TEAM       -> TeamScoreRow(match.participants)
                GameModality.INDIVIDUAL -> IndividualScoreRow(match.participants, match.status)
            }

            // ── Botón Resume (solo partidas activas/pausadas) ─────────────
            if (isActive) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.neonGreen)
                        .clickable(onClick = onResume),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text  = "RESUME SESSION",
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PlayArrow, null,
                            tint     = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Status Badge ───────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(status: GameStatus, isActive: Boolean) {
    val colors = MaterialTheme.kineticColors
    val (label, borderColor, textColor) = when {
        isActive                       -> Triple("ACTIVE",   colors.cyanAccent,                           colors.cyanAccent)
        status == GameStatus.FINISHED  -> Triple("FINISHED", MaterialTheme.colorScheme.outline,           MaterialTheme.colorScheme.onSurfaceVariant)
        else                           -> Triple("PAUSED",   colors.neonGreen.copy(alpha = 0.5f),         colors.neonGreen)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(colors.cyanAccent))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, color = textColor, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ── Team Score Row ─────────────────────────────────────────────────────────────

@Composable
private fun TeamScoreRow(participants: List<ParticipantHistoryUi>) {
    if (participants.size < 2) return

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Equipo A (siempre el de seatOrder=1)
        Column(Modifier.weight(1f)) {
            Text(
                text  = participants[0].name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text  = participants[0].score.toString(),
                color = if (participants[0].isWinner) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 52.sp
                )
            )
        }

        // Divisor central
        Box(
            Modifier
                .width(1.dp)
                .height(48.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        // Equipo B
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(
                text  = participants[1].name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text  = participants[1].score.toString(),
                color = if (participants[1].isWinner) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 52.sp
                )
            )
        }
    }
}

// ── Individual Score Row ───────────────────────────────────────────────────────

@Composable
private fun IndividualScoreRow(
    participants: List<ParticipantHistoryUi>,
    status: GameStatus
) {
    val colors     = MaterialTheme.kineticColors
    val accentList = listOf(
        colors.neonGreen,
        Color(0xFF7B61FF),
        colors.cyanAccent,
        MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (participants.size == 2) {
        // 2 jugadores: winner vs runner-up lado a lado
        val winner   = participants.maxByOrNull { it.score }
        val runnerUp = participants.minByOrNull { it.score }
        if (winner != null && runnerUp != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ParticipantBox(
                    label  = if (status == GameStatus.FINISHED) "WINNER" else "LEADING",
                    name   = winner.name,
                    score  = winner.score,
                    accent = colors.neonGreen,
                    modifier = Modifier.weight(1f)
                )
                ParticipantBox(
                    label  = "RUNNER UP",
                    name   = runnerUp.name,
                    score  = runnerUp.score,
                    accent = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        // 3-4 jugadores: chips ordenados por score
        val sorted = participants.sortedByDescending { it.score }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sorted.forEachIndexed { i, p ->
                PlayerChip(
                    rank   = i + 1,
                    name   = p.name,
                    score  = p.score,
                    accent = accentList.getOrElse(i) { accentList.last() }
                )
            }
        }
    }
}

@Composable
private fun ParticipantBox(
    label: String, name: String, score: Int, accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = accent, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(2.dp))
            Text(name, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(score.toString(), color = accent,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        }
    }
}

@Composable
private fun PlayerChip(rank: Int, name: String, score: Int, accent: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "#$rank ${name.take(6)}",
                color = accent,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text  = score.toString(),
                color = if (rank == 1) accent else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}