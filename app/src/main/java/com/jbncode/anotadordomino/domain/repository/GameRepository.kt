package com.jbncode.anotadordomino.domain.repository

import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.model.RoundScore
import kotlinx.coroutines.flow.Flow

interface  GameRepository {
    suspend fun createGame(game: Game): Long
    suspend fun addParticipants(participants: List<Participant>)
    suspend fun addRoundScore(roundScore: RoundScore) {}
    suspend fun undoLastRoundScore(gameId: Int)

    // Funciones reactivas
    fun observeTotalScore(gameId: Int, participantId: Int): Flow<Int>
    fun observeGameStatus(gameId: Int): Flow<Game>
}