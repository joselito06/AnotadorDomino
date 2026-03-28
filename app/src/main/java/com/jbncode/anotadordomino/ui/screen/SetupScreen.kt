package com.jbncode.anotadordomino.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbncode.anotadordomino.ui.components.AddPlayerBottomSheet
import com.jbncode.anotadordomino.ui.components.NeonPrimaryButton
import com.jbncode.anotadordomino.ui.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    onGameStarted: (Int) -> Unit, // Recibimos el evento de navegación
    viewModel: SetupViewModel = hiltViewModel() // Inyectamos el ViewModel
) {
    // Controlamos si el modal de "Nuevo Jugador" está abierto o cerrado
    var showAddPlayerSheet by remember { mutableStateOf(false) }

    // Aquí iría todo el diseño que hicimos antes (Sliders, Botones, etc.)
    Column( /* ... */ ) {

        // ...

        NeonPrimaryButton("START MATCH ⚡") {
            // Simulamos que el ViewModel guardó el juego en la BD
            // y nos devolvió el ID = 1 para esta partida.
            val newGameId = 1
            onGameStarted(newGameId) // Dispara la navegación a la pizarra
        }
    }

    // Lógica del Modal (Imagen 2)
    if (showAddPlayerSheet) {
        AddPlayerBottomSheet(
            onDismiss = { showAddPlayerSheet = false },
            onAddPlayer = { newName ->
                // viewModel.guardarNuevoJugador(newName)
                showAddPlayerSheet = false
            }
        )
    }
}