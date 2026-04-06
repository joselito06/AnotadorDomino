package com.jbncode.anotadordomino.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.jbncode.anotadordomino.domain.model.AvatarConfig
import com.jbncode.anotadordomino.ui.theme.kineticColors
import androidx.core.net.toUri

/**
 * Devuelve la configuración visual de un avatar a partir de su tipo (String).
 * Se llama desde cualquier pantalla — Scoreboard, History, Setup.
 */
@Composable
fun avatarConfigFor(avatarType: String): AvatarConfig {
    val colors = MaterialTheme.kineticColors
    return when (avatarType) {
        "PRESET_STAR"    -> AvatarConfig(Icons.Default.Star,           Color(0xFF1A1A2E), colors.neonGreen)
        "PRESET_CROWN"   -> AvatarConfig(Icons.Default.EmojiEvents,    Color(0xFF1A1500), Color(0xFFD4A800))
        "PRESET_BOLT"    -> AvatarConfig(
            Icons.Default.FlashOn,
            Color(0xFF0D1A2A),
            colors.cyanAccent
        )
        "PRESET_FIRE"    -> AvatarConfig(Icons.Default.Whatshot,       Color(0xFF2A0D0D), Color(0xFFFF5C3A))
        "PRESET_DIAMOND" -> AvatarConfig(Icons.Default.Diamond,        Color(0xFF0D1A2A), Color(0xFF7B61FF))
        "PRESET_SHIELD"  -> AvatarConfig(Icons.Default.Shield,         Color(0xFF1A1A1A), colors.cyanAccent)
        // ── Nuevos avatares de personajes ──────────────────────────────────
        "PRESET_KING"    -> AvatarConfig(Icons.Default.AccountCircle,  Color(0xFF1A0D00), Color(0xFFFFAA00))
        "PRESET_NINJA"   -> AvatarConfig(Icons.Default.SelfImprovement,Color(0xFF0A0A0A), Color(0xFF00E5FF))
        "PRESET_ROBOT"   -> AvatarConfig(Icons.Default.SmartToy,       Color(0xFF0D1A12), colors.neonGreen)
        "PRESET_WIZARD"  -> AvatarConfig(Icons.Default.AutoAwesome,    Color(0xFF1A001A), Color(0xFFCC44FF))
        "PRESET_PIRATE"  -> AvatarConfig(Icons.Default.SportsKabaddi,  Color(0xFF1A0D00), Color(0xFFFF6B35))
        "PRESET_ALIEN"   -> AvatarConfig(Icons.Default.BubbleChart,    Color(0xFF001A0D), Color(0xFF44FF88))
        else             -> AvatarConfig(Icons.Default.Person,         Color(0xFF1A1A1A), colors.cyanAccent)
    }
}

// ── Shared AvatarDisplay composable ───────────────────────────────────────────

/**
 * Composable reutilizable que renderiza el avatar correcto:
 * - Foto de galería (Coil) si photoUri != null y avatarType == "GALLERY"
 * - Ícono preset con su color y fondo si es cualquier PRESET_*
 *
 * @param avatarType  String del tipo ("PRESET_STAR", "GALLERY", etc.)
 * @param photoUri    URI de foto como String, nullable
 * @param size        Tamaño del círculo del avatar
 * @param iconSize    Tamaño del ícono dentro (default = size * 0.55)
 * @param shape       Forma del clip (default CircleShape)
 */
@Composable
fun AvatarDisplay(
    avatarType: String,
    photoUri: String?,
    size: Dp      = 44.dp,
    iconSize: Dp  = (size.value * 0.55f).dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config  = avatarConfigFor(avatarType)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(config.bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (avatarType == "GALLERY" && !photoUri.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(photoUri.toUri())
                        .crossfade(true)
                        .allowHardware(false)
                        .build()
                ),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Icon(
                config.icon, null,
                tint     = config.tintColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Variante cuadrada con esquinas redondeadas (usada en HandLogRow del Scoreboard).
 */
@Composable
fun AvatarSquare(
    avatarType: String,
    photoUri: String?,
    size: Dp      = 40.dp,
    iconSize: Dp  = (size.value * 0.5f).dp,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val config  = avatarConfigFor(avatarType)
    val shape   = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(config.bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (avatarType == "GALLERY" && !photoUri.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(photoUri.toUri())
                        .crossfade(true)
                        .allowHardware(false)
                        .build()
                ),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(shape)
            )
        } else {
            Icon(config.icon, null, tint = config.tintColor, modifier = Modifier.size(iconSize))
        }
    }
}