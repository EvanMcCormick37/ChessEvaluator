package com.evanmccormick.chessevaluator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for managing app theme settings
 */
object ThemeController {
    // State to track if dark mode is enabled
    private val _isDarkTheme = MutableStateFlow(true) // Default to dark theme
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Update the theme setting
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    // Composable to easily access the current theme state
    @Composable
    fun isDarkThemeState(): State<Boolean> {
        return remember { mutableStateOf(isDarkTheme.value) }
    }
}