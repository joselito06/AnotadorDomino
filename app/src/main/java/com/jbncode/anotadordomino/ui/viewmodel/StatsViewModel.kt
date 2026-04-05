package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.model.GameStats
import com.jbncode.anotadordomino.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StatsUiState {
    object Loading : StatsUiState()
    object Empty   : StatsUiState()
    data class Success(val stats: GameStats) : StatsUiState()
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = StatsUiState.Loading
            val stats = repository.getGameStats()
            _uiState.value = if (stats.totalGames == 0) StatsUiState.Empty
            else StatsUiState.Success(stats)
        }
    }
}