package com.jbncode.anotadordomino.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Setup : Screen("setup")
    object Stats : Screen("stats_screen")

    // Pasamos el ID del juego por la ruta para que la pizarra sepa qué cargar
    object Scoreboard : Screen("scoreboard_screen/{gameId}") {
        fun createRoute(gameId: Int) = "scoreboard_screen/$gameId"
    }
}