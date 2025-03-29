package com.evanmccormick.chessevaluator.ui.evaluation

import android.app.Notification.Extender
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // Evaluation graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp)
            ) {
                // Show the evaluation curve
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Simple visualization showing user guess vs actual
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Left side - White's evaluation
                            Box(
                                modifier = Modifier
                                    .weight(min(evaluationState.sigmoidEvaluation, evaluationState.userSigmoidEvaluation))
                                    .fillMaxHeight()
                                    .background(ExtendedTheme.colors.evaluationWhite)
                            )
                            // Red box marking user error
                            if (abs(evaluationState.sigmoidEvaluation - evaluationState.userSigmoidEvaluation) > 0) {
                                Box(
                                modifier = Modifier
                                    .weight(abs(evaluationState.sigmoidEvaluation - evaluationState.userSigmoidEvaluation))
                                    .fillMaxHeight()
                                    .background(Color.Red)
                            )
                            }
                            // Right side - Black's evaluation
                            Box(
                                modifier = Modifier
                                    .weight(1 - max(evaluationState.sigmoidEvaluation, evaluationState.userSigmoidEvaluation))
                                    .fillMaxHeight()
                                    .background(ExtendedTheme.colors.evaluationBlack)
                            )
                        }
                    }
                }
            }

            // Position difficulty and accuracy indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Tag(
                    text = "Position Elo: ${evaluationState.positionElo}",
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                Tag(
                    text = "User Elo: ${evaluationState.userElo}",
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }

        // Tags Section
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

        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
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