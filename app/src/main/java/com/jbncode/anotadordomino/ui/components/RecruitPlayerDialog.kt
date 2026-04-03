package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jbncode.anotadordomino.ui.theme.kineticColors

// ── Avatar slots ───────────────────────────────────────────────────────────────

private enum class AvatarSlot {
    P1, P2, P3, P4, P5, UPLOAD, EMOJI, SILHOUETTE
}

private val avatarSlots = AvatarSlot.entries

// ── Dialog ─────────────────────────────────────────────────────────────────────

/**
 * Dialog "Recruit New Player".
 *
 * @param onDismiss   Cierra el dialog sin guardar.
 * @param onAddPlayer Devuelve el nombre ingresado para que el ViewModel lo persista.
 */
@Composable
fun RecruitPlayerDialog(
    onDismiss: () -> Unit,
    onAddPlayer: (name: String) -> Unit = {}
) {
    var playerName     by remember { mutableStateOf("") }
    var selectedSlot   by remember { mutableStateOf(AvatarSlot.P1) }
    val colors = MaterialTheme.kineticColors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.dialogBackground)
                .padding(24.dp)
        ) {
            Column {

                // ── Header ────────────────────────────────────────────────
                RecruitHeader(onDismiss)
                Spacer(Modifier.height(24.dp))

                // ── Identity tag ──────────────────────────────────────────
                RecruitSectionLabel("IDENTITY TAG")
                Spacer(Modifier.height(8.dp))
                IdentityTagInput(value = playerName, onValueChange = { playerName = it })
                Spacer(Modifier.height(20.dp))

                // ── Visual signature ──────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RecruitSectionLabel("VISUAL SIGNATURE")
                    Text(
                        text  = "${avatarSlots.size} OPTIONS",
                        color = colors.cyanAccent,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(Modifier.height(12.dp))
                AvatarGrid(selected = selectedSlot, onSelect = { selectedSlot = it })
                Spacer(Modifier.height(20.dp))

                // ── Status card ───────────────────────────────────────────
                RecruitStatusCard()
                Spacer(Modifier.height(24.dp))

                // ── Add button ────────────────────────────────────────────
                AddPlayerButton(
                    enabled = playerName.isNotBlank(),
                    onClick = { onAddPlayer(playerName) }
                )
            }
        }
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────

@Composable
private fun RecruitHeader(onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text  = "RECRUIT",
                color = MaterialTheme.kineticColors.cyanAccent,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text  = "New Player",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint     = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Section label (privado al dialog) ─────────────────────────────────────────

@Composable
private fun RecruitSectionLabel(text: String) {
    Text(
        text  = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
}

// ── Identity tag input ─────────────────────────────────────────────────────────

@Composable
private fun IdentityTagInput(value: String, onValueChange: (String) -> Unit) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (value.isNotBlank()) colors.cyanAccent.copy(alpha = 0.5f)
                else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier.weight(1f),
                textStyle     = TextStyle(
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontSize   = MaterialTheme.typography.bodyLarge.fontSize,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                ),
                decorationBox = { inner ->
                    Box(Modifier.padding(vertical = 16.dp)) {
                        if (value.isEmpty()) {
                            Text(
                                text  = "Enter name...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        inner()
                    }
                }
            )
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Avatar grid ────────────────────────────────────────────────────────────────

@Composable
private fun AvatarGrid(selected: AvatarSlot, onSelect: (AvatarSlot) -> Unit) {
    val rows = avatarSlots.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { slot ->
                    AvatarCell(
                        slot       = slot,
                        isSelected = slot == selected,
                        onSelect   = { onSelect(slot) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun AvatarCell(
    slot: AvatarSlot,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 2.dp,
                color = if (isSelected) colors.cyanAccent else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        // Icono según el tipo de slot
        val (icon, tintAlpha) = when (slot) {
            AvatarSlot.UPLOAD    -> Icons.Default.AddPhotoAlternate to 0.6f
            AvatarSlot.EMOJI     -> Icons.Default.TagFaces           to 0.5f
            AvatarSlot.SILHOUETTE-> Icons.Default.SmartToy           to 0.3f
            else                 -> Icons.Default.Person             to 0.5f
        }
        Icon(
            icon,
            contentDescription = null,
            tint     = if (slot == AvatarSlot.UPLOAD) colors.cyanAccent.copy(alpha = tintAlpha)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = tintAlpha),
            modifier = Modifier.size(28.dp)
        )

        // Check mark cuando está seleccionado
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(colors.cyanAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// ── Status card ────────────────────────────────────────────────────────────────

@Composable
private fun RecruitStatusCard() {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(colors.cyanAccent.copy(alpha = 0.35f), Color.Transparent)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text  = "STATUS",
                    color = colors.cyanAccent,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "New Challenger detected. Assigning initial kinetic ranking...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "RANK",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text  = "N/A",
                    color = colors.cyanAccent,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

// ── Add Player button ──────────────────────────────────────────────────────────

@Composable
private fun AddPlayerButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.neonGreen.copy(alpha = if (enabled) 1f else 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text  = "ADD PLAYER",
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}