package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.evaluation.components.AnalysisBoard
import com.evanmccormick.chessevaluator.ui.evaluation.components.HeaderContent
import com.evanmccormick.chessevaluator.ui.evaluation.components.PostSubmitCard
import com.evanmccormick.chessevaluator.ui.evaluation.components.PreSubmitCard
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation

@Composable
fun EvaluationScreen(
    navController: NavController,
    viewModel: EvaluationViewModel
) {
    LaunchedEffect(key1 = true) {
        viewModel.loadUserEloFromApi()
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
    val sideToMove = remember { viewModel.getSideToMove(evaluationState.pos.fen) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (evaluationState.settings.darkMode) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary)
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
            HeaderContent(
                evaluationState,
                viewModel,
                sideToMove
            )

            // Chess board - always visible
            AnalysisBoard(fen = evaluationState.pos.fen)

            // Conditional rendering based on submission state
            if (!evaluationState.hasSubmitted) {
                // Pre-submission: Slider, Timer, Guess button
                PreSubmitCard(
                    evaluationState,
                    viewModel,
                    timerRemaining,
                    sideToMove,
                    onSliderChange = { viewModel.updateSliderPosition(it) },
                    onGuess = { viewModel.evaluatePosition() },
                )
            } else {
                // Post-submission: Evaluation graph, Tags, Continue button
                PostSubmitCard(
                    evaluationState,
                    viewModel,
                    sideToMove,
                    onContinue = { viewModel.resetForNewPosition() },
                )
            }
        }
    }
}