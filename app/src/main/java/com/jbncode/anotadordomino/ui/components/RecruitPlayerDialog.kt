package com.jbncode.anotadordomino.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jbncode.anotadordomino.domain.model.AvatarType
import com.jbncode.anotadordomino.ui.theme.kineticColors

// ── Avatar preset definitions ──────────────────────────────────────────────────

private data class AvatarPreset(
    val type: AvatarType,
    val icon: ImageVector,
    val bgColor: Color,
    val tintColor: Color
)

// Los colores se calculan en Composable, así que los definimos como función
@Composable
private fun avatarPresets(): List<AvatarPreset> {
    val colors = MaterialTheme.kineticColors
    return listOf(
        AvatarPreset(AvatarType.PRESET_STAR,    Icons.Default.Star,         Color(0xFF1A1A2E), colors.neonGreen),
        AvatarPreset(AvatarType.PRESET_CROWN,   Icons.Default.EmojiEvents,  Color(0xFF1A1500), Color(0xFFD4A800)),
        AvatarPreset(AvatarType.PRESET_BOLT,    Icons.Default.FlashOn,      Color(0xFF0D1A2A), colors.cyanAccent),
        AvatarPreset(AvatarType.PRESET_FIRE,    Icons.Default.Whatshot,     Color(0xFF2A0D0D), Color(0xFFFF5C3A)),
        AvatarPreset(AvatarType.PRESET_DIAMOND, Icons.Default.Diamond,      Color(0xFF0D1A2A), Color(0xFF7B61FF)),
        AvatarPreset(AvatarType.PRESET_SHIELD,  Icons.Default.Shield,       Color(0xFF1A1A1A), colors.cyanAccent.copy(alpha = 0.8f)),
    )
}

// ── Dialog ─────────────────────────────────────────────────────────────────────

/**
 * Dialog "Recruit New Player" con:
 *  - Nombre del jugador
 *  - 6 avatares predefinidos con íconos y colores únicos
 *  - Opción de elegir foto desde la galería del teléfono
 *  - Preview del avatar seleccionado
 *
 * @param onDismiss   Cierra sin guardar.
 * @param onAddPlayer Devuelve (nombre, avatarType, photoUri?) al ViewModel.
 */
