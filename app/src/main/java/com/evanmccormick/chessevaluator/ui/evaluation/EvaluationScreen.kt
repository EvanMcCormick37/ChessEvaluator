package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation

@Composable
fun EvaluationScreen(
    navController: NavController,
    viewModel: EvaluationViewModel
) {
    ScreenWithNavigation(
        navController,
        currentRoute = "play_screen"
    ) {
        EvaluationContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvaluationContent(
    viewModel: EvaluationViewModel
) {
    val evaluationState by viewModel.evaluationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Section - Title and instructions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "X to move.",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "What do you think the evaluation is? (% chance for X to win).",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Chess Position Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chess Position Here",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
                            .weight(evaluationState.sliderPosition)
                            .fillMaxHeight()
                            .background(ExtendedTheme.colors.evaluationBlack)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f - evaluationState.sliderPosition)
                            .fillMaxHeight()
                            .background(ExtendedTheme.colors.evaluationWhite)
                    )
                }

                // Slider
                Slider(
                    value = evaluationState.sliderPosition,
                    onValueChange = { viewModel.updateSliderPosition(it) },
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

            // Percentage Text Field
            OutlinedTextField(
                value = evaluationState.evaluationText,
                onValueChange = { viewModel.updateEvaluationText(it) },
                modifier = Modifier
                    .width(100.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Left),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                trailingIcon = {
                    Text(text = "%")
                }
            )
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
                modifier = Modifier.padding(bottom = 8.dp)
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

        Spacer(modifier = Modifier.weight(1f))

        //Button to submit evaluation:
        Button(
            onClick = { viewModel.evaluatePosition() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Guess"
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun Tag(text: String) {
    Surface(
        modifier = Modifier.padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = ExtendedTheme.colors.tagBackground
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onTertiary,
            fontSize = 14.sp
        )
    }
}