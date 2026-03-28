package com.jbncode.anotadordomino.data.repository

import com.jbncode.anotadordomino.data.local.daos.DominoDao
import com.jbncode.anotadordomino.data.local.entities.GameEntity
import com.jbncode.anotadordomino.data.local.entities.ParticipantEntity
import com.jbncode.anotadordomino.data.local.entities.RoundEntity
import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.model.RoundScore
import com.jbncode.anotadordomino.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val dao: DominoDao
) : GameRepository {

    override suspend fun createGame(game: Game): Long {
        val entity = GameEntity(
            startTime = game.startTime,
            targetScore = game.targetScore,
            modality = game.modality.name,
            status = game.status.name
        )
        return dao.insertGame(entity)
    }

    override suspend fun addParticipants(participants: List<Participant>) {
        val entities = participants.map {
            ParticipantEntity(
                gameId = it.gameId,
                name = it.name,
                seatOrder = it.seatOrder
            )
        }
        dao.insertParticipants(entities)
    }

    override suspend fun addRoundScore(roundScore: RoundScore) {
        val entity = RoundEntity(
            gameId = roundScore.gameId,
            winnerId = roundScore.winnerId,
            pointsScored = roundScore.pointsScored,
            winType = roundScore.winType.name
        )
        dao.insertRound(entity)
    }

    override suspend fun undoLastRoundScore(gameId: Int) {
        dao.deleteLastRound(gameId)
    }

    override fun observeTotalScore(
        gameId: Int,
        participantId: Int
    ): Flow<Int> {
        return dao.observeTotalScore(gameId, participantId)
    }

    override fun observeGameStatus(gameId: Int): Flow<Game> {
        return dao.observeGame(gameId).map { entity ->
            // Mapeo inverso: de Entity a Model del Dominio
            Game(
                id = entity.id,
                startTime = entity.startTime,
                targetScore = entity.targetScore,
                modality = GameModality.valueOf(entity.modality),
                status = GameStatus.valueOf(entity.status)
            )
        }
    }

}