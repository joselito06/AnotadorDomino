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

    // Consultas Reactivas (Flow)
    @Query("SELECT IFNULL(SUM(pointsScored), 0) FROM rounds WHERE gameId = :gameId AND winnerId = :participantId")
    fun observeTotalScore(gameId: Int, participantId: Int): Flow<Int>

    @Query("SELECT * FROM games WHERE id = :gameId")
    fun observeGame(gameId: Int): Flow<GameEntity>
}