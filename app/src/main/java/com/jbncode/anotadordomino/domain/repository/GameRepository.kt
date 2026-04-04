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
    /** Marca la partida como PAUSED al salir del Scoreboard. */
    suspend fun pauseGame(gameId: Int)

    /** Marca la partida como ACTIVE al reanudarla. */
    suspend fun resumeGame(gameId: Int)
    suspend fun finishGame(gameId: Int)
    suspend fun getParticipants(gameId: Int): List<Participant>

    /**
     * Devuelve la partida más reciente en estado ACTIVE o PAUSED.
     * Retorna null si no hay ninguna pendiente.
     * Usado al iniciar la app para detectar si hay que reanudar.
     */
    suspend fun getActiveGame(): Game?
    /** Emite todas las partidas ordenadas más reciente primero. */
    fun observeAllGames(): Flow<List<Game>>
    /**
     * Para cada gameId, devuelve el score total de cada participante.
     * Usado en History para mostrar los puntajes finales sin observar
     * cada partida individualmente.
     */
    suspend fun getScoresForGame(gameId: Int): Map<Int, Int>

    // Funciones reactivas
    fun observeTotalScore(gameId: Int, participantId: Int): Flow<Int>
    fun observeGameStatus(gameId: Int): Flow<Game>
    fun observeRoundScores(gameId: Int): Flow<List<RoundScore>>
}