package com.jbncode.anotadordomino.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.ui.theme.DarkBackground
import com.jbncode.anotadordomino.ui.theme.NeonCyan
import com.jbncode.anotadordomino.ui.theme.TextGray
import com.jbncode.anotadordomino.ui.theme.TextWhite

@Composable
fun HomeScreen(
    onNavigateToGame: (Int) -> Unit // Para reanudar un juego
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("CURRENT SEASON", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("MATCH HISTORY", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(48.dp))

        // Estado vacío (Empty State)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🀄", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No matches played yet.",
                    color = TextGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Tap the + PLAY button to start.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}