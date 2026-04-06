package com.jbncode.anotadordomino.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbncode.anotadordomino.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppStartDestination {
    object Checking : AppStartDestination()             // splash/loading inicial
    object Setup : AppStartDestination()                 // no hay partida pendiente
    data class ResumeGame(val gameId: Int) : AppStartDestination()  // hay partida activa/pausada
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<AppStartDestination>(AppStartDestination.Checking)
    val startDestination: StateFlow<AppStartDestination> = _startDestination.asStateFlow()

    init {
        checkForActiveGame()
    }

    private fun checkForActiveGame() {
        viewModelScope.launch {
            val activeGame = repository.getActiveGame()
            _startDestination.value = if (activeGame != null) {
                AppStartDestination.ResumeGame(activeGame.id)
            } else {
                AppStartDestination.Setup
            }
        }
    }

    // NUEVO: Función para limpiar el estado y evitar re-navegaciones
    fun onNavigationConsumed() {
        _startDestination.value = AppStartDestination.Setup
    }
}