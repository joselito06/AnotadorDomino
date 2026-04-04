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
            currentRoute == Screen.Stats.route

    // BottomBar visible en estas rutas
    //val showBottomBar = currentRoute in listOf(
    //    Screen.Setup.route, Screen.History.route, Screen.Stats.route

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
                            popUpTo(Screen.History.route) { inclusive = true }
                        }
                    },
                    onPlayClick = { navController.navigate(Screen.Setup.route) },
                    onStatsClick = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(Screen.Setup.route)
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

            // 1. Home (Historial)
            composable(Screen.Setup.route) {
                SetupScreen(
                    onGameStarted = { gameId ->
                        navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                            popUpTo(Screen.Setup.route)
                        }
                    },
                    onSettingClicked = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Setup.route)
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onResumeGame = { gameId ->
                        navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                            popUpTo(Screen.Setup.route)
                        }
                    }
                )
            }

            // 2. Stats
            composable(Screen.Stats.route) {
                StatsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
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
                        navController.navigate(Screen.Setup.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
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
        }

        // Si había partida activa/pausada → navegar directo al Scoreboard
        LaunchedEffect(startDestination) {
            if (startDestination is AppStartDestination.ResumeGame) {
                val gameId = (startDestination as AppStartDestination.ResumeGame).gameId
                navController.navigate(Screen.Scoreboard.createRoute(gameId)) {
                    popUpTo(Screen.Setup.route) { inclusive = false }
                }
            }
        }
    }
}