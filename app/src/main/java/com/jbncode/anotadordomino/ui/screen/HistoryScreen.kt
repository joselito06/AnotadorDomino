package com.jbncode.anotadordomino.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.ui.theme.kineticColors
import java.text.SimpleDateFormat
import java.util.*

// ── Placeholder model — reemplazar con HistoryViewModel ───────────────────────

data class MatchHistoryUi(
    val gameId: Int,
    val modality: GameModality,
    val status: GameStatus,
    val startTime: Long,
    val participants: List<ParticipantHistoryUi>
)

data class ParticipantHistoryUi(
    val name: String,
    val score: Int,
    val isWinner: Boolean
)

private val placeholderHistory = listOf(
    MatchHistoryUi(1, GameModality.TEAM, GameStatus.ACTIVE, System.currentTimeMillis() - 3600_000,
        listOf(ParticipantHistoryUi("Team A", 184, true), ParticipantHistoryUi("Team B", 142, false))),
    MatchHistoryUi(2, GameModality.INDIVIDUAL, GameStatus.FINISHED, System.currentTimeMillis() - 2 * 86400_000,
        listOf(ParticipantHistoryUi("Marcus", 200, true), ParticipantHistoryUi("Elena", 165, false))),
    MatchHistoryUi(3, GameModality.TEAM, GameStatus.FINISHED, System.currentTimeMillis() - 4 * 86400_000,
        listOf(ParticipantHistoryUi("Team Alpha", 200, true), ParticipantHistoryUi("Team Beta", 88, false))),
    MatchHistoryUi(4, GameModality.INDIVIDUAL, GameStatus.FINISHED, System.currentTimeMillis() - 6 * 86400_000,
        listOf(ParticipantHistoryUi("P1", 100, true), ParticipantHistoryUi("P2", 95, false), ParticipantHistoryUi("P3", 42, false))),
)

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    onResumeGame: (Int) -> Unit = {}
    // Cuando implementes el ViewModel: viewModel: HistoryViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.kineticColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TopBar
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp))
            Text("DOMINO KINETIC", color = colors.cyanAccent, style = MaterialTheme.typography.titleLarge)
            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp))
        }

        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Column {
                Text("Match History", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.width(40.dp).height(3.dp).background(colors.neonGreen))
            }
            Text("${placeholderHistory.size} SESSIONS", color = colors.neonGreen, style = MaterialTheme.typography.labelMedium)
        }

        Spacer(Modifier.height(16.dp))

        // List
        LazyColumn(
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(placeholderHistory) { _, match ->
                MatchHistoryCard(match = match, onResume = { onResumeGame(match.gameId) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Match History Card ─────────────────────────────────────────────────────────

@Composable
private fun MatchHistoryCard(match: MatchHistoryUi, onResume: () -> Unit) {
    val colors    = MaterialTheme.kineticColors
    val isActive  = match.status == GameStatus.ACTIVE || match.status == GameStatus.PAUSED
    val dateStr   = remember(match.startTime) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(match.startTime))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (isActive) Modifier.border(1.dp, colors.cyanAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp)) else Modifier)
            .padding(16.dp)
    ) {
        Column {
            // Header row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${match.modality.name} • $dateStr",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        generateMatchName(match),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isActive) Color.Transparent
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isActive) colors.cyanAccent else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isActive) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(colors.cyanAccent))
                            Spacer(Modifier.width(5.dp))
                        }
                        Text(
                            if (isActive) "ACTIVE" else "FINISHED",
                            color = if (isActive) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scores
            when {
                match.modality == GameModality.TEAM && match.participants.size == 2 -> {
                    TeamScoreRow(match.participants)
                }
                match.modality == GameModality.INDIVIDUAL -> {
                    IndividualScoreRow(match.participants)
                }
            }

            // Resume button — solo si está activa
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
                        Text("RESUME SESSION",
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.PlayArrow, null,
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamScoreRow(participants: List<ParticipantHistoryUi>) {
    val colors = MaterialTheme.kineticColors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Team A
        Column(Modifier.weight(1f)) {
            Text(participants[0].name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            Text(participants[0].score.toString(),
                color = if (participants[0].isWinner) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                fontSize = 52.sp)
        }
        // Divider
        Box(Modifier.width(1.dp).height(48.dp).background(MaterialTheme.colorScheme.outline))
        // Team B
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(participants[1].name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall)
            Text(participants[1].score.toString(),
                color = if (participants[1].isWinner) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                fontSize = 52.sp)
        }
    }
}

@Composable
private fun IndividualScoreRow(participants: List<ParticipantHistoryUi>) {
    val colors     = MaterialTheme.kineticColors
    val accentList = listOf(colors.neonGreen, Color(0xFF7B61FF), colors.cyanAccent, MaterialTheme.colorScheme.onSurfaceVariant)

    // Winner destacado + runner-ups
    val winner    = participants.firstOrNull { it.isWinner }
    val runnerUps = participants.filter { !it.isWinner }

    if (winner != null && runnerUps.size == 1) {
        // 2 jugadores: layout lado a lado
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ParticipantScoreBox(label = "WINNER", name = winner.name, score = winner.score,
                accent = colors.neonGreen, modifier = Modifier.weight(1f))
            ParticipantScoreBox(label = "RUNNER UP", name = runnerUps[0].name, score = runnerUps[0].score,
                accent = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        }
    } else {
        // 3-4 jugadores: chips en fila
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            participants.forEachIndexed { i, p ->
                PlayerChip(index = i + 1, name = p.name, score = p.score,
                    accent = accentList.getOrElse(i) { accentList.last() })
            }
        }
    }
}

@Composable
private fun ParticipantScoreBox(label: String, name: String, score: Int, accent: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
        Column {
            Text(label, color = accent, style = MaterialTheme.typography.labelSmall)
            Text(name,  color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(score.toString(), color = accent, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        }
    }
}

@Composable
private fun PlayerChip(index: Int, name: String, score: Int, accent: Color) {
    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("P$index", color = accent, style = MaterialTheme.typography.labelSmall)
            Text(score.toString(), color = if (index == 1) accent else Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        }
    }
}

private fun generateMatchName(match: MatchHistoryUi): String {
    val names = listOf("The Champions League","Friday Night Rumble","Beach House Series","Quick Game","Kinetic Classic","Sunday Showdown")
    return names[match.gameId % names.size]
}

// Extension para remember con key
@Composable
private fun <T> remember(key: Any?, calculation: () -> T): T =
    androidx.compose.runtime.remember(key) { calculation() }