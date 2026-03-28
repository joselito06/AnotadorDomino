package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.usecase.CreateGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    // Estado interno
    private val _targetScore = MutableStateFlow(200)
    val targetScore: StateFlow<Int> = _targetScore.asStateFlow()

    private val _isPairsMode = MutableStateFlow(true)
    val isPairsMode: StateFlow<Boolean> = _isPairsMode.asStateFlow()

    fun updateTargetScore(score: Float) {
        _targetScore.value = score.toInt()
    }

    fun updateGameMode(isPairs: Boolean) {
        _isPairsMode.value = isPairs
    }

    // Función principal que se llama al presionar "START MATCH"
    fun startGame(onGameCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val newGame = Game(
                startTime = System.currentTimeMillis(),
                targetScore = _targetScore.value,
                modality = if (_isPairsMode.value) GameModality.TEAM else GameModality.INDIVIDUAL,
                status = GameStatus.ACTIVE
            )

            // Simulamos los jugadores por ahora. En la app real, saldrían de una lista dinámica.
            val participants = listOf(
                Participant(name = "Nosotros", seatOrder = 1, gameId = 0),
                Participant(name = "Ellos", seatOrder = 2, gameId = 0)
            )

            // Guardamos en Room y obtenemos el ID
            val gameId = createGameUseCase(newGame, participants)

            // Avisamos a la UI que ya puede navegar a la pizarra
            onGameCreated(gameId)
        }
    }
}