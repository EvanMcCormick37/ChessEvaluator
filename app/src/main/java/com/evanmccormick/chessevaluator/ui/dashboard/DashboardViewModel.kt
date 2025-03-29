package com.evanmccormick.chessevaluator.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.evaluation.TimeControl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    // Navigation events as a shared flow to be observed by the UI
    private val _navigationEvent = MutableSharedFlow<NavigationDestination>()
    val navigationEvent: SharedFlow<NavigationDestination> = _navigationEvent

    // Store the selected time control
    private val _selectedTimeControl = MutableStateFlow<TimeControl?>(null)
    val selectedTimeControl: StateFlow<TimeControl?> = _selectedTimeControl.asStateFlow()

    // Function to set the selected time control and navigate
    fun navigateToEvalWithTimeControl(timeControl: TimeControl) {
        viewModelScope.launch {
            _selectedTimeControl.value = timeControl
            navigateTo(NavigationDestination.Eval(timeControl.durationSeconds))
        }
    }
    fun navigateTo(destination: NavigationDestination) {
        viewModelScope.launch {
            _navigationEvent.emit(destination)
        }
    }
    sealed class NavigationDestination {
        data class Eval(val timeControlDuration: Int) : NavigationDestination()
        object Stats : NavigationDestination()
        object Leaderboard : NavigationDestination()
        object Settings : NavigationDestination()
        object Donate : NavigationDestination()
    }
}