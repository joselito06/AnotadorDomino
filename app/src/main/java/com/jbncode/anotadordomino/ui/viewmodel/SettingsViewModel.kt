package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.data.repository.SettingsRepository
import com.jbncode.anotadordomino.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean      = true,
    val capicuaEnabled: Boolean  = true,
    val tranqueEnabled: Boolean  = false,
    val isResetting: Boolean     = false,
    val resetSuccess: Boolean    = false
)

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

    private val _isResetting  = MutableStateFlow(false)
    val isResetting: StateFlow<Boolean> = _isResetting.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    val appLanguage: StateFlow<String> = settingsRepository.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "en")

    var onNavigateToRestartScreen: (() -> Unit)? = null

    // ── Actions ────────────────────────────────────────────────────────────

    fun setLanguage(langTag: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(langTag)

            /*withContext(Dispatchers.Main){
                onNavigateToRestartScreen?.invoke()
            }*/
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