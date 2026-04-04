package com.jbncode.anotadordomino.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Setup : Screen("setup")
    object Game : Screen("game_screen")
    object History : Screen("history")
    object Stats : Screen("stats")
    object Settings : Screen("settings")

    // Pasamos el ID del juego por la ruta para que la pizarra sepa qué cargar
    object Scoreboard : Screen("scoreboard/{gameId}") {
        fun createRoute(gameId: Int) = "scoreboard/$gameId"
    }
}