package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.ui.theme.kineticColors

/**
 * TopBar compartida para todas las pantallas principales.
 *
 * @param onSettingsClick   Navega a SettingsScreen — siempre visible.
 * @param showBack          true en pantallas secundarias (Scoreboard, Settings).
 * @param onBackClick       Acción del botón back — solo usado si showBack=true.
 * @param title             Título opcional que sobreescribe el logo (usado en Settings).
 */
@Composable
fun KineticTopBar(
    onSettingsClick: () -> Unit,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    title: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.kineticColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Izquierda: back o logo
        if (showBack) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint     = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            // Logo de la app — domino icon + texto
            AppLogo()
        }

        // Centro: título o nombre de app
        if (title != null) {
            Text(
                text       = title,
                color      = colors.cyanAccent,
                fontSize   = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily
            )
        } else {
            Text(
                text       = "ANOTADOR DOMINO",
                color      = colors.cyanAccent,
                fontSize   = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily
            )
        }

        // Derecha: settings (siempre visible salvo en SettingsScreen)
        if (!showBack) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint     = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            // Placeholder para mantener el título centrado
            Spacer(Modifier.size(40.dp))
        }
    }
}

// ── Logo de la app ─────────────────────────────────────────────────────────────

@Composable
private fun AppLogo() {
    val colors = MaterialTheme.kineticColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Ficha de dominó pequeña como ícono
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Punto izquierdo (verde = activo)
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.neonGreen)
                )
                // Línea divisoria del dominó
                Box(
                    Modifier
                        .width(1.dp)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                // Punto derecho (cian)
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.cyanAccent)
                )
            }
        }
    }
}