package com.evanmccormick.chessevaluator.utils.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.components.navigation.AppNavBar

@Composable
fun ScreenWithNavigation(
    navController: NavController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            content()
        }

        // Navigation bar at the bottom
        AppNavBar(
            navController = navController,
            currentRoute = currentRoute
        )
    }
}