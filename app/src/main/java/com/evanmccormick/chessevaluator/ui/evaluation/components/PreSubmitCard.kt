package com.evanmccormick.chessevaluator.ui.evaluation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import com.github.bhlangonijr.chesslib.Side

@Composable
fun PreSubmitCard(
    timerRemaining: Int,
    sideToMove: Side,
    userEvaluation: Float,
    darkMode: Boolean,
    evalToSigmoid: (Float, Side) -> Float,
    onSliderChange: (Float) -> Unit,
    onGuess: () -> Unit,
) {
    val evaluationText = String.format("%.2f", userEvaluation)
    val userSliderPosition = evalToSigmoid(userEvaluation, sideToMove)
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Evaluation Slider and Input Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .width(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    // Format time as MM:SS
                    val minutes = timerRemaining / 60
                    val seconds = timerRemaining % 60
                    val timeString = String.format("%d:%02d", minutes, seconds)

                    Text(
                        text = timeString,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Guess Button
            Button(
                onClick = onGuess,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userEvaluation.toFloat() > 0) ExtendedTheme.colors.evaluationWhite else ExtendedTheme.colors.evaluationBlack
                )
            ) {
                Text(
                    text = evaluationText,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (evaluationText.toFloat() > 0) ExtendedTheme.colors.evaluationBlack else ExtendedTheme.colors.evaluationWhite,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            PreSubmitSlider(
                sideToMove,
                userSliderPosition,
                onSliderChange
            )
        }
    }
}