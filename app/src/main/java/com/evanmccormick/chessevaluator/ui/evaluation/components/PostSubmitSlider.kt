package com.evanmccormick.chessevaluator.ui.evaluation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import androidx.compose.ui.graphics.Color
import com.github.bhlangonijr.chesslib.Side

@Composable
fun PostSubmitSlider(
    minEval: Float,
    evalDifference: Float,
    maxEval: Float,
    sideToMove: Side
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
                        Box(
                            modifier = Modifier
                                .weight(minEval)
                                .fillMaxHeight()
                                .background(if (sideToMove == Side.WHITE) ExtendedTheme.colors.evaluationWhite else ExtendedTheme.colors.evaluationBlack)
                        )
                    // Red box marking user error
                    if (evalDifference > 0) {
                        Box(
                            modifier = Modifier
                                .weight(maxEval-minEval)
                                .fillMaxHeight()
                                .background(when{
                                    evalDifference < 0.125f -> Color.Green
                                    evalDifference < 0.25f -> Color.Yellow
                                    else -> Color.Red
                                })
                        )
                    }
                        // Right side - Black's evaluation
                        Box(
                            modifier = Modifier
                                .weight(1 - maxEval)
                                .fillMaxHeight()
                                .background(if(sideToMove == Side.WHITE) ExtendedTheme.colors.evaluationBlack else ExtendedTheme.colors.evaluationWhite)
                        )
                }
            }
        }
    }
}