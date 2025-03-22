package com.evanmccormick.chessevaluator.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val elo: Int,
    val isCurrentUser: Boolean = false
)

data class LeaderboardTab(
    val title: String,
    val tabTitle: String,
    val entries: List<LeaderboardEntry>
)

data class LeaderboardState(
    val isLoading: Boolean = false,
    val tabs: List<LeaderboardTab> = emptyList(),
    val currentTabIndex: Int = 2, // Default to 30 Seconds tab (index 2)
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = listOf(
        "Opening", "Middlegame", "Endgame", "Tactics",
        "Strategy", "Positional", "Attacking", "Defending"
    ),
    val errorMessage: String? = null
)

class LeaderboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state.asStateFlow()

    // Mock data for each time control
    private val mockData = listOf(
        Triple("5 Second Leaders", "0:05", generateMockEntries(prefix = "Bullet")),
        Triple("15 Second Leaders", "0:15", generateMockEntries(prefix = "Blitz")),
        Triple("30 Second Leaders", "0:30", generateMockEntries(prefix = "Rapid")),
        Triple("1 Minute Leaders","1:00",generateMockEntries(prefix = "Steady")),
        Triple("2 Minute Leaders","2:00",generateMockEntries(prefix = "Careful")),
        Triple("5 Minute Leaders","5:00",generateMockEntries(prefix = "Classical"))
    )

    init {
        loadLeaderboards()
    }

    private fun loadLeaderboards() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Simulate network delay
                delay(500)

                // Create tabs from mock data
                val tabs = mockData.map{
                    (title,tabTitle,entries)->LeaderboardTab(title,tabTitle,entries)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        tabs = tabs,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load leaderboards: ${e.message}"
                    )
                }
            }
        }
    }

    fun changeTab(index: Int) {
        if (index in _state.value.tabs.indices) {
            _state.update { it.copy(currentTabIndex = index) }
        }
    }

    fun findMe() {
        // In the future, this would locate the user in the leaderboard
        // For now, we'll just highlight the 30th position (index 29)
        val targetIndex = 29 // Index for position 30 (0-based indexing)

        val updatedTabs = _state.value.tabs.mapIndexed { tabIndex, tab ->
            if (tabIndex == _state.value.currentTabIndex) {
                val updatedEntries = tab.entries.mapIndexed { entryIndex, entry ->
                    entry.copy(isCurrentUser = entryIndex == targetIndex)
                }
                tab.copy(entries = updatedEntries)
            } else {
                tab
            }
        }

        _state.update { it.copy(tabs = updatedTabs) }
    }

    fun addTag(tag: String) {
        if (!_state.value.selectedTags.contains(tag)) {
            _state.update {
                it.copy(selectedTags = it.selectedTags + tag)
            }
            // In a real implementation, this would trigger filtering
            // of the leaderboard data based on the selected tags
        }
    }

    fun removeTag(tag: String) {
        _state.update {
            it.copy(selectedTags = it.selectedTags - tag)
        }
        // In a real implementation, this would trigger filtering
        // of the leaderboard data based on the selected tags
    }

    // Helper to generate mock data
    private fun generateMockEntries(prefix: String): List<LeaderboardEntry> {
        val names = listOf("Paul", "Raul", "Rail", "Pail", "Nail", "Snail", "Tail", "Mail",
            "Hail", "Dale", "Gale", "Yale", "Nial", "Karl", "Earl")
        return (1..30).map { rank ->
            val elo = when (rank) {
                1 -> 1980
                2 -> 1970
                3 -> 1960
                else -> (1900 - (rank - 3) * 10).coerceAtLeast(1100)
            }

            LeaderboardEntry(
                rank = rank,
                name = if (rank <= names.size) names[rank - 1] else "$prefix Player $rank",
                elo = elo
            )
        }
    }
}