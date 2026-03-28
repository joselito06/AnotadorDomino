package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.ui.theme.NeonGreen
import com.jbncode.anotadordomino.ui.theme.TextGray

@Composable
fun AnotadorDomBottomBar(
    onHistoryClick: () -> Unit,
    onPlayClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    // Un Box principal para poder solapar el botón flotante sobre la barra
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), // Altura total incluyendo el botón que sobresale
        contentAlignment = Alignment.BottomCenter
    ) {
        // La barra oscura del fondo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color(0xFF0A0C10)), // Color ultra oscuro
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón History
            BottomBarItem(icon = "🕒", text = "HISTORY", onClick = onHistoryClick)

            // Espacio vacío en el medio para el botón flotante
            Spacer(modifier = Modifier.width(60.dp))

            // Botón Stats
            BottomBarItem(icon = "📊", text = "STATS", onClick = onStatsClick)
        }

        // El botón flotante gigante verde (centrado en la parte superior del Box)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(76.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(NeonGreen)
                .clickable { onPlayClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("+", fontSize = 32.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                Text("PLAY", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BottomBarItem(icon: String, text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}