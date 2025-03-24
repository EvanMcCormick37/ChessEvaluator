package com.evanmccormick.chessevaluator.ui.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TagEloData(
    val tag: String,
    val elo: Int
)

data class StatsState(
    val currentTimeControlIndex: Int = 2, // Default to 30 seconds (index 2)
    val timeControls: List<String> = listOf("0:05", "0:15", "0:30", "1:00", "2:00", "5:00"),
    val isShowingStrengths: Boolean = true, // True for strengths, false for weaknesses
    val strengthsByTag: List<TagEloData> = emptyList(),
    val weaknessesByTag: List<TagEloData> = emptyList(),
    val overallElo: Int = 1650,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StatsViewModel : ViewModel() {
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        loadUserStats()
    }

    private fun loadUserStats() {
        // In a real app, this would fetch data from a repository
        // For now, we'll load mock data
        val mockStrengths = listOf(
            TagEloData("Endgame", 1670),
            TagEloData("Middlegame", 1640),
            TagEloData("Closed Position", 1627),
            TagEloData("Open Position", 1618),
            TagEloData("Opening", 1616),
            TagEloData("Tactical", 1605)
        )

        val mockWeaknesses = listOf(
            TagEloData("Knight Endgames", 1580),
            TagEloData("Rook Endgames", 1570),
            TagEloData("King Activity", 1565),
            TagEloData("Pawn Structure", 1550),
            TagEloData("Queen vs Rook", 1545),
            TagEloData("Bishop Pair", 1520)
        )

        _state.update { currentState ->
            currentState.copy(
                strengthsByTag = mockStrengths,
                weaknessesByTag = mockWeaknesses,
                isLoading = false
            )
        }
    }

    fun onTimeControlSelected(index: Int) {
        if (index in _state.value.timeControls.indices) {
            _state.update { it.copy(currentTimeControlIndex = index) }
            // In a real app, this would trigger loading stats for the selected time control
            loadUserStats()
        }
    }

    fun toggleStrengthsWeaknesses() {
        _state.update { it.copy(isShowingStrengths = !it.isShowingStrengths) }
    }

    fun setShowingStrengths(isShowingStrengths: Boolean) {
        _state.update { it.copy(isShowingStrengths = isShowingStrengths) }
    }
}