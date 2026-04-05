package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.model.AvatarType
import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.usecase.CreateGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val name: String,
    val wins: Int = 0,
    val avatarType: AvatarType = AvatarType.PRESET_STAR,
    /** URI de la foto elegida desde galería (solo válido cuando avatarType == GALLERY) */
    val photoUri: String? = null
)
data class PlayerLimits(val min: Int, val max: Int)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    // ── Configuración de la partida ────────────────────────────────────────
    private val _targetScore = MutableStateFlow(200)
    val targetScore: StateFlow<Int> = _targetScore.asStateFlow()

    private val _isPairsMode = MutableStateFlow(true)
    val isPairsMode: StateFlow<Boolean> = _isPairsMode.asStateFlow()

    // ── Lista de jugadores rápidos ─────────────────────────────────────────
    // En una versión futura esto vendría de un PlayerRepository para persistirlos.
    // Por ahora se mantiene en memoria durante la sesión.

    private val _quickPlayers = MutableStateFlow<List<PlayerUiState>>(emptyList())
    val quickPlayers: StateFlow<List<PlayerUiState>> = _quickPlayers.asStateFlow()

    // ── Estado de carga (mientras Room guarda la partida) ──────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Derived state (combine isPairsMode + quickPlayers) ─────────────────

    /** Límites calculados según la modalidad activa. */
    val playerLimits: StateFlow<PlayerLimits> =
        _isPairsMode.combine(_quickPlayers) { isPairs, _ ->
            if (isPairs) PlayerLimits(min = 2, max = 2)
            else         PlayerLimits(min = 2, max = 4)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerLimits(2, 2))

    /**
     * Habilita el botón START MATCH.
     *  - PAIRS      → exactamente 2 jugadores
     *  - INDIVIDUAL → entre 2 y 4 jugadores
     */
    val canStartGame: StateFlow<Boolean> =
        _isPairsMode.combine(_quickPlayers) { isPairs, players ->
            val n = players.size
            if (isPairs) n == 2 else n in 2..4
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Habilita el botón "+ NEW".
     *  - PAIRS      → solo si hay menos de 2
     *  - INDIVIDUAL → solo si hay menos de 4
     */
    val canAddPlayer: StateFlow<Boolean> =
        _isPairsMode.combine(_quickPlayers) { isPairs, players ->
            if (isPairs) players.size < 2 else players.size < 4
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /**
     * Texto de ayuda contextual bajo el grid de jugadores.
     */
    val playerHint: StateFlow<String> =
        _isPairsMode.combine(_quickPlayers) { isPairs, players ->
            val n = players.size
            when {
                isPairs  && n == 0 -> "Add 2 teams to start"
                isPairs  && n == 1 -> "Add 1 more team to start"
                isPairs  && n >= 2 -> "✓ Ready! 2 teams set"
                !isPairs && n == 0 -> "Add 2 to 4 players to start"
                !isPairs && n == 1 -> "Add at least 1 more player"
                !isPairs && n in 2..3 -> "✓ Ready! You can add ${4 - n} more"
                !isPairs && n >= 4 -> "Max players reached (4)"
                else -> ""
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Add 2 teams to start")

    fun updateTargetScore(score: Float) {
        _targetScore.value = score.toInt()
    }

    fun updateGameMode(isPairs: Boolean) {
        _isPairsMode.value = isPairs
        _quickPlayers.value = emptyList()
    }

    /**
     * Agrega un jugador a la lista de Quick Add.
     * No persiste en DB todavía; se usa al construir los [Participant] en [startGame].
     */
    fun addQuickPlayer(
        name: String,
        avatarType: AvatarType = AvatarType.PRESET_STAR,
        photoUri: String? = null
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        // Rechaza si ya se alcanzó el límite
        if (!canAddPlayer.value) return
        // Evita duplicados (mismo nombre, case-insensitive)
        if (_quickPlayers.value.any { it.name.equals(trimmed, ignoreCase = true) }) return
        _quickPlayers.value = _quickPlayers.value + PlayerUiState(
            name = trimmed,
            avatarType = avatarType,
            photoUri   = photoUri
        )
    }

    /**
     * Elimina un jugador de la lista Quick Add por nombre.
     */
    fun removeQuickPlayer(name: String) {
        _quickPlayers.value = _quickPlayers.value.filter { it.name != name }
    }

    /**
     * Crea la partida en Room con [CreateGameUseCase] y notifica a la UI con el gameId.
     *
     * Los participantes se construyen desde [_quickPlayers] si hay jugadores agregados,
     * o con los valores por defecto (Nosotros / Ellos) en modo TEAM sin jugadores.
     */
    fun startGame(onGameCreated: (Int) -> Unit) {
        if (!canStartGame.value) return   // guardia extra por si la UI falla
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newGame = Game(
                    startTime = System.currentTimeMillis(),
                    targetScore = _targetScore.value,
                    modality = if (_isPairsMode.value) GameModality.TEAM else GameModality.INDIVIDUAL,
                    status = GameStatus.ACTIVE
                )

                // Guardamos en Room y obtenemos el ID
                val gameId = createGameUseCase(newGame, buildParticipants())

                // Avisamos a la UI que ya puede navegar a la pizarra
                onGameCreated(gameId)

            }finally {
                _isLoading.value = false
            }

        }
    }

    /**
     * Construye la lista de [Participant] para Room.
     *
     * Reglas:
     * - Si hay jugadores en Quick Add → usa esos nombres.
     * - Si está en modo TEAM y no hay jugadores → crea dos equipos default.
     * - Si está en modo INDIVIDUAL y no hay jugadores → crea dos jugadores default.
     *
     * gameId = 0 porque [CreateGameUseCase] lo reemplaza con el ID real de Room.
     */
    private fun buildParticipants(): List<Participant> =
        _quickPlayers.value.mapIndexed { index, player ->
            Participant(name = player.name, seatOrder = index + 1, gameId = 0)
        }
    /*private fun buildParticipants(): List<Participant> {
        val players = _quickPlayers.value

        return if (players.isNotEmpty()) {
            players.mapIndexed { index, player ->
                Participant(
                    name      = player.name,
                    seatOrder = index + 1,
                    gameId    = 0
                )
            }
        } else {
            // Defaults según modalidad
            val defaultNames = if (_isPairsMode.value)
                listOf("Nosotros", "Ellos")
            else
                listOf("Jugador 1", "Jugador 2")

            defaultNames.mapIndexed { index, name ->
                Participant(name = name, seatOrder = index + 1, gameId = 0)
            }
        }
    }*/
}