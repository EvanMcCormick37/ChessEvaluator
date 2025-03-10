package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class EvaluationState(
    val sliderPosition: Float = 0.5f,
    val evaluationText: String = "50.0",
    val positionFen: String = "", // Will be used later when integrating with backend
    val nextMove: String = "", // Will be used later
    val tags: List<String> = listOf("Opening", "Closed Position", "Ray Lopez", "Queenless Middlegame")
)

class EvaluationViewModel : ViewModel() {

    private val _evaluationState = MutableStateFlow(EvaluationState())
    val evaluationState: StateFlow<EvaluationState> = _evaluationState.asStateFlow()

    // Update the slider position and sync with text field
    fun updateSliderPosition(position: Float) {
        _evaluationState.update { currentState ->
            val clampedPosition = position.coerceIn(0f, 1f)
            val percentageValue = (clampedPosition * 1000).toInt().toFloat()/10
            currentState.copy(
                sliderPosition = clampedPosition,
                evaluationText = percentageValue.toString()
            )
        }
    }

    // Update text field and sync with slider
    fun updateEvaluationText(text: String) {
        try {
            // Only process if text is valid or empty
            if (text.isEmpty() || text.matches(Regex("^\\d+(\\.\\d*)?$"))) {
                val numericValue = text.toFloatOrNull() ?: 0f

                // Ensure the value is within the acceptable range
                val clampedValue = numericValue.coerceIn(0f, 100f)
                val normalizedPosition = clampedValue / 100f

                _evaluationState.update { currentState ->
                    currentState.copy(
                        evaluationText = text,
                        sliderPosition = normalizedPosition
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore invalid inputs
        }
    }

    // This will be called later when integrating with your backend
    fun loadPositionFromApi(fen: String, nextMove: String, evaluation: Float) {
        _evaluationState.update { currentState ->
            currentState.copy(
                positionFen = fen,
                nextMove = nextMove,
                sliderPosition = evaluation / 100f,
                evaluationText = evaluation.toString()
            )
        }
    }

    // Add or remove tags
    fun addTag(tag: String) {
        _evaluationState.update { currentState ->
            val updatedTags = currentState.tags.toMutableList()
            if (!updatedTags.contains(tag)) {
                updatedTags.add(tag)
            }
            currentState.copy(tags = updatedTags)
        }
    }

    fun removeTag(tag: String) {
        _evaluationState.update { currentState ->
            val updatedTags = currentState.tags.toMutableList()
            updatedTags.remove(tag)
            currentState.copy(tags = updatedTags)
        }
    }
}