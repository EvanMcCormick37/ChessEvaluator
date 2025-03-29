package com.evanmccormick.chessevaluator.ui.dashboard

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.ui.evaluation.TimeControl

@Composable
fun EvalButton(
    text: String,
    onClick: (selectedTimeControl: TimeControl) -> Unit
) {
    var showTimeControls by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showTimeControls) {
            TimeControlSelector { selectedTimeControl ->
                // Handle time control selection
                showTimeControls = false
                onClick(selectedTimeControl)
            }
        } else {
            Button(
                onClick = { showTimeControls = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}