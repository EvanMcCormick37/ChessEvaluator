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
    val elo: Int
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
    val currentTabIndex: Int = 1, // Default to 15 Seconds tab (index 1)
    val userEntry: LeaderboardEntry? = null,
    val showUserInfoBox: Boolean = false,
    val errorMessage: String? = null
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
                    elo = user.elo
                )
            }

            // Fetch user info from Firebase
            val userInfo = dbManager.getUserLeaderboardInfo(timeControl)

            val userLeaderboardEntry = userInfo?.let {
                LeaderboardEntry(
                    rank = it.rank,
                    name = it.username,
                    elo = it.elo
                )
            }

            // Update just the current tab
            val updatedTabs = _state.value.tabs.toMutableList()
            updatedTabs[currentTabIndex] = currentTab.copy(entries = entries)

            _state.update {
                it.copy(
                    tabs = updatedTabs,
                    userEntry = userLeaderboardEntry,
                    showUserInfoBox = userInfo != null,
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

    fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }
    
}