@Composable
fun RecruitPlayerDialog(
    onDismiss: () -> Unit,
    onAddPlayer: (name: String, avatarType: AvatarType, photoUri: String?) -> Unit = { _, _, _ -> }
) {
    val colors     = MaterialTheme.kineticColors
    val presets    = avatarPresets()
    val context    = LocalContext.current

    var playerName    by remember { mutableStateOf("") }
    var selectedType  by remember { mutableStateOf(AvatarType.PRESET_STAR) }
    var galleryUri    by remember { mutableStateOf<Uri?>(null) }

    // Launcher para el picker de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            galleryUri   = uri
            selectedType = AvatarType.GALLERY
        }
    }

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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column {
                        Text("RECRUIT", color = colors.cyanAccent,
                            style = MaterialTheme.typography.labelMedium)
                        Text("New Player", color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineLarge)
                    }
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, "Close",
                            tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Preview del avatar seleccionado ───────────────────────
                AvatarPreview(
                    selectedType = selectedType,
                    galleryUri   = galleryUri,
                    presets      = presets,
                    playerName   = playerName
                )

                Spacer(Modifier.height(20.dp))

                // ── Identity tag ──────────────────────────────────────────
                Text("IDENTITY TAG", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                IdentityTagInput(value = playerName, onValueChange = { playerName = it })

                Spacer(Modifier.height(20.dp))

                // ── Visual signature label ────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("VISUAL SIGNATURE", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium)
                    Text("${presets.size + 1} OPTIONS", color = colors.cyanAccent,
                        style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(12.dp))

                // ── Avatar grid: 6 presets + 1 galería ───────────────────
                AvatarGrid(
                    presets      = presets,
                    selectedType = selectedType,
                    galleryUri   = galleryUri,
                    onSelectPreset = { type ->
                        selectedType = type
                        // Si elige un preset, descarta la foto de galería visualmente
                        // pero la URI sigue en memoria por si vuelve a elegir GALLERY
                        if (type != AvatarType.GALLERY) galleryUri = null
                    },
                    onPickGallery = {
                        galleryLauncher.launch("image/*")
                    }
                )

                Spacer(Modifier.height(20.dp))

                // ── Status card ───────────────────────────────────────────
                StatusCard()

                Spacer(Modifier.height(20.dp))

                // ── Add button ────────────────────────────────────────────
                val canAdd = playerName.isNotBlank()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (canAdd) colors.neonGreen else colors.neonGreen.copy(alpha = 0.35f))
                        .clickable(enabled = canAdd) {
                            onAddPlayer(
                                playerName.trim(),
                                selectedType,
                                galleryUri?.toString()
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonAdd, null,
                            tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ADD PLAYER",
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

// ── Avatar Preview ─────────────────────────────────────────────────────────────

@Composable
private fun AvatarPreview(
    selectedType: AvatarType,
    galleryUri: Uri?,
    presets: List<AvatarPreset>,
    playerName: String
) {
    val colors = MaterialTheme.kineticColors
    val preset = presets.firstOrNull { it.type == selectedType }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(preset?.bgColor ?: MaterialTheme.colorScheme.surface)
                    .border(2.dp, colors.cyanAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedType == AvatarType.GALLERY && galleryUri != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(galleryUri).crossfade(true).build()
                            ),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier     = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    preset != null -> {
                        Icon(preset.icon, null, tint = preset.tintColor, modifier = Modifier.size(32.dp))
                    }
                    else -> {
                        Icon(Icons.Default.AddPhotoAlternate, null,
                            tint = colors.cyanAccent, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text  = if (playerName.isBlank()) "Player Name" else playerName,
                    color = if (playerName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = if (selectedType == AvatarType.GALLERY && galleryUri != null)
                        "Custom photo"
                    else preset?.type?.name?.replace("PRESET_", "")?.lowercase()
                        ?.replaceFirstChar { it.uppercase() } ?: "Select avatar",
                    color = colors.cyanAccent,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ── Avatar Grid ────────────────────────────────────────────────────────────────

@Composable
private fun AvatarGrid(
    presets: List<AvatarPreset>,
    selectedType: AvatarType,
    galleryUri: Uri?,
    onSelectPreset: (AvatarType) -> Unit,
    onPickGallery: () -> Unit
) {
    val colors = MaterialTheme.kineticColors
    // 6 presets + 1 celda de galería = 7 celdas en grid de 4 columnas = 2 filas
    val allItems: List<Any> = presets + "GALLERY_CELL"  // String como marker

    val rows = allItems.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    if (item is String && item == "GALLERY_CELL") {
                        // Celda especial de galería
                        GalleryCell(
                            isSelected = selectedType == AvatarType.GALLERY,
                            hasPhoto   = galleryUri != null,
                            photoUri   = galleryUri,
                            onClick    = onPickGallery,
                            modifier   = Modifier.weight(1f)
                        )
                    } else if (item is AvatarPreset) {
                        PresetAvatarCell(
                            preset     = item,
                            isSelected = selectedType == item.type,
                            onSelect   = { onSelectPreset(item.type) },
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
                // Rellenar fila incompleta
                val remainder = 4 - row.size
                repeat(remainder) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PresetAvatarCell(
    preset: AvatarPreset,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(preset.bgColor)
            .border(
                width = 2.dp,
                color = if (isSelected) colors.cyanAccent else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            preset.icon, null,
            tint     = preset.tintColor,
            modifier = Modifier.size(28.dp)
        )
        if (isSelected) CheckBadge()
    }
}

@Composable
private fun GalleryCell(
    isSelected: Boolean,
    hasPhoto: Boolean,
    photoUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = if (isSelected) colors.cyanAccent else
                    colors.cyanAccent.copy(alpha = 0.3f),   // siempre borde tenue para destacar
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isSelected && hasPhoto && photoUri != null -> {
                // Muestra miniatura de la foto elegida
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(photoUri).crossfade(true).build()
                    ),
                    contentDescription = "Gallery photo",
                    contentScale = ContentScale.Crop,
                    modifier     = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                )
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddPhotoAlternate, null,
                        tint     = colors.cyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("PHOTO", color = colors.cyanAccent,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp))
                }
            }
        }
        if (isSelected) CheckBadge()
    }
}

@Composable
private fun BoxScope.CheckBadge() {
    val colors = MaterialTheme.kineticColors
    Box(
        modifier = Modifier
            .size(20.dp)
            .align(Alignment.TopEnd)
            .offset(x = (-4).dp, y = 4.dp)
            .clip(CircleShape)
            .background(colors.cyanAccent),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Check, null,
            tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(13.dp))
    }
}

// ── Identity Tag Input ─────────────────────────────────────────────────────────

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
                            Text("Enter name...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyLarge)
                        }
                        inner()
                    }
                }
            )
            Icon(Icons.Default.Fingerprint, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp))
        }
    }
}

// ── Status Card ────────────────────────────────────────────────────────────────

@Composable
private fun StatusCard() {
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
                Text("STATUS", color = colors.cyanAccent, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text("New Challenger detected. Assigning initial kinetic ranking...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(16.dp))
            Box(Modifier.width(1.dp).height(48.dp).background(MaterialTheme.colorScheme.outline))
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RANK", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text("N/A", color = colors.cyanAccent,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}