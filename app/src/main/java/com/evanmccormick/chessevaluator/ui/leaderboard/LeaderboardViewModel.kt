package com.evanmccormick.chessevaluator.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.utils.db.DatabaseManager
import com.evanmccormick.chessevaluator.ui.utils.db.LeaderboardUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    val entries: List<LeaderboardEntry>,
    val timeControl: Int // Store the time control value for this tab
)

data class LeaderboardState(
    val isLoading: Boolean = true,
    val tabs: List<LeaderboardTab> = emptyList(),
    val currentTabIndex: Int = 2, // Default to 30 Seconds tab (index 2)
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = listOf(
        "Opening", "Middlegame", "Endgame", "Tactics",
        "Strategy", "Positional", "Attacking", "Defending"
    ),
    val errorMessage: String? = null,
    val showTagSelector: Boolean = false
)

class LeaderboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state.asStateFlow()

    // Database Handling
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val dbManager = DatabaseManager(auth, db)

    // Define time controls
    private val timeControls = listOf(
        Triple("5 Second Leaders", "0:05", 5),
        Triple("15 Second Leaders", "0:15", 15),
        Triple("30 Second Leaders", "0:30", 30),
        Triple("1 Minute Leaders", "1:00", 60),
        Triple("2 Minute Leaders", "2:00", 120),
        Triple("5 Minute Leaders", "5:00", 300)
    )

    init {
        loadLeaderboards()
    }

    private fun loadLeaderboards() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Create empty tabs first with proper structure
                val emptyTabs = timeControls.map { (title, tabTitle, timeControlValue) ->
                    LeaderboardTab(
                        title = title,
                        tabTitle = tabTitle,
                        entries = emptyList(),
                        timeControl = timeControlValue
                    )
                }
                _state.update { it.copy(tabs = emptyTabs) }

                // Load data for the current tab
                loadCurrentTabData()

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

    // Load data only for the current tab to improve performance
    private suspend fun loadCurrentTabData() {
        val currentTabIndex = _state.value.currentTabIndex
        if (currentTabIndex >= 0 && currentTabIndex < _state.value.tabs.size) {
            val currentTab = _state.value.tabs[currentTabIndex]
            val timeControl = currentTab.timeControl

            // Fetch leaderboard data from Firebase
            val leaderboardUsers = dbManager.getLeaderboard(timeControl)

            // Convert to UI entries
            val entries = leaderboardUsers.map { user ->
                LeaderboardEntry(
                    rank = user.rank,
                    name = user.username,
                    elo = user.elo,
                    isCurrentUser = user.isCurrentUser
                )
            }

            // Update just the current tab
            val updatedTabs = _state.value.tabs.toMutableList()
            updatedTabs[currentTabIndex] = currentTab.copy(entries = entries)

            _state.update {
                it.copy(
                    tabs = updatedTabs,
                    isLoading = false
                )
            }
        }
    }

    fun changeTab(index: Int) {
        if (index in _state.value.tabs.indices && index != _state.value.currentTabIndex) {
            _state.update { it.copy(currentTabIndex = index, isLoading = true) }

            // Load data for the newly selected tab
            viewModelScope.launch {
                loadCurrentTabData()
            }
        }
    }

    fun findMe() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val currentTabIndex = _state.value.currentTabIndex
                val currentTab = _state.value.tabs[currentTabIndex]
                val timeControl = currentTab.timeControl

                // Get user's position on the leaderboard
                val userPosition = dbManager.getUserLeaderboardPosition(timeControl)

                if (userPosition != null) {
                    // Fetch an updated leaderboard centered around the user's position
                    val leaderboardUsers = dbManager.getLeaderboard(timeControl)

                    // Convert to UI entries
                    val entries = leaderboardUsers.map { user ->
                        LeaderboardEntry(
                            rank = user.rank,
                            name = user.username,
                            elo = user.elo,
                            isCurrentUser = user.id == userPosition.id
                        )
                    }

                    // Update the current tab
                    val updatedTabs = _state.value.tabs.toMutableList()
                    updatedTabs[currentTabIndex] = currentTab.copy(entries = entries)

                    _state.update {
                        it.copy(
                            tabs = updatedTabs,
                            isLoading = false
                        )
                    }

                    // TODO: Scroll to user's position in the UI
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to find your position: ${e.message}"
                    )
                }
            }
        }
    }

    fun addTag(tag: String) {
        if (!_state.value.selectedTags.contains(tag)) {
            _state.update {
                it.copy(selectedTags = it.selectedTags + tag)
            }
            // In a real implementation, this would trigger filtering
            // of the leaderboard data based on the selected tags
            filterByTags()
        }
    }

    fun removeTag(tag: String) {
        _state.update {
            it.copy(selectedTags = it.selectedTags - tag)
        }
        // Remove the tag filter
        filterByTags()
    }

    fun showTagSelector() {
        _state.update { it.copy(showTagSelector = true) }
    }

    fun hideTagSelector() {
        _state.update { it.copy(showTagSelector = false) }
    }

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    // Note: This is a placeholder for tag filtering
    // In a real implementation, you would query Firestore with a tag filter
    private fun filterByTags() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Reload current tab data
                // In a real implementation, you would pass tags to the database query
                loadCurrentTabData()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to apply filters: ${e.message}"
                    )
                }
            }
        }
    }
}