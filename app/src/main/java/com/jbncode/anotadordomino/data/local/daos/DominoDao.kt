package com.jbncode.anotadordomino.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jbncode.anotadordomino.data.local.entities.GameEntity
import com.jbncode.anotadordomino.data.local.entities.ParticipantEntity
import com.jbncode.anotadordomino.data.local.entities.RoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DominoDao {
    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Insert
    suspend fun insertParticipants(participants: List<ParticipantEntity>)

    @Insert
    suspend fun insertRound(round: RoundEntity)

    @Query("DELETE FROM rounds WHERE id = (SELECT MAX(id) FROM rounds WHERE gameId = :gameId)")
    suspend fun deleteLastRound(gameId: Int)

    /** Actualiza el status de una partida (ACTIVE / PAUSED / FINISHED). */
    @Query("UPDATE games SET status = :status WHERE id = :gameId")
    suspend fun updateGameStatus(gameId: Int, status: String)

    @Query("SELECT * FROM participants WHERE gameId = :gameId ORDER BY seatOrder ASC")
    suspend fun getParticipants(gameId: Int): List<ParticipantEntity>

    /**
     * Partida más reciente que NO sea FINISHED.
     * Devuelve null si todas las partidas están terminadas o no hay ninguna.
     */
    @Query("SELECT * FROM games WHERE status != 'FINISHED' ORDER BY id DESC LIMIT 1")
    suspend fun getActiveGame(): GameEntity?

    @Query("SELECT IFNULL(SUM(pointsScored), 0) FROM rounds WHERE gameId = :gameId AND winnerId = :participantId")
    suspend fun getTotalScore(gameId: Int, participantId: Int): Int

    // History — todas las partidas ordenadas por fecha
    @Query("SELECT * FROM games ORDER BY id DESC")
    fun observeAllGames(): Flow<List<GameEntity>>

    // Consultas Reactivas (Flow)
    @Query("SELECT IFNULL(SUM(pointsScored), 0) FROM rounds WHERE gameId = :gameId AND winnerId = :participantId")
    fun observeTotalScore(gameId: Int, participantId: Int): Flow<Int>

    @Query("SELECT * FROM games WHERE id = :gameId")
    fun observeGame(gameId: Int): Flow<GameEntity>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY id DESC")
    fun observeRoundScores(gameId: Int): Flow<List<RoundEntity>>
}