package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// ── UI models ──────────────────────────────────────────────────────────────────

data class ParticipantHistoryUi(
    val id: Int,
    val name: String,
    val score: Int,
    val isWinner: Boolean
)

data class MatchHistoryUi(
    val gameId: Int,
    val modality: GameModality,
    val status: GameStatus,
    val dateLabel: String,       // "Oct 24, 10:45 PM"
    val participants: List<ParticipantHistoryUi>,
    val totalRounds: Int
)

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    object Empty   : HistoryUiState()
    data class Success(val matches: List<MatchHistoryUi>) : HistoryUiState()
}

// ── ViewModel ──────────────────────────────────────────────────────────────────

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.observeAllGames().collect { games ->
                if (games.isEmpty()) {
                    _uiState.value = HistoryUiState.Empty
                    return@collect
                }

                // Para cada juego cargamos participantes y scores en paralelo
                val matches = games.map { game -> buildMatchUi(game) }
                _uiState.value = HistoryUiState.Success(matches)
            }
        }
    }

    private suspend fun buildMatchUi(game: Game): MatchHistoryUi {
        val participants = repository.getParticipants(game.id)
        val scores       = repository.getScoresForGame(game.id)
        val maxScore     = scores.values.maxOrNull() ?: 0

        val participantUis = participants.map { p ->
            val score = scores[p.id] ?: 0
            ParticipantHistoryUi(
                id       = p.id,
                name     = p.name,
                score    = score,
                // Ganador: quien llegó al targetScore, o el de mayor score si la partida está activa
                isWinner = when (game.status) {
                    GameStatus.FINISHED -> score >= game.targetScore || score == maxScore
                    else                -> score == maxScore && maxScore > 0
                }
            )
        }

        return MatchHistoryUi(
            gameId      = game.id,
            modality    = game.modality,
            status      = game.status,
            dateLabel   = dateFormatter.format(Date(game.startTime)),
            participants = participantUis,
            totalRounds = 0   // opcional: podrías agregar un query COUNT en el DAO
        )
    }
}