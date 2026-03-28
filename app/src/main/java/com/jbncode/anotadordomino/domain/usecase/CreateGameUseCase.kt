package com.jbncode.anotadordomino.domain.usecase

import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.repository.GameRepository
import javax.inject.Inject

class CreateGameUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(game: Game, participants: List<Participant>): Int {
        // 1. Guardamos el juego y obtenemos el ID generado
        val gameId = repository.createGame(game).toInt()

        // 2. Asignamos ese ID a cada participante y los guardamos
        val participantsWithGameId = participants.map {
            it.copy(gameId = gameId)
        }
        repository.addParticipants(participantsWithGameId)

        return gameId
    }
}