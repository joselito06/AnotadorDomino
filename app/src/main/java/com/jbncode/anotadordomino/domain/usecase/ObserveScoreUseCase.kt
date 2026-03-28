package com.jbncode.anotadordomino.domain.usecase

import com.jbncode.anotadordomino.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveScoreUseCase @Inject constructor(
    private val repository: GameRepository
) {
    operator fun invoke(gameId: Int, participantId: Int): Flow<Int> {
        return repository.observeTotalScore(gameId, participantId)
    }
}