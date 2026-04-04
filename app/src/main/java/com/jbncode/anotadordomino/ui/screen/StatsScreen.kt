package com.jbncode.anotadordomino.ui.screen

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.jbncode.anotadordomino.ui.theme.kineticColors
import kotlin.math.cos
import kotlin.math.sin

// Datos placeholder — reemplazar con StatsViewModel cuando lo implementes
private data class StatCard(val icon: ImageVector, val label: String, val value: String, val unit: String, val accentColor: Color)

@Composable
fun StatsScreen() {
    val colors = MaterialTheme.kineticColors
    val scrollState = rememberScrollState()

    val statCards = listOf(
        StatCard(Icons.Default.TrendingUp,  "RECORD HIGH", "98",  "PTS", Color(0xFF7B61FF)),
        StatCard(Icons.Default.Timer,        "AVG TIME",    "14:22","MIN", colors.neonGreen),
        StatCard(Icons.Default.Star,         "SHUTOUTS",    "12",   "WIN", colors.cyanAccent),
        StatCard(Icons.Default.GridOn,       "DOUBLE-6",    "318",  "X",  Color(0xFFFF5C5C)),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
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

        // Performance core card
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("PERFORMANCE CORE", color = colors.neonGreen, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(20.dp))

                // Circular progress
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                    CircularStatIndicator(progress = 0.72f, size = 160.dp, strokeWidth = 14.dp, color = colors.neonGreen)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("72%", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, fontFamily = MaterialTheme.typography.displayLarge.fontFamily)
                        Text("EFFICIENCY", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("WIN RATE", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("Dominating the table with precision kinetic movements across 124 professional sessions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Current level card
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("CURRENT LEVEL", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("The King of Capicúa", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("42", color = colors.cyanAccent, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(Modifier.width(4.dp))
                        Text("STRIKES", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.cyanAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MilitaryTech, null, tint = colors.cyanAccent, modifier = Modifier.size(28.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2x2 stat cards grid
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            statCards.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { card ->
                        StatMiniCard(card = card, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 7-day momentum
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Text("7-DAY MOMENTUM", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(16.dp))
                MomentumBar()
            }
        }

        Spacer(Modifier.height(16.dp))

        // Next milestone
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFD4A800).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFD4A800), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("NEXT MILESTONE", color = colors.neonGreen, style = MaterialTheme.typography.labelSmall)
                    Text("Capicúa Master II", color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text("Reach 50 strikes to unlock the Neon Slate Interface.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ── Circular indicator ─────────────────────────────────────────────────────────

@Composable
private fun CircularStatIndicator(progress: Float, size: Dp, strokeWidth: Dp, color: Color) {
    val trackColor = MaterialTheme.colorScheme.surface
    Canvas(modifier = Modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val sweepAngle = 270f * progress
        // Track
        drawArc(color = trackColor, startAngle = 135f, sweepAngle = 270f, useCenter = false, style = stroke)
        // Progress
        drawArc(color = color, startAngle = 135f, sweepAngle = sweepAngle, useCenter = false, style = stroke)
    }
}

// ── Mini stat card ─────────────────────────────────────────────────────────────

@Composable
private fun StatMiniCard(card: StatCard, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(card.icon, null, tint = card.accentColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(card.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(card.value, color = Color.White, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold))
                Spacer(Modifier.width(4.dp))
                Text(card.unit, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(8.dp))
            // Accent bar
            Box(Modifier.fillMaxWidth(0.6f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(card.accentColor))
        }
    }
}

// ── Momentum bar chart ─────────────────────────────────────────────────────────

@Composable
private fun MomentumBar() {
    val colors = MaterialTheme.kineticColors
    val days   = listOf("M","T","W","T","TODAY","S","S")
    // Heights relativas (0f-1f)
    val values = listOf(0.5f, 0.8f, 0.65f, 0.4f, 0.9f, 0.3f, 0.45f)
    val barColors = listOf(
        colors.neonGreen, Color(0xFF7B61FF), colors.neonGreen, colors.neonGreen,
        colors.cyanAccent, colors.neonGreen, colors.neonGreen
    )
    val todayIdx = 4

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { i, day ->
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
                            .fillMaxHeight(values[i])
                            .clip(RoundedCornerShape(16.dp))
                            .background(barColors[i].copy(alpha = if (i == todayIdx) 1f else 0.7f))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(day,
                    color = if (i == todayIdx) colors.neonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (i == todayIdx) FontWeight.Bold else FontWeight.Normal
                    ))
            }
        }
    }
}