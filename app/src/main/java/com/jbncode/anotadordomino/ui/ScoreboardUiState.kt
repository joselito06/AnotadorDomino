package com.jbncode.anotadordomino.ui

data class ScoreboardUiState(
    val usScore: Int = 0,
    val themScore: Int = 0,
    val targetScore: Int = 200,
    val isGameOver: Boolean = false,
    val winnerName: String? = null
)
