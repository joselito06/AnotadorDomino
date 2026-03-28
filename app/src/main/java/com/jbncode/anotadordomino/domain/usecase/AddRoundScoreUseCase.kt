package com.jbncode.anotadordomino.domain.usecase

import com.jbncode.anotadordomino.domain.model.RoundScore
import com.jbncode.anotadordomino.domain.repository.GameRepository
import javax.inject.Inject

class AddRoundScoreUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(roundScore: RoundScore) {
        // Regla de negocio básica: no se pueden anotar puntos negativos
        if (roundScore.pointsScored > 0) {
            repository.addRoundScore(roundScore)
        }
    }
}