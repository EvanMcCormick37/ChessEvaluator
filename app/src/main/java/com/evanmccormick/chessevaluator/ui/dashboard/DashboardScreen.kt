package com.evanmccormick.chessevaluator.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
                is DashboardViewModel.NavigationDestination.Settings -> navController.navigate("settings_screen")
                is DashboardViewModel.NavigationDestination.Donate -> navController.navigate("donate_screen")
            }
        }
    }

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
                text = "Survival Mode",
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
                        Text(text = "Leaderboard")
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
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Donate) },
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