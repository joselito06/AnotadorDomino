package com.jbncode.anotadordomino.domain.model

data class Participant(
    val id: Int = 0,
    val gameId: Int,
    val name: String,
    val seatOrder: Int
)
