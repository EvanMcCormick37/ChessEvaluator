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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EvaluationScreen(
    viewModel: EvaluationViewModel = viewModel()
) {
    val evaluationState by viewModel.evaluationState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8E24AA)) // Purple background
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
                color = Color.White,
                fontSize = 24.sp
            )
            Text(
                text = "What do you think the evaluation is? (% chance for X to win).",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }

        // Chess Position Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .background(Color.LightGray)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chess Position Here",
                fontSize = 18.sp,
                color = Color.Black
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
                            .background(Color.Black)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f - evaluationState.sliderPosition)
                            .fillMaxHeight()
                            .background(Color.White)
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
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            // Percentage Text Field
            OutlinedTextField(
                value = evaluationState.evaluationText,
                onValueChange = { viewModel.updateEvaluationText(it) },
                modifier = Modifier
                    .width(100.dp)
                    .background(MaterialTheme.colorScheme.primary),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Left),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
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
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                Tag("Opening")
                Tag("Closed Position")
                Tag("Ray Lopez")
                Tag("Queenless Middlegame")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Navigation Bar Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF004D40)) // Dark teal color
        ) {
            // This is a placeholder for your navbar that will be implemented separately
            Divider(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(2.dp),
                color = Color(0xFF00BFA5) // Teal accent
            )
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val itemConstraints = constraints.copy(minWidth = 0)

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var itemsInCurrentRow = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(itemConstraints)

            if (currentRowWidth + placeable.width > constraints.maxWidth || itemsInCurrentRow >= maxItemsInEachRow) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
                itemsInCurrentRow = 0
            }

            currentRow.add(placeable)
            currentRowWidth += placeable.width
            itemsInCurrentRow++
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row -> row.maxOfOrNull { it.height } ?: 0 } + (rows.size - 1) * 8

        layout(constraints.maxWidth, height) {
            var y = 0

            rows.forEach { row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + 8
                }
                y += row.maxOfOrNull { it.height } ?: 0
                y += 8 // Add 8.dp spacing between rows
            }
        }
    }
}

@Composable
fun Tag(text: String) {
    Surface(
        modifier = Modifier.padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFCE93D8) // Light purple
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}