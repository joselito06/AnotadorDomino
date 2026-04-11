package com.jbncode.anotadordomino.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.domain.model.AvatarType
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.util.UiText

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
        // ── Símbolos de domino ─────────────────────────────────────────────
        AvatarPreset(AvatarType.PRESET_STAR,    Icons.Default.Star,             Color(0xFF1A1A2E), colors.neonGreen),
        AvatarPreset(AvatarType.PRESET_CROWN,   Icons.Default.EmojiEvents,      Color(0xFF1A1500), Color(0xFFD4A800)),
        AvatarPreset(AvatarType.PRESET_BOLT,    Icons.Default.FlashOn,          Color(0xFF0D1A2A), colors.cyanAccent),
        AvatarPreset(AvatarType.PRESET_FIRE,    Icons.Default.Whatshot,         Color(0xFF2A0D0D), Color(0xFFFF5C3A)),
        AvatarPreset(AvatarType.PRESET_DIAMOND, Icons.Default.Diamond,          Color(0xFF0D1A2A), Color(0xFF7B61FF)),
        AvatarPreset(AvatarType.PRESET_SHIELD,  Icons.Default.Shield,           Color(0xFF1A1A1A), colors.cyanAccent.copy(alpha = 0.8f)),
        // ── Personajes del juego ───────────────────────────────────────────
        AvatarPreset(AvatarType.PRESET_KING,    Icons.Default.AccountCircle,    Color(0xFF1A0D00), Color(0xFFFFAA00)),
        AvatarPreset(AvatarType.PRESET_NINJA,   Icons.Default.SelfImprovement,  Color(0xFF0A0A0A), Color(0xFF00E5FF)),
        AvatarPreset(AvatarType.PRESET_ROBOT,   Icons.Default.SmartToy,         Color(0xFF0D1A12), colors.neonGreen),
        AvatarPreset(AvatarType.PRESET_WIZARD,  Icons.Default.AutoAwesome,      Color(0xFF1A001A), Color(0xFFCC44FF)),
        AvatarPreset(AvatarType.PRESET_PIRATE,  Icons.Default.SportsKabaddi,    Color(0xFF1A0D00), Color(0xFFFF6B35)),
        AvatarPreset(AvatarType.PRESET_ALIEN,   Icons.Default.BubbleChart,      Color(0xFF001A0D), Color(0xFF44FF88)),
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

    var inputBounds by remember { mutableStateOf(Rect.Zero) }
    var addBtnBounds by remember { mutableStateOf(Rect.Zero) }

    // Launcher para el picker de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            galleryUri   = uri
            selectedType = AvatarType.GALLERY
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                //.fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.dialogBackground)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Header ────────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column {
                        Text(
                            stringResource(R.string.dialog_recruit_label), color = colors.cyanAccent,
                            style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.dialog_recruit_title), color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineLarge)
                    }
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
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
                Text(stringResource(R.string.dialog_identity_tag), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                IdentityTagInput(
                    value = playerName,
                    onValueChange = { playerName = it },
                    modifier = Modifier.onGloballyPositioned { inputBounds = it.boundsInRoot() }
                )

                Spacer(Modifier.height(20.dp))

                // ── Visual signature label ────────────────────────────────
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dialog_visual_signature), color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium)
                    Text(UiText.StringResource(R.string.dialog_options_count, presets.size + 1).asString(context), color = colors.cyanAccent,
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
                        galleryLauncher.launch(arrayOf("image/*"))
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
                        .onGloballyPositioned { addBtnBounds = it.boundsInRoot() }
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
                        Text(stringResource(R.string.dialog_btn_add),
                            color = MaterialTheme.colorScheme.background,
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            DialogTutorialOverlay(
                name = playerName,
                inputBounds = inputBounds,
                addBtnBounds = addBtnBounds
            )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
                            modifier     = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
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
                    text  = if (playerName.isBlank()) stringResource(R.string.dialog_player_name_default) else playerName,
                    color = if (playerName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = if (selectedType == AvatarType.GALLERY && galleryUri != null)
                        stringResource(R.string.dialog_custom_photo)
                    else preset?.type?.name?.replace("PRESET_", "")?.lowercase()
                        ?.replaceFirstChar { it.uppercase() } ?: stringResource(R.string.dialog_select_avatar),
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
                    modifier     = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
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
                    Text(stringResource(R.string.dialog_photo_label), color = colors.cyanAccent,
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
private fun IdentityTagInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.kineticColors

    Box(
        modifier = modifier
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
                            Text(stringResource(R.string.dialog_enter_name),
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
                Text(stringResource(R.string.dialog_status_label), color = colors.cyanAccent, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.dialog_status_desc),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(16.dp))
            Box(Modifier
                .width(1.dp)
                .height(48.dp)
                .background(MaterialTheme.colorScheme.outline))
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.dialog_rank_label), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
                Text(stringResource(R.string.dialog_rank_na), color = colors.cyanAccent,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun DialogTutorialOverlay(
    name: String,
    inputBounds: Rect,
    addBtnBounds: Rect
) {
    val density = LocalDensity.current
    val colors = MaterialTheme.kineticColors

    val infiniteTransition = rememberInfiniteTransition(label = "dialog_tutorial")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )

    Box(Modifier.fillMaxSize()) {
        if (name.isBlank() && inputBounds != Rect.Zero) {
            // PASO 1: Si NO hay nombre -> Señalamos el TextField apuntando hacia abajo
            val arrowX = with(density) { inputBounds.center.x.toDp() - 86.dp }
            val arrowY = with(density) { inputBounds.top.toDp() - 40.dp }

            Icon(
                Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = colors.cyanAccent,
                modifier = Modifier
                    .offset(x = arrowX, y = arrowY + bounce.dp)
                    .size(32.dp)
            )
        } else if (name.isNotBlank() && addBtnBounds != Rect.Zero) {
            // PASO 2: Si YA HAY nombre -> Señalamos el botón simulando un toque
            val fingerX = with(density) { addBtnBounds.center.x.toDp() - 16.dp }
            val fingerY = with(density) { addBtnBounds.bottom.toDp() + 2.dp }

            Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                tint = colors.neonGreen,
                modifier = Modifier
                    .offset(x = fingerX, y = fingerY - bounce.dp) // - bounce para que suba y tape el borde
                    .size(32.dp)
            )
        }
    }
}