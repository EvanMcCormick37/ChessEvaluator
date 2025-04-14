package com.evanmccormick.chessevaluator.ui.evaluation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationState
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostSubmitCard(
    evaluationState: EvaluationState,
    onContinue: () -> Unit
) {

    val minEval = min(evaluationState.sigmoidEvaluation, evaluationState.userSigmoidEvaluation)
    val maxEval = max(evaluationState.sigmoidEvaluation, evaluationState.userSigmoidEvaluation)
    val evalDifference = abs(maxEval - minEval)
    val eloTransferGood = evaluationState.eloTransfer < 0 //Elo transfer is categorized in terms of position elo gain/loss. This tracks user gain/loss
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Evaluation graph with the correct value
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PostSubmitSlider(
                evaluationState,
                minEval,
                evalDifference,
                maxEval,
                evaluationState.sideToMove!!
            )

            // Position difficulty and accuracy indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Tag(
                    text = "User Elo ${if (eloTransferGood) "+" else ""}${-evaluationState.eloTransfer}: ${evaluationState.userElo}",
                    color = if (eloTransferGood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                )
                Tag(
                    text = "Position Elo ${if (eloTransferGood) "" else "+"}${evaluationState.eloTransfer}: ${evaluationState.positionElo}",
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }

        // Tags Section
        if (!evaluationState.tags.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Tags",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    for (tag in evaluationState.tags) {
                        Tag(tag)
                    }
                }
            }
        }


        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (evaluationState.darkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(text = "Continue")
        }
    }
}

@Composable
fun Tag(text: String, color: Color = ExtendedTheme.colors.tagBackground) {
    Surface(
        modifier = Modifier.padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onTertiary,
            fontSize = 14.sp
        )
    }
}