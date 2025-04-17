package com.evanmccormick.chessevaluator.ui.evaluation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationState
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationViewModel
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.abs

@Composable
fun HeaderContent(
    evaluationState: EvaluationState,
    viwModel: EvaluationViewModel,
    sideToMove: Side
) {
    val evalExplanation = remember { viwModel.getEvalExplanation(evaluationState.pos.eval) }
    val sideToMoveText = if (sideToMove == Side.WHITE) "White" else "Black"
    val evaluationDifference = abs(evaluationState.userEvaluation - evaluationState.pos.eval)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (evaluationState.hasSubmitted) {
            // Show evaluation result after submission
            Text(
                text = "${evalExplanation}, ${evaluationState.pos.eval}.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your evaluation: ${
                    String.format(
                        "%.2f",
                        evaluationState.userEvaluation
                    )
                }. You were off by ${
                    String.format(
                        "%.2f",
                        evaluationDifference
                    )
                }.",
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