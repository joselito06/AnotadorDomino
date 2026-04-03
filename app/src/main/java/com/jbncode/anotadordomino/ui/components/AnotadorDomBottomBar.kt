package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jbncode.anotadordomino.ui.Screen
import com.jbncode.anotadordomino.ui.theme.kineticColors

/**
 * Barra de navegación inferior de la app.
 * Se muestra en Home y Stats. Se oculta en Scoreboard (ver DominoComposeApp).
 *
 * @param currentRoute  Ruta activa para resaltar el ítem correcto.
 * @param onHistoryClick  Navega a Home (historial).
 * @param onPlayClick     Abre el Setup para nueva partida.
 * @param onStatsClick    Navega a Stats.
 */
@Composable
fun AnotadorDomBottomBar(
    currentRoute: String? = null,
    onHistoryClick: () -> Unit,
    onPlayClick: () -> Unit,
    onStatsClick: () -> Unit,
) {
    val colors = MaterialTheme.kineticColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.97f)
                    )
                )
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── History ───────────────────────────────────────────────────
            BottomNavItem(
                icon     = Icons.Default.History,
                label    = "HISTORY",
                selected = currentRoute == Screen.History.route,
                onClick  = onHistoryClick
            )

            // ── FAB central (Play) ────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.neonGreen)
                        .clickable(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Play",
                        tint     = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = "PLAY",
                    color = colors.neonGreen,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // ── Stats ─────────────────────────────────────────────────────
            BottomNavItem(
                icon     = Icons.Default.BarChart,
                label    = "STATS",
                selected = currentRoute == Screen.Stats.route,
                onClick  = onStatsClick
            )
        }
    }
}

// ── Item genérico ──────────────────────────────────────────────────────────────

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.kineticColors
    val tint   = if (selected) colors.cyanAccent
    else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(text = label, color = tint, style = MaterialTheme.typography.labelSmall)
    }
}