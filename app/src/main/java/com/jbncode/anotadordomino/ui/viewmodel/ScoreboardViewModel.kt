package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.model.RoundScore
import com.jbncode.anotadordomino.domain.model.WinType
import com.jbncode.anotadordomino.domain.usecase.AddRoundScoreUseCase
import com.jbncode.anotadordomino.domain.usecase.ObserveScoreUseCase
import com.jbncode.anotadordomino.ui.ScoreboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoreboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Para extraer el gameId de la ruta de navegación
    private val observeScoreUseCase: ObserveScoreUseCase,
    private val addRoundScoreUseCase: AddRoundScoreUseCase
) : ViewModel() {

    // Extraemos el ID del juego que pasamos por la ruta del NavHost
    val gameId: Int = checkNotNull(savedStateHandle["gameId"])

    // Estado reactivo de la UI
    private val _uiState = MutableStateFlow(ScoreboardUiState())
    val uiState: StateFlow<ScoreboardUiState> = _uiState.asStateFlow()

    init {
        // En una app real, primero buscaríamos los IDs de los participantes reales de este gameId.
        // Aquí simulamos que los IDs son 1 y 2.
        val usTeamId = 1
        val themTeamId = 2

        viewModelScope.launch {
            combine(
                observeScoreUseCase(gameId, usTeamId),
                observeScoreUseCase(gameId, themTeamId)
            ) { usPoints, themPoints ->
                ScoreboardUiState(
                    usScore = usPoints,
                    themScore = themPoints,
                    isGameOver = usPoints >= 200 || themPoints >= 200
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun addPoints(teamId: Int, points: Int, winType: WinType = WinType.NORMAL) {
        viewModelScope.launch {
            val round = RoundScore(
                gameId = gameId,
                winnerId = teamId,
                pointsScored = points,
                winType = winType
            )
            addRoundScoreUseCase(round) // Guarda en DB y el Flow actualiza la UI automáticamente
        }
    }
}