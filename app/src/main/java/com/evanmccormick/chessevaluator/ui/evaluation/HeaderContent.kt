package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.abs

@Composable
fun HeaderContent(
    evaluationState: EvaluationState,
){
    val board = remember(evaluationState.positionFen) {
        try {
            Board().apply {
                loadFromFen(evaluationState.positionFen.ifEmpty {
                    "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
                })
            }
        } catch (e: Exception) {
            Board()
        }
    }
    val sideToMove = board.sideToMove
    val sideToMoveText = if (sideToMove == Side.WHITE) "White" else "Black"
    val evaluationDifference = abs(evaluationState.userEvaluation - evaluationState.evaluation)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            if (evaluationState.hasSubmitted) {
                // Show evaluation result after submission
                Text(
                    text = "${evaluationState.evalExplanation}, ${evaluationState.evaluation}.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Your evaluation: ${String.format("%.2f",evaluationState.userEvaluation)}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (evaluationDifference > 0) "You were off by ${String.format("%.2f", evaluationDifference)}." else "You are correct!",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Show instructions before submission
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
        }
}