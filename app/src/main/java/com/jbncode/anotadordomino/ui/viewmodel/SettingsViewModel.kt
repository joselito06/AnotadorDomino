package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.data.repository.SettingsRepository
import com.jbncode.anotadordomino.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/*data class SettingsUiState(
    val isDarkMode: Boolean      = true,
    val capicuaEnabled: Boolean  = true,
    val tranqueEnabled: Boolean  = false,
    val isResetting: Boolean     = false,
    val resetSuccess: Boolean    = false
)*/

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    // Exponer isDarkMode como StateFlow para que MainActivity lo observe
    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val capicuaEnabled: StateFlow<Boolean> = settingsRepository.capicuaPointsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val tranqueEnabled: StateFlow<Boolean> = settingsRepository.doubleTranqueEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportCsvEvent = MutableSharedFlow<String>()
    val exportCsvEvent = _exportCsvEvent.asSharedFlow()

    private val _isResetting  = MutableStateFlow(false)
    val isResetting: StateFlow<Boolean> = _isResetting.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    val appLanguage: StateFlow<String> = settingsRepository.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")

    // ── Actions ────────────────────────────────────────────────────────────

    fun setLanguage(langTag: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(langTag)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkMode(enabled) }
    }

    fun setCapicuaPoints(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCapicuaPoints(enabled) }
    }

    fun setDoubleTranque(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDoubleTranque(enabled) }
    }

    fun exportMatchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _isExporting.value = true
            try {
                // 1. Obtenemos todos los juegos usando first() para leer el Flow actual
                val games = gameRepository.observeAllGames().first()

                val csv = java.lang.StringBuilder()
                // Cabeceras del archivo Excel/CSV
                csv.append("Game ID,Modality,Status,Target Score,Players & Scores\n")

                // 2. Llenamos los datos cruzando las tablas
                games.forEach { game ->
                    // Usamos el repository para obtener los participantes y sus puntajes
                    val participants = gameRepository.getParticipants(game.id)
                    val scores = gameRepository.getScoresForGame(game.id)

                    val playerDetails = participants.joinToString(" | ") { p ->
                        "${p.name.uppercase()}: ${scores[p.id] ?: 0} pts"
                    }

                    csv.append("${game.id},${game.modality},${game.status},${game.targetScore},$playerDetails\n")
                }

                // 3. Enviamos el texto resultante a la UI
                _exportCsvEvent.emit(csv.toString())
            } catch (e: Exception) {
                e.printStackTrace() // Manejo de error silencioso o puedes emitir un estado de error
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _isResetting.value = true
            try {
                gameRepository.deleteAllData()
                _resetSuccess.value = true
            } finally {
                _isResetting.value = false
            }
        }
    }

    fun resetSuccessAcknowledged() {
        _resetSuccess.value = false
    }
}