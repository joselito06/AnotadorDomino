package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.R
import com.jbncode.anotadordomino.domain.model.AvatarType
import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.domain.model.GameModality
import com.jbncode.anotadordomino.domain.model.GameStatus
import com.jbncode.anotadordomino.domain.model.Participant
import com.jbncode.anotadordomino.domain.usecase.CreateGameUseCase
import com.jbncode.anotadordomino.ui.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PlayerUiState(
    val name: String,
    val wins: Int = 0,
    val avatarType: AvatarType = AvatarType.PRESET_STAR,
    /** URI de la foto elegida desde galería (solo válido cuando avatarType == GALLERY) */
    val photoUri: String? = null,
    val id: String = UUID.randomUUID().toString()
)
//data class PlayerLimits(val min: Int, val max: Int)

// NUEVO: Enum para saber en qué paso del tutorial estamos
enum class SetupTutorialStep { ADD_PLAYERS, START_MATCH }
enum class PlayerResult { OK, NAME_TAKEN, INVALID, FULL }

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    // ── Configuración de la partida ────────────────────────────────────────
    private val maxPlayers get() = if (_isPairsMode.value) 2 else 4

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

    private val autoAvatars = listOf(
        AvatarType.PRESET_STAR, AvatarType.PRESET_BOLT, AvatarType.PRESET_CROWN,
        AvatarType.PRESET_FIRE, AvatarType.PRESET_DIAMOND, AvatarType.PRESET_SHIELD,
        AvatarType.PRESET_ROBOT, AvatarType.PRESET_WIZARD
    )

    // ── Derived state (combine isPairsMode + quickPlayers) ─────────────────

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

    val tutorialStep: StateFlow<SetupTutorialStep> =
        canStartGame.map { canStart ->
            if (canStart) SetupTutorialStep.START_MATCH else SetupTutorialStep.ADD_PLAYERS
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SetupTutorialStep.ADD_PLAYERS)

    /**
     * Texto de ayuda contextual bajo el grid de jugadores.
     */
    val playerHint: StateFlow<UiText> =
        _isPairsMode.combine(_quickPlayers) { isPairs, players ->
            val n = players.size
            when {
                isPairs  && n == 0 -> UiText.StringResource(R.string.setup_hint_add_teams)
                isPairs  && n == 1 -> UiText.StringResource(R.string.setup_hint_add_1_team)
                isPairs  && n == 2 -> UiText.StringResource(R.string.setup_hint_ready_teams)
                !isPairs && n == 0 -> UiText.StringResource(R.string.setup_hint_add_players)
                !isPairs && n == 1 -> UiText.StringResource(R.string.setup_hint_add_1_player)
                !isPairs && n in 2..3 -> UiText.StringResource(R.string.setup_hint_ready_add_more, 4 - n)
                !isPairs && n == 4 -> UiText.StringResource(R.string.setup_hint_max_players)
                else -> UiText.DynamicString("")
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiText.StringResource(R.string.setup_hint_add_teams))

    private fun nameTaken(name: String, exceptId: String? = null) =
        _quickPlayers.value.any { it.id != exceptId && it.name.equals(name, ignoreCase = true) }

    private fun canStartNow(): Boolean {
        val n = _quickPlayers.value.size
        return if (_isPairsMode.value) n == 2 else n in 2..4
    }

    fun updateTargetScore(score: Float) {
        _targetScore.value = score.toInt()
    }

    /*fun updateGameMode(isPairs: Boolean) {
        _isPairsMode.value = isPairs
        _quickPlayers.value = emptyList()
    }*/

    /**
     * Cambia de modo sin borrar jugadores y renombra los nombres automáticos:
     * "Equipo N" <-> "Jugador N". Los nombres personalizados no se tocan.
     */
    fun updateGameMode(isPairs: Boolean, teamPrefix: String, playerPrefix: String) {
        if (_isPairsMode.value == isPairs) return
        _isPairsMode.value = isPairs

        val oldPrefix = if (isPairs) playerPrefix else teamPrefix
        val newPrefix = if (isPairs) teamPrefix else playerPrefix
        val kept = _quickPlayers.value.take(if (isPairs) 2 else 4)

        val pattern = Regex("^${Regex.escape(oldPrefix)} (\\d+)$", RegexOption.IGNORE_CASE)
        val used = kept.filterNot { pattern.matches(it.name) }.map { it.name.lowercase() }.toMutableSet()

        _quickPlayers.value = kept.map { p ->
            val m = pattern.find(p.name) ?: return@map p
            var n = m.groupValues[1].toIntOrNull() ?: 1
            while ("$newPrefix $n".lowercase() in used) n++
            val newName = "$newPrefix $n"
            used += newName.lowercase()
            p.copy(name = newName)
        }
    }

    /** Añade un jugador con nombre y avatar automáticos. [namePrefix] viene de strings.xml. */
    fun quickAddPlayer(namePrefix: String): PlayerResult {
        val current = _quickPlayers.value
        if (current.size >= maxPlayers) return PlayerResult.FULL
        var n = current.size + 1
        while (nameTaken("$namePrefix $n")) n++
        val usedAvatars = current.map { it.avatarType }.toSet()
        val avatar = autoAvatars.firstOrNull { it !in usedAvatars } ?: AvatarType.PRESET_STAR
        _quickPlayers.value = current + PlayerUiState(name = "$namePrefix $n", avatarType = avatar)
        return PlayerResult.OK
    }

    /** Edita nombre y avatar de un jugador existente (se identifica por su nombre actual). */
    fun updatePlayer(id: String, newName: String, avatarType: AvatarType, photoUri: String?): PlayerResult {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return PlayerResult.INVALID
        if (nameTaken(trimmed, exceptId = id)) return PlayerResult.NAME_TAKEN
        _quickPlayers.value = _quickPlayers.value.map {
            if (it.id == id) it.copy(name = trimmed, avatarType = avatarType, photoUri = photoUri) else it
        }
        return PlayerResult.OK
    }

    /**
     * Agrega un jugador a la lista de Quick Add.
     * No persiste en DB todavía; se usa al construir los [Participant] en [startGame].
     */
    fun addQuickPlayer(
        name: String,
        avatarType: AvatarType = AvatarType.PRESET_STAR,
        photoUri: String? = null
    ): PlayerResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return PlayerResult.INVALID
        if (_quickPlayers.value.size >= maxPlayers) return PlayerResult.FULL
        if (nameTaken(trimmed)) return PlayerResult.NAME_TAKEN
        _quickPlayers.value += PlayerUiState(name = trimmed, avatarType = avatarType, photoUri = photoUri)
        return PlayerResult.OK
    }

    /**
     * Elimina un jugador de la lista Quick Add por nombre.
     */
    fun removeQuickPlayer(id: String) {
        _quickPlayers.value = _quickPlayers.value.filter { it.id != id  }
    }

    /**
     * Crea la partida en Room con [CreateGameUseCase] y notifica a la UI con el gameId.
     *
     * Los participantes se construyen desde [_quickPlayers] si hay jugadores agregados,
     * o con los valores por defecto (Nosotros / Ellos) en modo TEAM sin jugadores.
     */
    fun startGame(onGameCreated: (Int) -> Unit) {
        //if (!canStartGame.value) return   // guardia extra por si la UI falla
        if (!canStartNow()) return
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
            Participant(
                name = player.name,
                seatOrder = index + 1,
                gameId = 0,
                avatarType = player.avatarType.name,
                photoUri = player.photoUri
            )
        }

}