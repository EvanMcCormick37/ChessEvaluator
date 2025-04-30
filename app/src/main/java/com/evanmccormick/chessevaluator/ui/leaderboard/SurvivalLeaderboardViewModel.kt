package com.evanmccormick.chessevaluator.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.utils.db.DatabaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SurvivalLeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
)

data class SurvivalLeaderboardTab(
    val title: String,
    val tabTitle: String,
    val entries: List<SurvivalLeaderboardEntry>,
    val timeControl: Int
)

data class SurvivalLeaderboardState(
    val isLoading: Boolean = true,
    val tabs: List<SurvivalLeaderboardTab> = emptyList(),
    val currentTabIndex: Int = 1, // Default to 15 Seconds tab (index 1)
    val userEntry: SurvivalLeaderboardEntry? = null,
    val showUserInfoBox: Boolean = false,
    val errorMessage: String? = null
)

class SurvivalLeaderboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(SurvivalLeaderboardState())
    val state: StateFlow<SurvivalLeaderboardState> = _state.asStateFlow()

    // Database Handling
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val dbManager = DatabaseManager(auth, db)

    // Define time controls - same as regular leaderboard
    private val timeControls = listOf(
        Triple("5 Second Survival", "0:05", 5),
        Triple("15 Second Survival", "0:15", 15),
        Triple("30 Second Survival", "0:30", 30),
        Triple("1 Minute Survival", "1:00", 60),
        Triple("2 Minute Survival", "2:00", 120),
        Triple("5 Minute Survival", "5:00", 300)
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
                    SurvivalLeaderboardTab(
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
                        errorMessage = "Failed to load survival leaderboards: ${e.message}"
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
            val leaderboardEntries = dbManager.getSurvivalLeaderboard(timeControl)

            // Convert to UI entries
            val entries = leaderboardEntries.map { user ->
                SurvivalLeaderboardEntry(
                    rank = user.rank,
                    name = user.username,
                    score = user.score,
                )
            }

            // Fetch user info from Firebase
            val userInfo = dbManager.getUserSurvivalLeaderboardInfo(timeControl)

            val userLeaderboardEntry = userInfo?.let {
                SurvivalLeaderboardEntry(
                    rank = it.rank,
                    name = it.username,
                    score = it.score,
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