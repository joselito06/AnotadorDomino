package com.jbncode.anotadordomino.ui.screen

import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.domain.model.supportedLanguages
import com.jbncode.anotadordomino.ui.components.KineticTopBar
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.SettingsViewModel

// ── Screen ─────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    //val colors      = MaterialTheme.kineticColors
    val scrollState = rememberScrollState()

    // Collect all settings state
    val isDarkMode     by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val capicuaEnabled by viewModel.capicuaEnabled.collectAsStateWithLifecycle()
    val tranqueEnabled by viewModel.tranqueEnabled.collectAsStateWithLifecycle()
    val isResetting    by viewModel.isResetting.collectAsStateWithLifecycle()
    val resetSuccess   by viewModel.resetSuccess.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val activeLangName = supportedLanguages.find { it.tag == appLanguage }?.nativeName ?: "English"

    // Snackbar cuando el reset termina
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(resetSuccess) {
        if (resetSuccess) {
            snackbarHostState.showSnackbar("All data deleted successfully")
            viewModel.resetSuccessAcknowledged()
        }
    }

    /*LaunchedEffect(viewModel) {
        viewModel.onNavigateToRestartScreen = {
            onRestarting()
        }
    }*/

    Scaffold(
        topBar = {
            KineticTopBar(
                onSettingsClick = {},
                showBack        = true,
                onBackClick     = onBackClick,
                title           = stringResource(R.string.settings_title)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                //.background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp)
        ) {

            // ── Profile card ──────────────────────────────────────────────
            ProfileCard()

            Spacer(Modifier.height(24.dp))

            // ── GENERAL RULES ─────────────────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Gavel, label = stringResource(R.string.settings_general_rules))
            SettingsCard {
                SettingsToggleRow(
                    title       = stringResource(R.string.settings_capicua_points),
                    description = stringResource(R.string.settings_capicua_desc),
                    checked     = capicuaEnabled,
                    onToggle    = { viewModel.setCapicuaPoints(it) }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title       = stringResource(R.string.settings_tranque_penalties),
                    description = stringResource(R.string.settings_tranque_desc),
                    checked     = tranqueEnabled,
                    onToggle    = { viewModel.setDoubleTranque(it) },
                    accentColor = MaterialTheme.kineticColors.cyanAccent
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── VISUALS ───────────────────────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Palette, label = stringResource(R.string.settings_appearance),
                iconTint = Color(0xFF7B61FF))
            SettingsCard {
                // Dark / Light mode
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_dark_mode),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.settings_dark_mode_desc),
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
                        Text(stringResource(R.string.settings_themes),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Row {
                            Text(stringResource(R.string.settings_themes_active), color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            // ── LOCALIZATION (ESCALABLE) ──────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Translate, label = stringResource(R.string.settings_localization),
                iconTint = MaterialTheme.kineticColors.cyanAccent)

            SettingsCard {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true } // Abre el diálogo
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_language),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))

                        // Muestra el idioma actualmente seleccionado en cyan
                        Text(activeLangName,
                            color = MaterialTheme.kineticColors.cyanAccent,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.ChevronRight, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── DATA MANAGEMENT ───────────────────────────────────────────
            SettingsSectionHeader(icon = Icons.Default.Storage, label = stringResource(R.string.settings_data_management),
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
                        Text(stringResource(R.string.settings_export_history),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.settings_export_desc),
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
                        Text(stringResource(R.string.settings_reset_data),
                            color = if (!isResetting) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.settings_reset_desc),
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
                Text("ANOTADOR DOMINO",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 4.sp))
                Spacer(Modifier.height(4.dp))
                Text("VERSION 2.4.0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    style = MaterialTheme.typography.labelSmall)
            }
        }

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
                Text(stringResource(R.string.settings_dialog_reset_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            },
            text  = {
                Text(
                    stringResource(R.string.settings_dialog_reset_body),
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.resetAllData()
                }) {
                    Text(stringResource(R.string.settings_dialog_reset_confirm), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.settings_dialog_reset_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ── Language selection dialog ──────────────────────────────────────────
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest  = { showLanguageDialog = false },
            containerColor    = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text(stringResource(R.string.settings_language),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold)
            },
            text  = {
                // Columna deslizable por si en el futuro tienes 20 idiomas
                Column(Modifier.verticalScroll(rememberScrollState())) {

                    supportedLanguages.forEach { lang ->
                        val isSelected = lang.tag == appLanguage

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.kineticColors.cyanAccent.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    viewModel.setLanguage(lang.tag)
                                    showLanguageDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lang.nativeName,
                                color = if (isSelected) MaterialTheme.kineticColors.cyanAccent else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)

                            if (isSelected) {
                                Icon(Icons.Default.Check, null,
                                    tint = MaterialTheme.kineticColors.cyanAccent,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("CLOSE", color = MaterialTheme.colorScheme.onSurfaceVariant)
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