package com.jbncode.anotadordomino.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jbncode.anotadordomino.ui.components.AnotadorDomBottomBar
import com.jbncode.anotadordomino.ui.screen.HistoryScreen
import com.jbncode.anotadordomino.ui.screen.RestartingScreen
import com.jbncode.anotadordomino.ui.screen.ScoreboardScreen
import com.jbncode.anotadordomino.ui.screen.SettingsScreen
import com.jbncode.anotadordomino.ui.screen.SetupScreen
import com.jbncode.anotadordomino.ui.screen.StatsScreen
import com.jbncode.anotadordomino.ui.theme.kineticColors
import com.jbncode.anotadordomino.ui.viewmodel.AppStartDestination
import com.jbncode.anotadordomino.ui.viewmodel.MainViewModel

@Composable
fun DominoComposeApp(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()

    // Splash mínimo mientras Room responde
    if (startDestination is AppStartDestination.Checking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.kineticColors.neonGreen)
        }
        return
    }

    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ocultamos el BottomBar en Scoreboard
    //val showBottomBar = currentRoute?.startsWith("scoreboard_screen") != true
    val showBottomBar = currentRoute == Screen.Setup.route ||
            currentRoute == Screen.Stats.route || currentRoute == Screen.History.route


    // BottomBar visible en estas rutas
    //val showBottomBar = currentRoute in listOf(
    //    Screen.Setup.route, Screen.History.route, Screen.Stats.route

    // Acción global de Settings — disponible desde cualquier pantalla del BottomBar
    val navigateToSettings = { navController.navigate(Screen.Settings.route) }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter   = slideInVertically(initialOffsetY = { it }),
                exit    = slideOutVertically(targetOffsetY = { it })
            ) {
                AnotadorDomBottomBar(
                    currentRoute = currentRoute,       // ← para resaltar el ítem activo
                    onHistoryClick = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(Screen.Setup.route)
                            launchSingleTop = true
                        }
                    },
                    onPlayClick = {
                        // Si ya estamos en Setup, no navegar de nuevo
                        if (currentRoute != Screen.Setup.route) {
                            navController.navigate(Screen.Setup.route) {
                                popUpTo(Screen.Setup.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onStatsClick = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(Screen.Setup.route)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = Screen.Setup.route,
            modifier         = Modifier.padding(innerPadding)
        ) {

            // 1. Setup (pantalla principal / Play)
            composable(Screen.Setup.route) {
                SetupScreen(
                    onSettingsClick = navigateToSettings,
                    onGameStarted = { gameId ->
                        navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                            popUpTo(Screen.Setup.route) {inclusive = false}
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onSettingsClick = navigateToSettings,
                    onResumeGame = { gameId ->
                        navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                            popUpTo(Screen.Setup.route)
                            launchSingleTop = true
                        }
                    }
                )
            }

            // 2. Stats
            composable(Screen.Stats.route) {
                StatsScreen(onSettingsClick = navigateToSettings)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onRestarting = {
                        navController.navigate(Screen.RestartingScreen.route) { // Usa tu constante de ruta
                            popUpTo(navController.graph.startDestinationId) { inclusive = true } // Limpia backstack
                            launchSingleTop = true
                        }
                    }
                )
            }


            // 4. Scoreboard (Pizarra)
            composable(
                route     = Screen.Scoreboard.route,
                arguments = listOf(navArgument("gameId") { type = NavType.IntType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable
                ScoreboardScreen(
                    gameId         = gameId,
                    onNavigateHome = {
                        // Volver a Setup (raíz). popBackStack lo encuentra siempre.
                        val wentBack = navController.popBackStack(
                            route     = Screen.Setup.route,
                            inclusive = false
                        )
                        if (!wentBack) {
                            navController.navigate(Screen.Setup.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    /*onMatchFinished = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    onLeave = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }*/
                )
            }

            composable(Screen.RestartingScreen.route){
                RestartingScreen()
            }
        }

        // Si había partida activa/pausada → navegar directo al Scoreboard
        LaunchedEffect(startDestination) {
            if (startDestination is AppStartDestination.ResumeGame) {
                val gameId = (startDestination as AppStartDestination.ResumeGame).gameId
                navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                    popUpTo(Screen.Setup.route) { inclusive = false }
                    launchSingleTop = true
                }
                mainViewModel.onNavigationConsumed()
            }
        }
    }
}