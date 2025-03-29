package com.evanmccormick.chessevaluator.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.ui.evaluation.TimeControl

@Composable
fun TimeControlSelector(
    onTimeSelected: (TimeControl) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Select a Time Control",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Grid(
                columns = 3,
                items = TimeControl.allOptions
            ) { timeControl ->
                TimeControlButton(
                    text = timeControl.displayText,
                    onClick = { onTimeSelected(timeControl) }
                )
            }
        }
    }
}

@Composable
fun TimeControlButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(64.dp)
            .width(90.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun <T> Grid(
    columns: Int,
    items: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var itemsInRow = 0
        var rowItems = mutableListOf<T>()

        for (item in items) {
            rowItems.add(item)
            itemsInRow++

            if (itemsInRow == columns) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (rowItem in rowItems) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            content(rowItem)
                        }
                    }
                }
                itemsInRow = 0
                rowItems = mutableListOf()
            }
        }

        if (itemsInRow > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (rowItem in rowItems) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        content(rowItem)
                    }
                }
                repeat(columns - itemsInRow) {
                    Box(modifier = Modifier.weight(1f)) { }
                }
            }
        }
    }
}