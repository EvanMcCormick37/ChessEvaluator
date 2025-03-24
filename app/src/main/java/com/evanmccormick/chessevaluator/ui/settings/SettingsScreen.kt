package com.evanmccormick.chessevaluator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    ScreenWithNavigation(
        navController,
        currentRoute = "settings_screen"
    ) {
        SettingsContent(viewModel)
    }
}

@Composable
fun SettingsContent(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val scrollState = rememberScrollState()

    // Get current color scheme based on settings
    val backgroundColor = MaterialTheme.colorScheme.background
    val containerColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onSurface
    val textFieldBackgroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(14.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Settings Section
            SettingsSection(title = "Profile Settings", textColor = textColor) {
                // Username Setting
                SettingRow(
                    label = "Username",
                    textColor = textColor,
                    content = {
                        OutlinedTextField(
                            value = settings.username,
                            onValueChange = { viewModel.updateUsername(it) },
                            modifier = Modifier
                                .height(56.dp) // Increased height
                                .width(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = textFieldBackgroundColor,
                                unfocusedContainerColor = textFieldBackgroundColor,
                                focusedBorderColor = textColor,
                                unfocusedBorderColor = textColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            singleLine = true
                        )
                    }
                )

                // Leaderboard Visibility Setting
                SettingRow(
                    label = "Leaderboard Visibility",
                    textColor = textColor,
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ToggleSegmentedButton(
                                options = listOf("On", "Off"),
                                selectedOption = if (settings.leaderboardVisible) "On" else "Off",
                                onOptionSelected = { viewModel.updateLeaderboardVisibility(it == "On") },
                                textColor = textColor,
                                backgroundColor = textFieldBackgroundColor
                            )
                        }
                    }
                )
            }

            // Display Settings Section
            SettingsSection(title = "Display Settings", textColor = textColor) {
                // Dark Mode Setting
                SettingRow(
                    label = "Dark Mode",
                    textColor = textColor,
                    content = {
                        ToggleSegmentedButton(
                            options = listOf("On", "Off"),
                            selectedOption = if (settings.darkMode) "On" else "Off",
                            onOptionSelected = { viewModel.updateDarkMode(it == "On") },
                            textColor = textColor,
                            backgroundColor = textFieldBackgroundColor
                        )
                    }
                )
            }

            // Evaluation Settings Section
            SettingsSection(title = "Evaluation Settings", textColor = textColor) {
                // Eval Type Setting
                SettingRow(
                    label = "Eval",
                    textColor = textColor,
                    content = {
                        ToggleSegmentedButton(
                            options = listOf("Centipawn", "Sigmoid"),
                            selectedOption = settings.evalType.name,
                            onOptionSelected = {
                                viewModel.updateEvalType(
                                    if (it == "Centipawn") EvalType.Centipawn else EvalType.Sigmoid
                                )
                            },
                            textColor = textColor,
                            backgroundColor = textFieldBackgroundColor
                        )
                    }
                )

                // Filter Tags Setting
                SettingRow(
                    label = "Filter Tags",
                    textColor = textColor,
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tag TextField
                            OutlinedTextField(
                                value = settings.currentTag,
                                onValueChange = { viewModel.updateCurrentTag(it) },
                                modifier = Modifier
                                    .height(56.dp) // Increased height
                                    .width(140.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = textFieldBackgroundColor,
                                    unfocusedContainerColor = textFieldBackgroundColor,
                                    focusedBorderColor = textColor,
                                    unfocusedBorderColor = textColor,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                singleLine = true
                            )

                            // Add Tag Button
                            IconButton(
                                onClick = { viewModel.addTag() },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(24.dp)
                            ) {
                                Text(
                                    text = "+",
                                    color = textColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )

                // Update Elo Setting
                SettingRow(
                    label = "Update Elo (Ranked Mode)",
                    textColor = textColor,
                    content = {
                        ToggleSegmentedButton(
                            options = listOf("On", "Off"),
                            selectedOption = if (settings.updateElo) "On" else "Off",
                            onOptionSelected = { viewModel.updateEloMode(it == "On") },
                            textColor = textColor,
                            backgroundColor = textFieldBackgroundColor
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section title
        Text(
            text = title,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Content
        content()

        // Divider
        HorizontalDivider(
            color = textColor.copy(alpha = 0.6f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Composable
fun SettingRow(
    label: String,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier.width(200.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

@Composable
fun ToggleSegmentedButton(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    textColor: Color,
    backgroundColor: Color,
    minWidth: Dp = 40.dp
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .background(Color.Transparent)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(min = minWidth)
                    .background(
                        color = if (isSelected) backgroundColor else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp)
                    .clickable { onOptionSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}