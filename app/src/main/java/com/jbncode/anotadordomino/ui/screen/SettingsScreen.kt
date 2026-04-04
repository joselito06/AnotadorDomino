package com.jbncode.anotadordomino.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.ui.components.KineticTopBar
import com.jbncode.anotadordomino.ui.theme.kineticColors

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    val colors      = MaterialTheme.kineticColors
    val scrollState = rememberScrollState()

    var capicuaPoints by remember { mutableStateOf(true) }
    var cloudSync     by remember { mutableStateOf(true) }
    var darkMode      by remember { mutableStateOf(isDarkTheme) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 40.dp)
    ) {
        // TopBar con back
        KineticTopBar(
            onSettingsClick = {},
            showBack        = true,
            onBackClick     = onBackClick,
            title           = "SETTINGS"
        )

        // Profile card
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier.size(68.dp).clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null,
                        tint = colors.cyanAccent, modifier = Modifier.size(36.dp))
                    // PRO badge
                    Box(
                        modifier = Modifier.align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(topEnd = 8.dp))
                            .background(colors.neonGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PRO", color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Pro Player", color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text("LEVEL 42 DOMINATOR", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text("• RANK #12", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(colors.neonGreen, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant).forEach { c ->
                            Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(c))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // GENERAL RULES section
        SettingsSectionHeader(icon = Icons.Default.Gavel, label = "GENERAL RULES")
        SettingsCard {
            SettingsToggleRow(
                title       = "Capicua Points",
                description = "Earn double points if the match ends with the same tile on both ends",
                checked     = capicuaPoints,
                onToggle    = { capicuaPoints = it }
            )
            SettingsDivider()
            SettingsArrowRow(
                title       = "Tranque Penalties",
                description = "Automatic points deduction for causing a locked board state"
            )
            SettingsDivider()
            SettingsActionRow(
                title       = "Winning Score Threshold",
                description = "Current Target: 200 Points",
                actionLabel = "ADJUST",
                accentDescription = true
            )
        }

        Spacer(Modifier.height(20.dp))

        // VISUALS section
        SettingsSectionHeader(icon = Icons.Default.Palette, label = "VISUALS", iconTint = Color(0xFF7B61FF))
        SettingsCard {
            // Dark / Light mode toggle custom
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Dark / Light Mode", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Optimize for night-time kinetic precision",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.width(12.dp))
                // Custom toggle con iconos luna/sol
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp)
                ) {
                    Row {
                        ThemeIcon(icon = Icons.Default.DarkMode, selected = darkMode,
                            onClick = { darkMode = true; onThemeToggle(true) })
                        ThemeIcon(icon = Icons.Default.LightMode, selected = !darkMode,
                            onClick = { darkMode = false; onThemeToggle(false) })
                    }
                }
            }
            SettingsDivider()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Themes & Tile Skins", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Row {
                        Text("Active: ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text("Obsidian Neon", color = MaterialTheme.kineticColors.cyanAccent, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Icon(Icons.Default.Palette, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        // DATA MANAGEMENT section
        SettingsSectionHeader(icon = Icons.Default.Storage, label = "DATA MANAGEMENT", iconTint = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsCard {
            SettingsToggleRow(
                title       = "Cloud Sync",
                description = "Last synced 2 minutes ago",
                checked     = cloudSync,
                onToggle    = { cloudSync = it },
                accentColor = MaterialTheme.kineticColors.cyanAccent
            )
            SettingsDivider()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).clickable { },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Export Match History", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Download all rounds in CSV format",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
            SettingsDivider()
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).clickable { showResetDialog = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Reset Local Data", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Irreversible action. Deletes local match history.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Footer
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DOMINO KINETIC", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp))
            Spacer(Modifier.height(4.dp))
            Text("VERSION 2.4.0-KINETIC-DELTA", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                style = MaterialTheme.typography.labelSmall)
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor   = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Reset Local Data?", color = Color.White) },
            text  = { Text("This will permanently delete all local match history. This cannot be undone.",
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("RESET", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

// ── Settings components ────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(icon: ImageVector, label: String, iconTint: Color = MaterialTheme.kineticColors.neonGreen) {
    Row(
        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = iconTint, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
}

@Composable
private fun SettingsToggleRow(
    title: String, description: String, checked: Boolean,
    onToggle: (Boolean) -> Unit, accentColor: Color = MaterialTheme.kineticColors.neonGreen
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = MaterialTheme.colorScheme.background,
                checkedTrackColor   = accentColor,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface
            ))
    }
}

@Composable
private fun SettingsArrowRow(title: String, description: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).clickable { },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsActionRow(title: String, description: String, actionLabel: String, accentDescription: Boolean = false) {
    val colors = MaterialTheme.kineticColors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            if (accentDescription) {
                Row {
                    Text("Current Target: ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("200 Points", color = colors.neonGreen, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 14.dp, vertical = 7.dp)) {
            Text(actionLabel, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ThemeIcon(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null,
            tint = if (selected) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp))
    }
}