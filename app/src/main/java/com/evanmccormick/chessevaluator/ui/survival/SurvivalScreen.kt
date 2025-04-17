package com.evanmccormick.chessevaluator.ui.survival

import ResultsView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationContent
import com.evanmccormick.chessevaluator.ui.evaluation.components.AnalysisBoard
import com.evanmccormick.chessevaluator.ui.evaluation.components.PreSubmitCard
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.abs

const val MAX_HEALTH = 1000

@Composable
fun SurvivalScreen(
    navController: NavController,
    viewModel: SurvivalViewModel
) {
    LaunchedEffect(key1 = true) {
        viewModel.resetForNewPosition()
    }

    ScreenWithNavigation(
        navController,
        currentRoute = "eval_screen"
    ) {
        SurvivalContent(viewModel = viewModel)
    }
}

@Composable
fun SurvivalContent(
    viewModel: SurvivalViewModel
) {
    val survivalState by viewModel.survivalState.collectAsState()
    val timerRemaining by viewModel.timerRemaining.collectAsState()
    val scrollState = rememberScrollState()

    val sideToMove = viewModel.getSideToMove(survivalState.pos.fen)
    val evalExplanation = viewModel.getEvalExplanation(survivalState.pos.eval)
    val evaluationDifference = abs(survivalState.userEvaluation - survivalState.pos.eval)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 48.dp)
            .background(if (survivalState.settings.darkMode) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (survivalState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 200.dp)
            )
        } else {
            // Status Text
            if (!survivalState.hasSubmitted) {
                val sideToMoveText = if (sideToMove == Side.WHITE) "White" else "Black"
                Text(
                    text = "$sideToMoveText to move.",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "What do you think the evaluation is?",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

            } else {
                Text(
                    text = "$evalExplanation, ${survivalState.pos.eval}.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Chess board
            AnalysisBoard(fen = survivalState.pos.fen)

            if (!survivalState.hasSubmitted) {
                PreSubmitCard(
                    timerRemaining,
                    sideToMove,
                    userEvaluation = survivalState.userEvaluation,
                    darkMode = survivalState.settings.darkMode,
                    evalToSigmoid = { eval, side -> viewModel.evalToSigmoid(eval, side) },
                    onSliderChange = { viewModel.updateSliderPosition(it) },
                    onGuess = { viewModel.evaluatePosition() },
                )
            } else {
                // Results view
                ResultsView(
                    sideToMove,
                    userEvaluation = survivalState.userEvaluation,
                    trueEvaluation = survivalState.pos.eval,
                    healthLost = viewModel.getHealthLost(),
                    gameOver = survivalState.gameOver,
                    evalToSigmoid = { eval, side -> viewModel.evalToSigmoid(eval, side) },
                    onContinue = { viewModel.resetForNewPosition() }
                )
            }

            if (!survivalState.gameOver) {
                HealthBar(
                    currentHealth = survivalState.currentHealth,
                    maxHealth = MAX_HEALTH,
                )
            } else {
                GameOverView(
                    positionsEvaluated = survivalState.positionsEvaluated,
                    onRestart = { viewModel.restartGame() }
                )
            }
        }
    }
}

@Composable
fun HealthBar(
    currentHealth: Int,
    maxHealth: Int,
    modifier: Modifier = Modifier
) {
    val healthPercentage = currentHealth.toFloat() / maxHealth.toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(2.dp)
    ) {
        // Health remaining
        Box(
            modifier = Modifier
                .fillMaxWidth(healthPercentage)
                .fillMaxHeight()
                .background(
                    when {
                        healthPercentage > 0.6f -> Color.Green
                        healthPercentage > 0.3f -> Color.Yellow
                        else -> Color.Red
                    },
                    RoundedCornerShape(2.dp)
                )
        )
    }
    Text(
        text = "Health Remaining: $currentHealth",
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun GameOverView(
    positionsEvaluated: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Final Score: ${positionsEvaluated - 1}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Restart Button
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Play Again")
        }
    }
}

@Composable
fun HeaderContent(
    survivalState: SurvivalState,
    sideToMove: Side
) {

}