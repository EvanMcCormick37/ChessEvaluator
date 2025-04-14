package com.evanmccormick.chessevaluator.ui.theme

import android.content.Context
import com.evanmccormick.chessevaluator.ui.settings.EvalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for managing app settings
 */
object AppSettingsController {
    // Dark theme state (existing from ThemeController)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Eval type state
    private val _evalType = MutableStateFlow(EvalType.Raw)
    val evalType: StateFlow<EvalType> = _evalType.asStateFlow()

    // Update Elo state
    private val _updateElo = MutableStateFlow(true)
    val updateElo: StateFlow<Boolean> = _updateElo.asStateFlow()

    // Update methods
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun setEvalType(type: EvalType) {
        _evalType.value = type
    }

    fun setUpdateElo(update: Boolean) {
        _updateElo.value = update
    }

    // Persistence with SharedPreferences
    fun saveToPreferences(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("dark_theme", _isDarkTheme.value)
            .putString("eval_type", _evalType.value.name)
            .putBoolean("update_elo", _updateElo.value)
            .apply()
    }

    fun loadFromPreferences(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("dark_theme", true)
        _evalType.value = EvalType.valueOf(prefs.getString("eval_type", EvalType.Raw.name)!!)
        _updateElo.value = prefs.getBoolean("update_elo", true)
    }
}