package com.evanmccormick.chessevaluator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.theme.AppSettingsController
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
    Raw,
    Sigmoid
}

data class SettingsState(
    val username: String = "User",
    val leaderboardVisible: Boolean = true,
    val darkMode: Boolean = true,
    val evalType: EvalType = EvalType.Raw,
    val updateElo: Boolean = true,
    val isDeleteLoading: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val deleteError: String? = null,
    val accountDeleted: Boolean = false
)

class SettingsViewModel : ViewModel() {
    // Mutable state flow to hold settings
    private val _settings = MutableStateFlow(
        SettingsState()
    )

    // Exposed as immutable StateFlow
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    // Database Handling
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val dbManager = DatabaseManager(auth, db)

    init {
        // Initialize the settings for the current user
        viewModelScope.launch {
            val isDarkTheme = AppSettingsController.isDarkTheme.value
            val evalType = AppSettingsController.evalType.value
            val updateElo = AppSettingsController.updateElo.value

            val userInfo = dbManager.getUserInfo()
            _settings.update { it.copy(
                darkMode = isDarkTheme,
                username = userInfo.username,
                evalType = evalType,
                updateElo = updateElo
            ) }
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

    fun updateDarkMode(enabled: Boolean) {
        // Update the local state
        _settings.update { currentState ->
            currentState.copy(darkMode = enabled)
        }
        //Update the global state so the rest of the app can see it
        AppSettingsController.setDarkTheme(enabled)
    }

    fun updateEvalType(type: EvalType) {
        _settings.update { currentState ->
            currentState.copy(evalType = type)
        }

        AppSettingsController.setEvalType(type)
    }

    fun updateEloMode(enabled: Boolean) {
        _settings.update { currentState ->
            currentState.copy(updateElo = enabled)
        }

        AppSettingsController.setUpdateElo(enabled)
    }

    // Delete account functions
    fun showDeleteConfirmation() {
        _settings.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _settings.update { it.copy(showDeleteConfirmation = false, deleteError = null) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _settings.update { it.copy(isDeleteLoading = true, deleteError = null) }

                dbManager.deleteUser()

                _settings.update {
                    it.copy(
                        isDeleteLoading = false,
                        showDeleteConfirmation = false,
                        accountDeleted = true
                    )
                }
            } catch (e: Exception) {
                _settings.update {
                    it.copy(
                        isDeleteLoading = false,
                        deleteError = e.message ?: "Failed to delete account"
                    )
                }
            }
        }
    }

    fun clearDeleteError() {
        _settings.update { it.copy(deleteError = null) }
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