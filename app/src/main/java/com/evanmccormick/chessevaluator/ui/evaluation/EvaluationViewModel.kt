package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class EvaluationState(
    val evaluationText: String = "0.0",
    val evalExplanation: String = "The position is equal",
    val positionFen: String = "", // Will be used later when integrating with backend
    val tags: List<String> = listOf("Opening", "Closed Position", "Ray Lopez", "Queenless Middlegame"),
    val evaluation: Float = 0f, // The actual evaluation from engine/database
    val sigmoidEvaluation: Float = 0.5f, // The evaluation after sigmoid
    val userEvaluation: Float = 0f, // The user's guess
    val userSigmoidEvaluation: Float = 0.5f, // The user's guess after sigmoid
    val hasSubmitted: Boolean = false,
    val isLoading: Boolean = true,
    val positionElo: Int = 1500,
    val userElo: Int = 1500,
)

class EvaluationViewModel : ViewModel() {

    private val _evaluationState = MutableStateFlow(EvaluationState(
        positionFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
    ))
    val evaluationState: StateFlow<EvaluationState> = _evaluationState.asStateFlow()

    // Timer functionality
    private val _timerRemaining = MutableStateFlow(0)
    val timerRemaining: StateFlow<Int> = _timerRemaining.asStateFlow()

    private var timerJob: Job? = null
    private var timeControl: TimeControl? = null

    // Set time control from navigation parameter
    fun setTimeControl(timeControl: TimeControl?) {
        timeControl?.let {
            this.timeControl = it
            resetTimer()
        }
    }

    // Reset and start timer
    private fun resetTimer() {
        timerJob?.cancel()
        timeControl?.let { tc ->
            _timerRemaining.value = tc.durationSeconds
            startTimer()
        }
    }

    // Start countdown timer
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerRemaining.value > 0) {
                delay(1000)
                _timerRemaining.value = _timerRemaining.value - 1

                // When timer reaches zero, auto-submit if not already submitted
                if (_timerRemaining.value == 0 && !_evaluationState.value.hasSubmitted) {
                    evaluatePosition()
                }
            }
        }
    }

    // Update the slider position and sync with text field
    fun updateSliderPosition(position: Float) {
        _evaluationState.update { currentState ->
            val clampedPosition = position.coerceIn(0.001f, 0.999f)
            val userEval = sigmoidToEval(clampedPosition)
            currentState.copy(
                evaluationText = String.format("%.2f",userEval),
                userEvaluation = userEval,
                userSigmoidEvaluation = clampedPosition //Sigmoid eval is the same as the slider position
            )
        }
    }

    fun sigmoidToEval(position: Float): Float {
        // Prevent division by zero or log of negative number
        val clampedPosition = position.coerceIn(0.001f, 0.999f)

        // Inverse sigmoid: -ln(1/y - 1)
        return -2*Math.log((1f / clampedPosition - 1).toDouble()).toFloat()
    }

    fun evalToSigmoid(value: Float): Float {
        // Sigmoid function: 1 / (1 + e^(-x))
        val result = (1f / (1f + Math.exp(-value.toDouble()))).toFloat()

        // Optional: ensure output is strictly in [0,1]
        return result.coerceIn(0.001f, 0999f)
    }

    fun getEvalExplanation(evaluation: Float): String {
        return when {
            evaluation <= -2.5f -> "Black is winning"
            evaluation <= -1.25f -> "Black has a significant advantage"
            evaluation <= -0.55f -> "Black is better"
            evaluation <= -0.25f -> "Black is slightly better"
            evaluation < 0.25f -> "The position is equal"
            evaluation < 0.55f -> "White is slightly better"
            evaluation < 1.25f -> "White is better"
            evaluation < 2.5f -> "White has a significant advantage"
            else -> "White is winning"
        }
    }



    // Loads the position info of a random position from the FirebaseFirestore API
    fun loadPositionFromApi() {
        viewModelScope.launch {
            _evaluationState.update { currentState ->
                currentState.copy(isLoading = true)
            }
            try{
                val db = FirebaseFirestore.getInstance()
                val positionsRef = db.collection("positions")

                positionsRef.limit(1).get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val fen = document.getString("fen")!!
                        val eval = document.getDouble("eval")!!.toFloat()
                        val elo = document.getLong("elo")!!.toInt()
                        val tags = document.get("tags") as List<String>
                        val evalExplanation = getEvalExplanation(eval)
                        val sigmoidEval = evalToSigmoid(eval)

                        _evaluationState.update { currentState ->
                            currentState.copy(
                                positionFen = fen,
                                evaluation = eval,
                                sigmoidEvaluation = sigmoidEval,
                                positionElo = elo,
                                tags = tags,
                                evalExplanation = evalExplanation,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch(e: Exception) {
                _evaluationState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun evaluatePosition() {
        // Stop the timer when evaluation is submitted
        timerJob?.cancel()

        // Set hasSubmitted to true
        _evaluationState.update { currentState ->
            currentState.copy(
                hasSubmitted = true
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

    // Reset the evaluation state for a new position
    fun resetForNewPosition() {
        loadPositionFromApi()
        _evaluationState.update { currentState ->
            currentState.copy(
                evaluationText = "0.0",
                userEvaluation = 0f,
                userSigmoidEvaluation = 0.5f,
                hasSubmitted = false,
                )
        }
        resetTimer()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}