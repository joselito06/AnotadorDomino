package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.ui.theme.NeonCyan
import com.jbncode.anotadordomino.ui.theme.NeonGreen
import com.jbncode.anotadordomino.ui.theme.SurfaceDark
import com.jbncode.anotadordomino.ui.theme.TextGray
import com.jbncode.anotadordomino.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerBottomSheet(
    onDismiss: () -> Unit,
    onAddPlayer: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var playerName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark, // Fondo gris oscuro
        dragHandle = null // Quitamos la rayita de arriba si queremos que se vea limpio
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("RECRUIT", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("New Player", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                // Botón de cerrar "X"
                IconButton(onClick = onDismiss) {
                    Text("✕", color = TextGray, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de Texto (Identity Tag)
            Text("IDENTITY TAG", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                placeholder = { Text("Enter name...", color = TextGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color(0xFF2A2E38),
                    focusedContainerColor = Color(0xFF121418),
                    unfocusedContainerColor = Color(0xFF121418),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF182226)) // Gris verdoso oscuro
                    .border(1.dp, Color(0xFF243338), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("STATUS", color = TextGray, fontSize = 10.sp)
                        Text(
                            "New Challenger detected. Assigning initial kinetic ranking...",
                            color = TextWhite, fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("RANK", color = TextGray, fontSize = 10.sp)
                        Text("N/A", color = NeonCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón ADD PLAYER
            Button(
                onClick = {
                    if (playerName.isNotBlank()) {
                        onAddPlayer(playerName)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("👤 ADD PLAYER", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp)) // Margen inferior para los gestos del sistema
        }
    }
}