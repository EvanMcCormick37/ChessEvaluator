package com.evanmccormick.chessevaluator

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evanmccormick.chessevaluator.ui.auth.LoginScreen
import com.evanmccormick.chessevaluator.ui.auth.LoginViewModel
import com.evanmccormick.chessevaluator.ui.dashboard.DashboardScreen
import com.evanmccormick.chessevaluator.ui.dashboard.DashboardViewModel
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationScreen
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationViewModel
import com.evanmccormick.chessevaluator.ui.evaluation.TimeControl
import com.evanmccormick.chessevaluator.ui.leaderboard.LeaderboardViewModel
import com.evanmccormick.chessevaluator.ui.leaderboard.LeaderboardScreen
import com.evanmccormick.chessevaluator.ui.profile.StatsScreen
import com.evanmccormick.chessevaluator.ui.profile.StatsViewModel
import com.evanmccormick.chessevaluator.ui.settings.SettingsViewModel
import com.evanmccormick.chessevaluator.ui.settings.SettingsScreen
import com.evanmccormick.chessevaluator.ui.theme.ChessEvaluatorTheme
import com.evanmccormick.chessevaluator.ui.theme.ThemeController

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Collect the theme state
            val isDarkTheme = ThemeController.isDarkTheme.collectAsState().value
            ChessEvaluatorTheme(darkTheme = isDarkTheme){
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login"){
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard_screen"){
                        popUpTo("login"){ inclusive = true }
                    }
                }
            )
        }
        composable("dashboard_screen") {
            val dashboardViewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                navController,
                viewModel = dashboardViewModel
            )
        }
        composable("eval_screen/{timeControlDuration}",
            arguments = listOf(navArgument("timeControlDuration") { type = NavType.IntType })
        ){ backStackEntry ->
            val timeControlDuration = backStackEntry.arguments?.getInt("timeControlDuration")
            val evaluationViewModel: EvaluationViewModel = viewModel()

            LaunchedEffect(Unit) {
                val timeControl = TimeControl.allOptions.find {
                    it.durationSeconds == timeControlDuration
                }
                evaluationViewModel.setTimeControl(timeControl)
            }
            EvaluationScreen(
                navController,
                viewModel = evaluationViewModel
            )
        }
        composable("review_screen"){
            val evaluationViewModel: EvaluationViewModel = viewModel()
            EvaluationScreen(
                navController,
                viewModel = evaluationViewModel
            )
        }
        composable("stats_screen"){
            val statsViewModel: StatsViewModel = viewModel()
            StatsScreen(
                navController,
                statsViewModel
            )
        }
        composable("leaderboard_screen"){
            val leaderboardViewModel: LeaderboardViewModel = viewModel()
            LeaderboardScreen(
                navController,
                leaderboardViewModel
            )
        }
        composable("settings_screen"){
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                navController,
                settingsViewModel
            )
        }
    }
}

