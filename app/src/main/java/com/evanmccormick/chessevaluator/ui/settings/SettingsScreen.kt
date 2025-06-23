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
    val settings by viewModel.settings.collectAsState()

    // Handle account deletion navigation
    LaunchedEffect(settings.accountDeleted) {
        if (settings.accountDeleted) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true } // Clear entire navigation stack
            }
        }
    }

    // Show delete confirmation dialog
    if (settings.showDeleteConfirmation) {
        DeleteAccountConfirmationDialog(
            isLoading = settings.isDeleteLoading,
            errorMessage = settings.deleteError,
            onConfirm = { viewModel.deleteAccount() },
            onDismiss = { viewModel.hideDeleteConfirmation() },
            onClearError = { viewModel.clearDeleteError() }
        )
    }
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
                // Username Setting with Update Button
                SettingRow(
                    label = "Username",
                    textColor = textColor,
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = settings.username,
                                onValueChange = { viewModel.updateUsername(it) },
                                modifier = Modifier
                                    .height(56.dp)
                                    .weight(1f), // Use weight instead of fixed width
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

                            Spacer(modifier = Modifier.width(16.dp)) // Increased spacing

                            Button(
                                onClick = { viewModel.updateUsernameInDatabase(settings.username) },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Update")
                            }
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

            SettingsSection(title = "Evaluation Settings", textColor = textColor) {
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

            // Account Management Section
            SettingsSection(title = "Account Management", textColor = textColor) {
                // Delete Account Button
                Button(
                    onClick = { viewModel.showDeleteConfirmation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Delete Account",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DeleteAccountConfirmationDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onClearError: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Delete Account",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone and will permanently remove:",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• All your game statistics and Elo ratings\n• Your leaderboard standings\n• Your user profile and data",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                TextButton(
                    onClick = {
                        if (errorMessage != null) {
                            onClearError()
                        } else {
                            onConfirm()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (errorMessage != null) "Retry" else "Delete Account")
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    )
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