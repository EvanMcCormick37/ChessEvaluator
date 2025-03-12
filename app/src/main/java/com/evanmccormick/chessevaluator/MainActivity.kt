package com.evanmccormick.chessevaluator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.evanmccormick.chessevaluator.ui.auth.LoginScreen
import com.evanmccormick.chessevaluator.ui.auth.LoginViewModel
import com.evanmccormick.chessevaluator.ui.dashboard.DashboardScreen
import com.evanmccormick.chessevaluator.ui.dashboard.DashboardViewModel
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationScreen
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationViewModel
import com.evanmccormick.chessevaluator.ui.theme.ChessEvaluatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChessEvaluatorTheme {
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
        composable("play_screen"){
            val evaluationViewModel: EvaluationViewModel = viewModel()
            EvaluationScreen(
                navController,
                viewModel = evaluationViewModel
            )
        }
        composable("review_screen"){
            //  TODO: Add ReviewScreen Implementation
        }
        composable("profile_screen"){
            //  TODO: Add ProfileScreen Implementation
        }
        composable("leaderboard_screen"){
            //  TODO: Add LeaderboardScreen implementation
        }
        composable("settings_screen"){
            //  TODO: Add SettingsScreen implementation
        }
    }
}

