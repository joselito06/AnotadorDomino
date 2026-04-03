package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.model.RoundScore
import com.jbncode.anotadordomino.domain.model.WinType
import com.jbncode.anotadordomino.domain.repository.GameRepository
import com.jbncode.anotadordomino.domain.usecase.AddRoundScoreUseCase
import com.jbncode.anotadordomino.ui.ScoreboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representa a un participante/equipo con su puntaje acumulado en la UI.
 */
data class ParticipantScoreUi(
    val participantId: Int,
    val name: String,
    val totalScore: Int,
    val targetScore: Int,
    val isLeading: Boolean      // tiene el puntaje más alto
)

/**
 * Representa una ronda en el log de manos.
 */
data class HandLogUi(
    val roundNumber: Int,       // número correlativo (1, 2, 3…)
    val roundScoreId: Int,      // ID en Room — necesario para undo
    val winnerName: String,
    val winnerId: Int,
    val pointsScored: Int,
    val winType: WinType
)



@HiltViewModel
class ScoreboardViewModel @Inject constructor(
    private val repository: GameRepository,
    private val addRoundScoreUseCase: AddRoundScoreUseCase
) : ViewModel() {

    // gameId se inicializa desde SavedStateHandle o manualmente con init()
    private var gameId: Int = -1

    // ── Internal state ─────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<ScoreboardUiState>(ScoreboardUiState.Loading)
    val uiState: StateFlow<ScoreboardUiState> = _uiState.asStateFlow()

    // Puntajes individuales por participante (cargados reactivamente)
    private val _participantScores = MutableStateFlow<Map<Int, Int>>(emptyMap())

    // Lista de participantes obtenida una sola vez al cargar el juego
    private var participants: List<Participant> = emptyList()

    // Log de rondas acumulado en UI (se reconstituye desde RoundScores en Room)
    private val _handLog = MutableStateFlow<List<HandLogUi>>(emptyList())

    // Estado de carga para acciones (add/undo)
    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    // Controla si el dialog "¿Abandonar partida?" está visible
    private val _showLeaveDialog = MutableStateFlow(false)
    val showLeaveDialog: StateFlow<Boolean> = _showLeaveDialog.asStateFlow()

    // ── Init ───────────────────────────────────────────────────────────────

    /**
     * Llamar desde la Screen justo después de que Hilt inyecte el ViewModel,
     * pasando el gameId que llega del NavHost.
     *
     * Idealmente esto se haría con SavedStateHandle; lo dejamos así
     * para no acoplar el ViewModel al navigation-compose en este momento.
     */
    fun init(id: Int) {
        if (gameId == id) return    // evita re-inicializar al recomposer
        gameId = id
        observeGame()
    }

    // ── Observers ──────────────────────────────────────────────────────────

    private fun observeGame() {
        viewModelScope.launch {
            try {
                // 1. Observamos el juego (estado + metadatos)
                repository.observeGameStatus(gameId).collect { game ->

                    // 2. Cargamos participantes (solo la primera vez o si cambian)
                    if (participants.isEmpty()) {
                        participants = repository.getParticipants(gameId)
                        // Arrancamos a observar el score de cada participante
                        participants.forEach { p -> observeScore(p.id) }
                        // Cargamos el log de rondas
                        observeHandLog()
                    }

                    rebuildState(game)
                }
            } catch (e: Exception) {
                _uiState.value = ScoreboardUiState.Error(
                    e.message ?: "Error loading game"
                )
            }
        }
    }

    private fun observeScore(participantId: Int) {
        viewModelScope.launch {
            repository.observeTotalScore(gameId, participantId).collect { score ->
                _participantScores.value = _participantScores.value
                    .toMutableMap().also { it[participantId] = score }
                val current = (_uiState.value as? ScoreboardUiState.Active)?.game
                if (current != null) rebuildState(current)
            }
        }
    }

    private fun observeHandLog() {
        viewModelScope.launch {
            repository.observeRoundScores(gameId).collect { rounds ->
                _handLog.value = rounds.mapIndexed { index, round ->
                    val winnerName = participants
                        .firstOrNull { it.id == round.winnerId }?.name ?: "?"
                    HandLogUi(
                        roundNumber  = rounds.size - index,   // más reciente = R más alto
                        roundScoreId = round.id,
                        winnerName   = winnerName,
                        winnerId     = round.winnerId,
                        pointsScored = round.pointsScored,
                        winType      = round.winType
                    )
                }
                // Reconstruir estado con el log actualizado
                val currentGame = (_uiState.value as? ScoreboardUiState.Active)?.game
                if (currentGame != null) rebuildState(currentGame)
            }
        }
    }

    /**
     * Construye el [ScoreboardUiState.Active] combinando game + scores + log.
     */
    private fun rebuildState(game: Game) {
        val scores = _participantScores.value
        val scoreList = participants.map { p ->
            ParticipantScoreUi(
                participantId = p.id,
                name          = p.name,
                totalScore    = scores[p.id] ?: 0,
                targetScore   = game.targetScore,
                isLeading     = false   // se calcula abajo
            )
        }
        val maxScore = scoreList.maxOfOrNull { it.totalScore } ?: 0
        val withLeading = scoreList.map { it.copy(isLeading = it.totalScore == maxScore && maxScore > 0) }

        val isFinished = game.status == GameStatus.FINISHED ||
                withLeading.any { it.totalScore >= game.targetScore }

        _uiState.value = ScoreboardUiState.Active(
            game         = game,
            participants = withLeading,
            handLog      = _handLog.value,
            isFinished   = isFinished
        )
    }

    // ── Actions ────────────────────────────────────────────────────────────

    /**
     * Registra una nueva ronda.
     *
     * @param winnerId   ID del participante ganador.
     * @param points     Puntos obtenidos en la mano.
     * @param isCapicua  Si fue capicúa (dobla los puntos según reglas opcionales).
     */
    fun addRoundScore(winnerId: Int, points: Int, isCapicua: Boolean) {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                addRoundScoreUseCase(
                    RoundScore(
                        gameId       = gameId,
                        winnerId     = winnerId,
                        pointsScored = if (isCapicua) points * 2 else points,
                        winType      = if (isCapicua) WinType.CAPICUA else WinType.NORMAL
                    )
                )
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    /**
     * Deshace la última ronda registrada.
     */
    fun undoLastRound() {
        viewModelScope.launch {
            _isActionLoading.value = true
            try {
                repository.undoLastRoundScore(gameId)
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    // ── Leave / Back handling ──────────────────────────────────────────────

    /**
     * Llamado cuando el usuario presiona Back o el botón de salir.
     * Muestra el dialog de confirmación si la partida está activa.
     */
    fun onBackPressed() {
        val state = _uiState.value
        if (state is ScoreboardUiState.Active && !state.isFinished) {
            _showLeaveDialog.value = true
        }
        // Si ya terminó, dejamos que la navegación proceda normalmente
    }

    fun dismissLeaveDialog() {
        _showLeaveDialog.value = false
    }

    /**
     * El usuario confirmó que quiere salir.
     * Pausa la partida para poder reanudarla después y navega al Home.
     */
    fun confirmLeave(onNavigateHome: () -> Unit) {
        viewModelScope.launch {
            repository.pauseGame(gameId)
            _showLeaveDialog.value = false
            onNavigateHome()
        }
    }
}