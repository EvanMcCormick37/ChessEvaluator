package com.evanmccormick.chessevaluator.ui.survival

import ResultsView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.evanmccormick.chessevaluator.ui.evaluation.components.AnalysisBoard
import com.evanmccormick.chessevaluator.ui.evaluation.components.PreSubmitCard
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation
import com.github.bhlangonijr.chesslib.Side

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
        SurvivalContent(
            viewModel = viewModel,
            navController = navController,
            )
    }
}

@Composable
fun SurvivalContent(
    viewModel: SurvivalViewModel,
    navController: NavController,
) {
    val survivalState by viewModel.survivalState.collectAsState()
    val timerRemaining by viewModel.timerRemaining.collectAsState()
    val scrollState = rememberScrollState()

    val sideToMove = viewModel.getSideToMove(survivalState.pos.fen)
    val evalExplanation = viewModel.getEvalExplanation(survivalState.pos.eval)
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
                Text(
                    text = "$evalExplanation, ${survivalState.pos.eval}.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    onRestart = { viewModel.restartGame() },
                    navController = navController,
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
            .padding(10.dp)
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
                    RoundedCornerShape(4.dp)
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
    onRestart: () -> Unit,
    navController: NavController,
) {
    // Calculate score using the same formula as in the ViewModel
    val score = positionsEvaluated - 1

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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Final Score: ${score}",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Restart Button
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Play Again")
        }

        // View Leaderboard Button
        Button(
            onClick = {
                navController.navigate("survival_leaderboard_screen"){
                    launchSingleTop = true
                    restoreState = true
                } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text(text = "View Leaderboard")
        }
    }
}