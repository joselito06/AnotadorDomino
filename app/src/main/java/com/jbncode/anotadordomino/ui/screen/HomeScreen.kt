package com.jbncode.anotadordomino.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.jbncode.anotadordomino.ui.components.RecruitPlayerDialog
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.PlayerUiState

// ── Modelo de UI (solo para preview/placeholder) ───────────────────────────────
// Cuando conectes el ViewModel, reemplaza samplePlayers por el StateFlow real.

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * Pantalla de inicio (Historial).
 *
 * @param onNavigateToGame  Callback que recibe el gameId y navega al Scoreboard.
 *                          Lo llama [DominoComposeApp] desde el NavHost.
 * @param onNewPlayer       Abre el dialog de "Recruit New Player"
 *                          (manejado internamente por defecto).
 */
@Composable
fun HomeScreen(
    onNavigateToGame: (Int) -> Unit = {},
    onNewPlayer: () -> Unit = {}
) {
    val colors       = MaterialTheme.kineticColors
    var selectedMode by remember { mutableStateOf(0) }      // 0=Pairs 1=Individual
    var pointGoal    by remember { mutableFloatStateOf(200f) }
    var showDialog   by remember { mutableStateOf(false) }
    val scrollState  = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            TopBar()
            SeasonBanner()
            Spacer(Modifier.height(24.dp))

            // ── Game mode ─────────────────────────────────────────────────
            HomeSectionLabel("GAME MODE", Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(10.dp))
            GameModeToggle(
                selected = selectedMode,
                onSelect = { selectedMode = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(24.dp))

            // ── Point goal ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeSectionLabel("POINT GOAL")
                Text(
                    text  = pointGoal.toInt().toString(),
                    color = colors.neonGreen,
                    style = MaterialTheme.typography.displaySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            PointGoalSlider(
                value         = pointGoal,
                onValueChange = { pointGoal = it },
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(28.dp))

            // ── Quick add players ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeSectionLabel("QUICK ADD PLAYERS")
                HomeNewButton(onClick = { showDialog = true })
            }
            Spacer(Modifier.height(12.dp))
            PlayersGrid(
                players  = emptyList<PlayerUiState>(),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(24.dp))

            // ── Start match ───────────────────────────────────────────────
            // Por ahora navega con gameId=0 como placeholder.
            // Cuando el SetupScreen cree la partida real pasará el ID correcto.
            StartMatchButton(
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick  = { onNavigateToGame(0) }
            )
        }

        // ── Recruit dialog ────────────────────────────────────────────────
        if (showDialog) {
            RecruitPlayerDialog(onDismiss = { showDialog = false })
        }
    }
}

// ── Top Bar ────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar() {
    val colors = MaterialTheme.kineticColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Menu,
            contentDescription = "Menu",
            tint     = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text  = "DOMINO KINETIC",
            color = colors.cyanAccent,
            style = MaterialTheme.typography.titleLarge
        )
        Icon(
            Icons.Default.Settings,
            contentDescription = "Settings",
            tint     = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(28.dp)
        )
    }
}

// ── Season Banner ──────────────────────────────────────────────────────────────

@Composable
private fun SeasonBanner() {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0D2B2B), Color(0xFF0A1A1A), Color(0xFF061515))
                )
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0E4040).copy(alpha = 0.6f), Color.Transparent),
                        radius = 500f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text  = "CURRENT SEASON",
                color = colors.neonGreen,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "KINETIC MASTERS",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

// ── Section label ──────────────────────────────────────────────────────────────

@Composable
private fun HomeSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        style    = MaterialTheme.typography.labelMedium,
        modifier = modifier
    )
}

// ── Game Mode Toggle ───────────────────────────────────────────────────────────

@Composable
private fun GameModeToggle(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors  = MaterialTheme.kineticColors
    val options = listOf(
        Icons.Default.Group  to "PAIRS",
        Icons.Default.Person to "INDIVIDUAL"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, (icon, label) ->
                val isSelected = index == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (isSelected) colors.cyanAccent else Color.Transparent
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (isSelected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = label,
                            color = if (isSelected) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// ── Point Goal Slider ──────────────────────────────────────────────────────────

@Composable
private fun PointGoalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = 50f..500f,
            colors        = SliderDefaults.colors(
                thumbColor         = colors.neonGreen,
                activeTrackColor   = colors.neonGreen,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("50", "100", "200", "500").forEach { mark ->
                Text(
                    text  = mark,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ── New Button ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeNewButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Add,
                contentDescription = "New",
                tint     = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text  = "NEW",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

// ── Players Grid ───────────────────────────────────────────────────────────────

@Composable
private fun PlayersGrid(players: List<PlayerUiState>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        players.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { player ->
                    PlayerCard(player = player, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PlayerCard(player: PlayerUiState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.kineticColors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.cyanAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint     = colors.cyanAccent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text  = player.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text  = "WINS: ${player.wins}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ── Start Match Button ─────────────────────────────────────────────────────────

@Composable
private fun StartMatchButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val colors = MaterialTheme.kineticColors
    val pulse  = rememberInfiniteTransition(label = "glow")
    val glowAlpha by pulse.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        label         = "glowAlpha",
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation    = 20.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = colors.neonGreen.copy(alpha = glowAlpha * 0.4f),
                spotColor    = colors.neonGreen.copy(alpha = glowAlpha)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(colors.neonGreen)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = "START MATCH",
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.FlashOn,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}