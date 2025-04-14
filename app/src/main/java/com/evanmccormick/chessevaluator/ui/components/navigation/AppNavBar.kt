package com.evanmccormick.chessevaluator.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme

@Composable
fun AppNavBar(
    navController: NavController,
    currentRoute: String? = null
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ExtendedTheme.colors.navBarColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //  Settings Icon
        if(currentRoute != "settings_screen") {
            NavBarIcon(
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
                onClick = {
                    navController.navigate("settings_screen") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        //  Home Icon
        if(currentRoute != "dashboard_screen") {
            NavBarIcon(
                icon = Icons.Filled.Home,
                contentDescription = "Home",
                onClick = {
                    navController.navigate("dashboard_screen") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        //  Leaderboard Icon
        if(currentRoute != "leaderboard_screen") {
            NavBarIcon(
                icon = Icons.Filled.Star,
                contentDescription = "Leaderboard",
                onClick = {
                    navController.navigate("leaderboard_screen") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}