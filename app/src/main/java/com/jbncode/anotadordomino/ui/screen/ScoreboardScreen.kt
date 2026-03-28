package com.jbncode.anotadordomino.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.ui.theme.*
import com.jbncode.anotadordomino.ui.viewmodel.ScoreboardViewModel

@Composable
fun ScoreboardScreen(
    gameId: Int, // Recibido de la navegación
    onMatchFinished: () -> Unit,
    viewModel: ScoreboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Encabezado
        Text("POINT GOAL", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(uiState.targetScore.toString(), color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(40.dp))

        // Pizarra principal (Equipos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Tarjeta Equipo 1
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NOSOTROS", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.usScore.toString(),
                        color = TextWhite,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Tarjeta Equipo 2
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ELLOS", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.themScore.toString(),
                        color = TextWhite,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botones de prueba para anotar (Aquí irá el teclado numérico después)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { viewModel.addPoints(teamId = 1, points = 25) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("+25 Nosotros", color = Color.Black)
            }

            Button(
                onClick = { viewModel.addPoints(teamId = 2, points = 25) },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
            ) {
                Text("+25 Ellos", color = TextWhite)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}