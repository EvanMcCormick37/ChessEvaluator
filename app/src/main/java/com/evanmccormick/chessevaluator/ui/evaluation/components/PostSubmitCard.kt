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
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import com.evanmccormick.chessevaluator.ui.utils.db.Position
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostSubmitCard(
    pos: Position,
    sideToMove: Side,
    userEvaluation: Float,
    trueSliderEvaluation: Float,
    eloTransfer: Int,
    userElo: Int,
    darkMode: Boolean,
    updateElo: Boolean,
    evalToSigmoid: (Float, Side) -> Float,
    onContinue: () -> Unit
) {

    val userSliderPosition = evalToSigmoid(userEvaluation, sideToMove)

    val minEval = min(trueSliderEvaluation, userSliderPosition)
    val maxEval = max(trueSliderEvaluation, userSliderPosition)
    val eloTransferGood =
        eloTransfer < 0 //Elo transfer is categorized in terms of position elo gain/loss. This tracks user gain/loss
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
                minEval,
                maxEval,
                sideToMove
            )

            if (updateElo) {
                // Elo Updates
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Tag(
                        text = "User Elo ${if (eloTransferGood) "+" else ""}${-eloTransfer}: ${userElo}",
                        color = if (eloTransferGood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                    Tag(
                        text = "Position Elo ${if (eloTransferGood) "" else "+"}${eloTransfer}: ${pos.elo}",
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
        }

        // Tags Section
        if (!pos.tags.isEmpty()) {
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
                    for (tag in pos.tags) {
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
                containerColor = if (darkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
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