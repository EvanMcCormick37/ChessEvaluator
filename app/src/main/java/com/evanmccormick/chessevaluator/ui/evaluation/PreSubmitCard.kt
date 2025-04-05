package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme

@Composable
fun PreSubmitCard(
    evaluationState: EvaluationState,
    timerRemaining: Int,
    onSliderChange: (Float) -> Unit,
    onGuess: () -> Unit,
    isDarkTheme: () -> Boolean
) {
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
            // Custom Slider with black and white sides
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(evaluationState.userSigmoidEvaluation)
                            .fillMaxHeight()
                            .background(ExtendedTheme.colors.evaluationWhite)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f - evaluationState.userSigmoidEvaluation)
                            .fillMaxHeight()
                            .background(ExtendedTheme.colors.evaluationBlack)
                    )
                }

                // Slider
                Slider(
                    value = evaluationState.userSigmoidEvaluation,
                    onValueChange = onSliderChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0f), // Make slider invisible but functional
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                    )
                )
            }

            Surface(
                modifier = Modifier
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 1.dp
            ) {
                Text(
                    text = evaluationState.evaluationText,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Timer Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Time Remaining",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                containerColor = if (isDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(text = "Guess")
        }
    }
}