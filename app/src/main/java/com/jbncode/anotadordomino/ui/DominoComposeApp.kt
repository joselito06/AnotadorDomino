package com.jbncode.anotadordomino.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jbncode.anotadordomino.ui.components.AnotadorDomBottomBar
import com.jbncode.anotadordomino.ui.screen.HomeScreen
import com.jbncode.anotadordomino.ui.screen.ScoreboardScreen
import com.jbncode.anotadordomino.ui.screen.SetupScreen

@Composable
fun DominoComposeApp() {
    // El controlador que maneja los viajes entre pantallas
    val navController = rememberNavController()

    // Obtenemos la ruta actual para saber en qué pantalla estamos
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lógica para ocultar el BottomBar en la pantalla del juego (Scoreboard)
    val showBottomBar = currentRoute?.startsWith("scoreboard_screen") != true

    // Scaffold principal (opcional a este nivel, pero útil para fondos generales)
    Scaffold(
        bottomBar = {
            // Animamos la entrada/salida de la barra inferior
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }), // Sube desde abajo
                exit = slideOutVertically(targetOffsetY = { it })   // Baja para ocultarse
            ) {
                AnotadorDomBottomBar(
                    onHistoryClick = {
                        // Navegamos al historial evitando duplicar pantallas en la pila
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onPlayClick = {
                        navController.navigate(Screen.Setup.route)
                    },
                    onStatsClick = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // 1. Pantalla de Inicio (Historial)
            composable(route = Screen.Home.route) {
                HomeScreen(
                    onNavigateToGame = { gameId ->
                        navController.navigate(Screen.Scoreboard.createRoute(gameId))
                    }
                )
            }

            // 2. PANTALLA ESTADÍSTICAS (Stats)
            composable(route = Screen.Stats.route) {

            }

            // 3. Pantalla de Configuración (Nueva Partida)
            composable(route = Screen.Setup.route) {
                SetupScreen(
                    onGameStarted = { gameId ->
                        // Cuando le dan al botón verde "START MATCH"
                        navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                            // Borramos el Setup de la pila para que el botón "Atrás"
                            // del celular no lo devuelva a la configuración, sino al inicio.
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // 4. Pantalla de la Pizarra
            composable(
                route = Screen.Scoreboard.route,
                arguments = listOf(navArgument("gameId") { type = NavType.IntType })
            ) { backStackEntry ->
                // Extraemos el ID del juego de la ruta
                val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable

                ScoreboardScreen(
                    gameId = gameId,
                    onMatchFinished = {
                        // Cuando el juego acaba y le dan a "Volver al inicio"
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }
        }
    }
}