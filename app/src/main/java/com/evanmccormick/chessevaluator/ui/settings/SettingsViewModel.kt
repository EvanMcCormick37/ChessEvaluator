package com.evanmccormick.chessevaluator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.theme.ThemeController
import com.evanmccormick.chessevaluator.ui.utils.db.DatabaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Enum for evaluation type
enum class EvalType {
    Centipawn,
    Sigmoid
}

// Settings state data class
// Available timer options in seconds
enum class TimerOption(val seconds: Int, val display: String) {
    SECONDS_5(5, "0:05"),
    SECONDS_15(15, "0:15"),
    SECONDS_30(30, "0:30"),
    MINUTES_1(60, "1:00"),
    MINUTES_2(120, "2:00"),
    MINUTES_5(300, "5:00")
}

data class SettingsState(
    val username: String = "User",
    val leaderboardVisible: Boolean = true,
    val darkMode: Boolean = true,
    val timerOption: TimerOption = TimerOption.SECONDS_30,
    val evalType: EvalType = EvalType.Centipawn,
    val tags: List<String> = listOf("Opening"),
    val currentTag: String = "Opening",
    val updateElo: Boolean = true
)

class SettingsViewModel : ViewModel() {
    // Mutable state flow to hold settings
    private val _settings = MutableStateFlow(SettingsState())

    // Exposed as immutable StateFlow
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    // Database Handling
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val dbManager = DatabaseManager(auth, db)

    init {
        // Initialize the settings with the current theme
        viewModelScope.launch {
            val isDarkTheme = ThemeController.isDarkTheme.value
            _settings.update { it.copy(darkMode = isDarkTheme) }
        }
    }

    // Functions to update settings
    fun updateUsername(username: String) {
        _settings.update { currentState ->
            currentState.copy(username = username)
        }
    }

    fun updateUsernameInDatabase(username: String) {
        viewModelScope.launch {
            try {
                dbManager.updateUsername(username)
            } catch (e: Exception) {
                println("Error updating username in database: ${e.message}")
            }
        }
    }

    fun updateLeaderboardVisibility(visible: Boolean) {
        _settings.update { currentState ->
            currentState.copy(leaderboardVisible = visible)
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        // Update the local state
        _settings.update { currentState ->
            currentState.copy(darkMode = enabled)
        }

        // Update the theme controller
        ThemeController.setDarkTheme(enabled)
    }

    fun selectTimerOption(option: TimerOption) {
        _settings.update { currentState ->
            currentState.copy(timerOption = option)
        }
    }

    fun incrementTimer() {
        _settings.update { currentState ->
            val currentIndex = TimerOption.values().indexOf(currentState.timerOption)
            val nextIndex = (currentIndex + 1).coerceAtMost(TimerOption.values().size - 1)
            currentState.copy(timerOption = TimerOption.values()[nextIndex])
        }
    }

    fun decrementTimer() {
        _settings.update { currentState ->
            val currentIndex = TimerOption.values().indexOf(currentState.timerOption)
            val prevIndex = (currentIndex - 1).coerceAtLeast(0)
            currentState.copy(timerOption = TimerOption.values()[prevIndex])
        }
    }

    fun isMinTimerSelected(): Boolean {
        return settings.value.timerOption == TimerOption.values().first()
    }

    fun isMaxTimerSelected(): Boolean {
        return settings.value.timerOption == TimerOption.values().last()
    }

    fun updateEvalType(type: EvalType) {
        _settings.update { currentState ->
            currentState.copy(evalType = type)
        }
    }

    fun updateCurrentTag(tag: String) {
        _settings.update { currentState ->
            currentState.copy(currentTag = tag)
        }
    }

    fun addTag() {
        val currentTag = settings.value.currentTag.trim()
        if (currentTag.isNotEmpty() && !settings.value.tags.contains(currentTag)) {
            _settings.update { currentState ->
                currentState.copy(
                    tags = currentState.tags + currentTag,
                    currentTag = "" // Clear the input field after adding
                )
            }
        }
    }

    fun removeTag(tag: String) {
        _settings.update { currentState ->
            currentState.copy(
                tags = currentState.tags.filter { it != tag }
            )
        }
    }

    fun updateEloMode(enabled: Boolean) {
        _settings.update { currentState ->
            currentState.copy(updateElo = enabled)
        }
    }

    // Function to save settings to persistent storage (would implement with actual storage)
    fun saveSettings() {
        // Implementation would depend on your storage solution (SharedPreferences, DataStore, etc.)
        // For now this is just a placeholder
    }

    // Function to load settings from persistent storage
    fun loadSettings() {
        // Implementation would depend on your storage solution
        // For now this is just a placeholder
    }
}