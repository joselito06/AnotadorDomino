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
import com.jbncode.anotadordomino.domain.model.WinType
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

    override suspend fun pauseGame(gameId: Int) {
        dao.updateGameStatus(gameId, GameStatus.PAUSED.name)
    }

    override suspend fun resumeGame(gameId: Int) {
        dao.updateGameStatus(gameId, GameStatus.ACTIVE.name)
    }

    override suspend fun getParticipants(gameId: Int): List<Participant> {
        return dao.getParticipants(gameId).map { entity ->
            Participant(
                id        = entity.id,
                gameId    = entity.gameId,
                name      = entity.name,
                seatOrder = entity.seatOrder
            )
        }
    }

    override suspend fun getActiveGame(): Game? {
        return dao.getActiveGame()?.toGame()
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

    override fun observeRoundScores(gameId: Int): Flow<List<RoundScore>> {
        return dao.observeRoundScores(gameId).map { entities ->
            entities.map { entity ->
                RoundScore(
                    id           = entity.id,
                    gameId       = entity.gameId,
                    winnerId     = entity.winnerId,
                    pointsScored = entity.pointsScored,
                    winType      = WinType.valueOf(entity.winType)
                )
            }
        }
    }

    // ── Mappers privados ───────────────────────────────────────────────────
    private fun GameEntity.toGame() = Game(
        id          = id,
        startTime   = startTime,
        targetScore = targetScore,
        modality    = GameModality.valueOf(modality),
        status      = GameStatus.valueOf(status)
    )

}