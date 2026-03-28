package com.jbncode.anotadordomino.domain.model

data class Game(
    val id: Int = 0,
    val startTime: Long,
    val targetScore: Int,
    val modality: GameModality,
    val status: GameStatus
)
