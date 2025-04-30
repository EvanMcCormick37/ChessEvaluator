package com.evanmccormick.chessevaluator.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    LaunchedEffect(key1 = true){
        viewModel.navigationEvent.collect { destination ->
            when (destination) {
                is DashboardViewModel.NavigationDestination.Survival -> navController.navigate("survival_screen/${destination.timeControlDuration}")
                is DashboardViewModel.NavigationDestination.Eval -> navController.navigate("eval_screen/${destination.timeControlDuration}")
                is DashboardViewModel.NavigationDestination.Leaderboard -> navController.navigate("leaderboard_screen")
                is DashboardViewModel.NavigationDestination.SurvivalLeaderboard -> navController.navigate("survival_leaderboard_screen")
                is DashboardViewModel.NavigationDestination.Settings -> navController.navigate("settings_screen")
                is DashboardViewModel.NavigationDestination.Donate -> navController.navigate("donate_screen")
            }
        }
    }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {// Main large buttons (Play and Review)
            EvalButton(
                text = "Play",
                onClick = { selectedTimeControl ->
                    viewModel.navigateWithTimeControlTo(DashboardViewModel.NavigationDestination::Eval, selectedTimeControl) }
            )
            EvalButton(
                text = "Survival",
                onClick = { selectedTimeControl ->
                    viewModel.navigateWithTimeControlTo(DashboardViewModel.NavigationDestination::Survival, selectedTimeControl) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Secondary smaller buttons (Stats, Leaderboard, Settings, Donate)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Leaderboard) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(text = "Eval Leaderboard")
                    }
                    Button(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.SurvivalLeaderboard) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(text = "Survival Leaderboard")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Settings) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(text = "Settings")
                    }

                    Button(
                        onClick = {
                            // Launch the intent to open your Ko-fi page
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://ko-fi.com/B0B81E9CGS".toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(text = "Donate")
                    }
                }
            }
        }
    }
}