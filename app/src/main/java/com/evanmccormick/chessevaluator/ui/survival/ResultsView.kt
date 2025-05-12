import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evanmccormick.chessevaluator.ui.evaluation.components.PostSubmitSlider
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun ResultsView(
    sideToMove: Side,
    userEvaluation: Float,
    trueEvaluation: Float,
    healthLost: Int,
    gameOver: Boolean,
    evalToSigmoid: (Float, Side) -> Float,
    onContinue: () -> Unit
) {
    val evaluationDifference = abs(userEvaluation - trueEvaluation)
    val userSliderPosition = evalToSigmoid(userEvaluation, sideToMove)
    val trueSliderEvaluation = evalToSigmoid(trueEvaluation, sideToMove)
    val minEval = min(trueSliderEvaluation, userSliderPosition)
    val maxEval = max(trueSliderEvaluation, userSliderPosition)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your evaluation: ${
                String.format(
                    "%.2f",
                    userEvaluation
                )
            }, You were off by ${String.format("%.2f", evaluationDifference)}.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PostSubmitSlider(
            minEval,
            maxEval,
            sideToMove
        )

        Text(
            text = "You lost $healthLost hp.",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!gameOver) {
            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(text = "Continue")
            }
        }
    }
}
