package com.evanmccormick.chessevaluator.utils.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.components.navigation.AppNavBar

/**
 * A reusable wrapper that adds the app navigation bar to any screen content
 *
 * @param navController The navigation controller for handling navigation between screens
 * @param currentRoute The current route/screen name for highlighting the active nav icon
 * @param content The screen content to be displayed below the navigation bar
 */
@Composable
fun ScreenWithNavigation(
    navController: NavController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Navigation Bar
        AppNavBar(
            navController,
            currentRoute
        )
        //  Screen Content
        content()
    }
}