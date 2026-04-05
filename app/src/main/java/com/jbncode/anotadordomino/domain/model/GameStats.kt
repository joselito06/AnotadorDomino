package com.jbncode.anotadordomino.domain.model

data class GameStats(
    val totalGames: Int,
    val finishedGames: Int,
    val totalRounds: Int,
    val recordHighScore: Int,
    val capicuaCount: Int,
    val blockedCount: Int,
    /** Partidas por día los últimos 7 días (índice 0=más antiguo, 6=hoy) */
    val last7DaysActivity: List<Int>
) {
    /** Win rate como porcentaje 0..100 (sin ViewModel separado) */
    val winRatePercent: Int
        get() = if (finishedGames == 0) 0
        else (totalGames * 100 / finishedGames.coerceAtLeast(1)).coerceAtMost(100)
}
