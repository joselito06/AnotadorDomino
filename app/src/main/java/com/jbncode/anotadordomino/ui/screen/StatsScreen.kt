package com.jbncode.anotadordomino.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.domain.model.GameStats
import com.jbncode.anotadordomino.ui.components.KineticTopBar
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.StatsUiState
import com.jbncode.anotadordomino.ui.viewmodel.StatsViewModel

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun StatsScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: StatsViewModel   = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val colors       = MaterialTheme.kineticColors
    val scrollState  = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
    ) {
        KineticTopBar(onSettingsClick = onSettingsClick)

        when (val state = uiState) {

            is StatsUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.neonGreen)
                }
            }

            is StatsUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BarChart, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No stats yet",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Play your first match and your stats will appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center)
                    }
                }
            }

            is StatsUiState.Success -> {
                StatsContent(stats = state.stats)
            }
        }
    }
}

// ── Stats content ──────────────────────────────────────────────────────────────

@Composable
private fun StatsContent(stats: GameStats) {
    val colors = MaterialTheme.kineticColors

    // ── Performance core ──────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("PERFORMANCE CORE", color = colors.neonGreen, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(20.dp))

            // Circular progress con el win rate real
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                CircularStatIndicator(
                    progress    = stats.winRatePercent / 100f,
                    size        = 160.dp,
                    strokeWidth = 14.dp,
                    color       = colors.neonGreen
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "${stats.winRatePercent}%",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize   = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = MaterialTheme.typography.displayLarge.fontFamily
                    )
                    Text("EFFICIENCY", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("WIN RATE", color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Dominating the table with precision kinetic movements across ${stats.totalGames} professional sessions.",
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // ── Current level card ─────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("CURRENT LEVEL", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                // Nivel dinámico basado en partidas jugadas
                Text(
                    text  = getLevelTitle(stats.totalGames),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${stats.capicuaCount}",
                        color = colors.cyanAccent,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.width(4.dp))
                    Text("CAPICÚAS", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                    .background(colors.cyanAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MilitaryTech, null, tint = colors.cyanAccent,
                    modifier = Modifier.size(28.dp))
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // ── 2×2 Stat cards ────────────────────────────────────────────────────
    val statCards = listOf(
        StatCardData(Icons.Default.TrendingUp, "RECORD HIGH",
            stats.recordHighScore.toString(), "PTS", Color(0xFF7B61FF)),
        StatCardData(Icons.Default.Casino, "TOTAL ROUNDS",
            stats.totalRounds.toString(), "RDS", MaterialTheme.kineticColors.neonGreen),
        StatCardData(Icons.Default.Star, "CAPICÚAS",
            stats.capicuaCount.toString(), "WIN", MaterialTheme.kineticColors.cyanAccent),
        StatCardData(Icons.Default.Block, "TRANQUES",
            stats.blockedCount.toString(), "X", Color(0xFFFF5C5C)),
    )

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        statCards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { card ->
                    StatMiniCard(card = card, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    // ── 7-day momentum ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column {
            Text("7-DAY MOMENTUM", color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(16.dp))
            MomentumBar(activity = stats.last7DaysActivity)
        }
    }

    Spacer(Modifier.height(16.dp))

    // ── Next milestone ─────────────────────────────────────────────────────
    val (milestoneTitle, milestoneDesc) = getNextMilestone(stats.totalGames)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD4A800).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFD4A800),
                    modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("NEXT MILESTONE", color = MaterialTheme.kineticColors.neonGreen,
                    style = MaterialTheme.typography.labelSmall)
                Text(milestoneTitle, color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleSmall)
                Text(milestoneDesc, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun getLevelTitle(games: Int): String = when {
    games == 0  -> "Novice Challenger"
    games < 5   -> "Apprentice Kinetic"
    games < 15  -> "Domino Warrior"
    games < 30  -> "The King of Capicúa"
    games < 50  -> "Kinetic Master"
    else        -> "Grandmaster Kinetic"
}

private fun getNextMilestone(games: Int): Pair<String, String> = when {
    games < 5  -> "Apprentice Kinetic"  to "Play 5 matches to unlock this rank."
    games < 15 -> "Domino Warrior"      to "Play ${15 - games} more matches to rank up."
    games < 30 -> "King of Capicúa"     to "Play ${30 - games} more matches to rank up."
    games < 50 -> "Kinetic Master"      to "Play ${50 - games} more matches to rank up."
    else       -> "Grandmaster ✓"       to "You have reached the highest rank!"
}

// ── Stat card data ─────────────────────────────────────────────────────────────

private data class StatCardData(
    val icon: ImageVector, val label: String,
    val value: String, val unit: String, val accentColor: Color
)

// ── Circular indicator ─────────────────────────────────────────────────────────

@Composable
private fun CircularStatIndicator(progress: Float, size: Dp, strokeWidth: Dp, color: Color) {
    val trackColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = Modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(color = trackColor, startAngle = 135f, sweepAngle = 270f,
            useCenter = false, style = stroke)
        drawArc(color = color, startAngle = 135f, sweepAngle = 270f * progress.coerceIn(0f, 1f),
            useCenter = false, style = stroke)
    }
}

// ── Mini stat card ─────────────────────────────────────────────────────────────

@Composable
private fun StatMiniCard(card: StatCardData, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(card.icon, null, tint = card.accentColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(card.label, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(card.value, color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold))
                Spacer(Modifier.width(4.dp))
                Text(card.unit, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.6f).height(3.dp)
                .clip(RoundedCornerShape(2.dp)).background(card.accentColor))
        }
    }
}

// ── Momentum bar chart ─────────────────────────────────────────────────────────

@Composable
private fun MomentumBar(activity: List<Int>) {
    val colors   = MaterialTheme.kineticColors
    val days     = listOf("M", "T", "W", "T", "T", "S", "S")
    val maxValue = activity.maxOrNull()?.coerceAtLeast(1) ?: 1
    val barColors = listOf(
        colors.neonGreen, Color(0xFF7B61FF), colors.neonGreen,
        colors.neonGreen, colors.cyanAccent, colors.neonGreen, colors.neonGreen
    )

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        activity.forEachIndexed { i, count ->
            val isTodayIdx = i == 6
            val fraction   = (count.toFloat() / maxValue).coerceIn(0.05f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(16.dp))
                            .background(barColors[i].copy(alpha = if (isTodayIdx) 1f else 0.7f))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = if (isTodayIdx) "TODAY" else days[i],
                    color = if (isTodayIdx) colors.neonGreen
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isTodayIdx) FontWeight.Bold else FontWeight.Normal,
                        fontSize   = if (isTodayIdx) 8.sp else 10.sp
                    )
                )
            }
        }
    }
}