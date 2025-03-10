package com.evanmccormick.chessevaluator.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    // Navigation events as a shared flow to be observed by the UI
    private val _navigationEvent = MutableSharedFlow<NavigationDestination>()
    val navigationEvent: SharedFlow<NavigationDestination> = _navigationEvent

    fun navigateTo(destination: NavigationDestination) {
        viewModelScope.launch {
            _navigationEvent.emit(destination)
        }
    }
    sealed class NavigationDestination {
        object Play : NavigationDestination()
        object Review : NavigationDestination()
        object Profile : NavigationDestination()
        object Leaderboard : NavigationDestination()
        object Settings : NavigationDestination()
        object Donate : NavigationDestination()
    }
}