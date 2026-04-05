package com.jbncode.anotadordomino.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.ui.components.KineticTopBar
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.SettingsViewModel

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val colors      = MaterialTheme.kineticColors
    val scrollState = rememberScrollState()

    // Collect all settings state
    val isDarkMode     by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val capicuaEnabled by viewModel.capicuaEnabled.collectAsStateWithLifecycle()
    val tranqueEnabled by viewModel.tranqueEnabled.collectAsStateWithLifecycle()
    val isResetting    by viewModel.isResetting.collectAsStateWithLifecycle()
    val resetSuccess   by viewModel.resetSuccess.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }

    // Snackbar cuando el reset termina
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(resetSuccess) {
        if (resetSuccess) {
            snackbarHostState.showSnackbar("All data deleted successfully")
            viewModel.resetSuccessAcknowledged()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp)
        ) {
            KineticTopBar(
                onSettingsClick = {},
                showBack        = true,
                onBackClick     = onBackClick,
                title           = "SETTINGS"
            )

            // ── Profile card ──────────────────────────────────────────────
            ProfileCard()

            Spacer(Modifier.height(24.dp))

            // ── GENERAL RULES ─────────────────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Gavel, label = "GENERAL RULES")
            SettingsCard {
                SettingsToggleRow(
                    title       = "Capicua Points",
                    description = "Earn double points when the match ends with the same tile on both ends",
                    checked     = capicuaEnabled,
                    onToggle    = { viewModel.setCapicuaPoints(it) }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title       = "Tranque Penalties",
                    description = "Automatic point deduction for causing a locked board state",
                    checked     = tranqueEnabled,
                    onToggle    = { viewModel.setDoubleTranque(it) },
                    accentColor = MaterialTheme.kineticColors.cyanAccent
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── VISUALS ───────────────────────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Palette, label = "VISUALS",
                iconTint = Color(0xFF7B61FF))
            SettingsCard {
                // Dark / Light mode
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Dark / Light Mode",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Optimize for night-time kinetic precision",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface).padding(4.dp)
                    ) {
                        Row {
                            ThemeIcon(Icons.Default.DarkMode, selected = isDarkMode,
                                onClick = { viewModel.setDarkMode(true) })
                            ThemeIcon(Icons.Default.LightMode, selected = !isDarkMode,
                                onClick = { viewModel.setDarkMode(false) })
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
                        Text("Themes & Tile Skins",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Row {
                            Text("Active: ", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall)
                            Text("Obsidian Neon", color = MaterialTheme.kineticColors.cyanAccent,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Icon(Icons.Default.Palette, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── DATA MANAGEMENT ───────────────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Storage, label = "DATA MANAGEMENT",
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant)
            SettingsCard {
                // Export (placeholder — puedes implementar con CSV writer)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                        .clickable { /* TODO: export CSV */ },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Export Match History",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Download all rounds in CSV format",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.Download, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp))
                }
                SettingsDivider()
                // Reset — destructivo
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                        .clickable(enabled = !isResetting) { showResetDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Reset Local Data",
                            color = if (!isResetting) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Irreversible. Deletes all local match history.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (isResetting) {
                        CircularProgressIndicator(
                            color    = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Box(
                            Modifier.size(30.dp).clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DeleteForever, null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Footer
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DOMINO KINETIC",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 4.sp))
                Spacer(Modifier.height(4.dp))
                Text("VERSION 2.4.0-KINETIC-DELTA",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    style = MaterialTheme.typography.labelSmall)
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── Reset confirmation dialog ──────────────────────────────────────────
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest  = { showResetDialog = false },
            containerColor    = MaterialTheme.colorScheme.surfaceVariant,
            icon              = {
                Icon(Icons.Default.DeleteForever, null,
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Reset All Data?",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            },
            text  = {
                Text(
                    "This will permanently delete ALL match history, scores and rounds. This action cannot be undone.",
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.resetAllData()
                }) {
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

// ── Profile Card ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileCard() {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(68.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = colors.cyanAccent,
                    modifier = Modifier.size(36.dp))
                Box(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(topEnd = 8.dp))
                        .background(colors.neonGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("PRO", color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp, fontWeight = FontWeight.ExtraBold))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Domino Kinetic", color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text("LOCAL PLAYER", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(colors.neonGreen, MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant).forEach { c ->
                        Box(Modifier.size(8.dp).clip(CircleShape).background(c))
                    }
                }
            }
        }
    }
}

// ── Settings components ────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(icon: ImageVector, label: String,
                                  iconTint: Color = MaterialTheme.kineticColors.neonGreen) {
    Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = iconTint, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
}

@Composable
private fun SettingsToggleRow(
    title: String, description: String, checked: Boolean,
    onToggle: (Boolean) -> Unit,
    accentColor: Color = MaterialTheme.kineticColors.neonGreen
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
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
private fun ThemeIcon(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null,
            tint     = if (selected) colors.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp))
    }
}