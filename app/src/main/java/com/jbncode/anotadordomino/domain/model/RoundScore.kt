package com.jbncode.anotadordomino.domain.model

data class RoundScore(
    val id: Int = 0,
    val gameId: Int,
    val winnerId: Int,
    val pointsScored: Int,
    val winType: WinType
)
