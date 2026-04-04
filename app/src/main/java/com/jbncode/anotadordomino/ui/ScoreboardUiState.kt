package com.jbncode.anotadordomino.ui

import com.jbncode.anotadordomino.domain.model.Game
import com.jbncode.anotadordomino.ui.viewmodel.HandLogUi
import com.jbncode.anotadordomino.ui.viewmodel.ParticipantScoreUi

/**
 * Estado global de la pantalla del Scoreboard.
 */

open class ScoreboardUiState {
    object Loading : ScoreboardUiState()
    data class Error(val message: String) : ScoreboardUiState()
    data class Active(
        val game: Game,
        val participants: List<ParticipantScoreUi>,
        val handLog: List<HandLogUi>,
        val isFinished: Boolean,
        val winner: ParticipantScoreUi? = null
    ) : ScoreboardUiState()
}
