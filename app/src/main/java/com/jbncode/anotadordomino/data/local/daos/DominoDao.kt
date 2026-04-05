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

    @Query("DELETE FROM rounds")
    suspend fun deleteAllRounds()

    @Query("DELETE FROM participants")
    suspend fun deleteAllParticipants()

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()

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

    /** Total de partidas FINISHED */
    @Query("SELECT COUNT(*) FROM games WHERE status = 'FINISHED'")
    suspend fun countFinishedGames(): Int

    /** Partidas ganadas: juegos donde el participante con mayor score tiene el winnerId */
    @Query("""
        SELECT COUNT(*) FROM games g
        WHERE g.status = 'FINISHED'
        AND (
            SELECT r.winnerId FROM rounds r
            WHERE r.gameId = g.id
            GROUP BY r.winnerId
            ORDER BY SUM(r.pointsScored) DESC
            LIMIT 1
        ) = :participantId
    """)
    suspend fun countWinsForParticipant(participantId: Int): Int

    /** Record high: mayor puntaje individual en una sola partida */
    @Query("""
        SELECT IFNULL(MAX(total), 0) FROM (
            SELECT SUM(pointsScored) as total
            FROM rounds
            GROUP BY gameId, winnerId
        )
    """)
    suspend fun getRecordHighScore(): Int

    /** Total de rondas CAPICUA */
    @Query("SELECT COUNT(*) FROM rounds WHERE winType = 'CAPICUA'")
    suspend fun countCapicuaRounds(): Int

    /** Total de rondas BLOCKED (tranque) */
    @Query("SELECT COUNT(*) FROM rounds WHERE winType = 'BLOCKED'")
    suspend fun countBlockedRounds(): Int

    /** Total de rondas jugadas en todas las partidas */
    @Query("SELECT COUNT(*) FROM rounds")
    suspend fun countTotalRounds(): Int

    /** Partidas jugadas en los últimos 7 días agrupadas por día */
    @Query("""
        SELECT (startTime / 86400000) as dayEpoch, COUNT(*) as gamesCount
        FROM games
        WHERE startTime >= :fromEpoch
        GROUP BY dayEpoch
        ORDER BY dayEpoch ASC
    """)
    suspend fun getGamesPerDayLast7(fromEpoch: Long): List<DayCount>

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

/** Helper para query de momentum */
data class DayCount(val dayEpoch: Long, val gamesCount: Int)