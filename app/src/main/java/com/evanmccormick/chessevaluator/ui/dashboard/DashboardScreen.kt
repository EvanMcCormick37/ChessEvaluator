package com.evanmccormick.chessevaluator.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    LaunchedEffect(key1 = true){
        viewModel.navigationEvent.collect { destination ->
            when (destination) {
                is DashboardViewModel.NavigationDestination.Play -> navController.navigate("play_screen")
                is DashboardViewModel.NavigationDestination.Review -> navController.navigate("review_screen")
                is DashboardViewModel.NavigationDestination.Profile -> navController.navigate("profile_screen")
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
            Button(
                onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Play) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Play",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer( modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Review) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Review",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Secondary smaller buttons (Profile, Leaderboard, Settings, Donate)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Profile) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "Profile")
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Leaderboard) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "Leaderboard")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Settings) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "Settings")
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(DashboardViewModel.NavigationDestination.Donate) },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Text(text = "Donate")
                    }
                }
            }
        }
    }
}