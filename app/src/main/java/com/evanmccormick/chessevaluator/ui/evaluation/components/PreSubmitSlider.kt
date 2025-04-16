package com.evanmccormick.chessevaluator.ui.evaluation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationState
import com.evanmccormick.chessevaluator.ui.evaluation.EvaluationViewModel
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import com.github.bhlangonijr.chesslib.Side

@Composable
fun PreSubmitSlider(
    evaluationState: EvaluationState,
    viewModel: EvaluationViewModel,
    sideToMove: Side,
    onSliderChange: (Float) -> Unit
){
    val userSliderPosition = viewModel.evalToSigmoid(evaluationState.userEvaluation, viewModel.getSideToMove(evaluationState.pos.fen))
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
                    .weight(userSliderPosition)
                    .fillMaxHeight()
                    .background(if (sideToMove == Side.WHITE) ExtendedTheme.colors.evaluationWhite else ExtendedTheme.colors.evaluationBlack)
            )
            Box(
                modifier = Modifier
                    .weight(1f - userSliderPosition)
                    .fillMaxHeight()
                    .background(if (sideToMove == Side.WHITE) ExtendedTheme.colors.evaluationBlack else ExtendedTheme.colors.evaluationWhite)
            )
        }

        // Slider
        Slider(
            value = userSliderPosition,
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
}