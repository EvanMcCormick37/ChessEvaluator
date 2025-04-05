package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.theme.ThemeController
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation

@Composable
fun EvaluationScreen(
    navController: NavController,
    viewModel: EvaluationViewModel
) {
    LaunchedEffect(key1 = true) {
        viewModel.resetForNewPosition()
    }

    ScreenWithNavigation(
        navController,
        currentRoute = "eval_screen"
    ) {
        EvaluationContent(viewModel = viewModel)
    }
}

@Composable
fun EvaluationContent(
    viewModel: EvaluationViewModel
) {
    val evaluationState by viewModel.evaluationState.collectAsState()
    val timerRemaining by viewModel.timerRemaining.collectAsState()
    val scrollState = rememberScrollState()
    val isDarkTheme by ThemeController.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary)
            .verticalScroll(scrollState)
    ) {
        if(evaluationState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 200.dp)
            )
        } else {
            // Title and position explanation text
            HeaderContent(evaluationState)

            // Chess board - always visible
            AnalysisBoard(
                fen = evaluationState.positionFen
            )

            // Conditional rendering based on submission state
            if (!evaluationState.hasSubmitted) {
                // Pre-submission: Slider, Timer, Guess button
                PreSubmitCard(
                    evaluationState = evaluationState,
                    timerRemaining = timerRemaining,
                    onSliderChange = { viewModel.updateSliderPosition(it) },
                    onGuess = { viewModel.evaluatePosition() },
                    isDarkTheme = { isDarkTheme }
                )
            } else {
                // Post-submission: Evaluation graph, Tags, Continue button
                PostSubmitCard(
                    evaluationState = evaluationState,
                    isDarkTheme = { isDarkTheme },
                    onContinue = { viewModel.resetForNewPosition() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}