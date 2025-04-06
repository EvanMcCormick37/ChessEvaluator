package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.utils.db.DatabaseManager
import com.github.bhlangonijr.chesslib.Side
import com.google.firebase.auth.FirebaseAuth
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
import kotlinx.coroutines.tasks.await
import kotlin.math.abs
import kotlin.random.Random

data class EvaluationState(
    val evaluationText: String = "0.0",
    val evalExplanation: String = "The position is equal",
    val positionFen: String = "", // Will be used later when integrating with backend
    val tags: List<String> = listOf("Opening", "Closed Position", "Ray Lopez", "Queenless Middlegame"),
    val sideToMove: Side? = null,
    val evaluation: Float = 0f, // The actual evaluation from engine/database
    val sigmoidEvaluation: Float = 0.5f, // The evaluation after sigmoid
    val userEvaluation: Float = 0f, // The user's guess
    val userSigmoidEvaluation: Float = 0.5f, // The user's guess after sigmoid
    val hasSubmitted: Boolean = false,
    val isLoading: Boolean = true,
    val positionElo: Int = 1500,
    val positionId: String = "",
    val userElo: Int = 1500,
    val eloTransfer: Int = 0,
)

class EvaluationViewModel : ViewModel() {

    private val _evaluationState = MutableStateFlow(EvaluationState(
        positionFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
    ))
    val evaluationState: StateFlow<EvaluationState> = _evaluationState.asStateFlow()

    // Database Handling
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val dbManager = DatabaseManager(auth, db)

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
            val userEval = sigmoidToEval(clampedPosition, currentState.sideToMove!!)
            currentState.copy(
                evaluationText = String.format("%.2f", userEval),
                userEvaluation = userEval,
                userSigmoidEvaluation = clampedPosition //Sigmoid eval is the same as the slider position
            )
        }
    }

    fun sigmoidToEval(position: Float, sideToMove: Side, stretch: Float = 1f): Float {
        // Prevent division by zero or log of negative number
        val clampedPosition = position.coerceIn(0.001f, 0.999f)

        // Inverse sigmoid: -ln(1/y - 1). Stretch multiplies the Eval output relative to the Sigmoid input
        val eval = -stretch*Math.log((1f / clampedPosition - 1).toDouble()).toFloat()
        return if (sideToMove == Side.WHITE) eval else -eval
    }

    fun evalToSigmoid(value: Float, sideToMove: Side, squish: Float = 0.5f): Float {
        // Sigmoid function: 1 / (1 + e^(-x)). Squish shrinks the output range relative to the Eval input
        val result = (1f / (1f + Math.exp(-((if (sideToMove == Side.BLACK) -value else value)*squish).toDouble()))).toFloat()

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

                val pos = dbManager.getRandomPosition(timeControl!!.durationSeconds)
                val sideToMove = if (pos.fen.split(" ")[1] == "w") Side.WHITE else Side.BLACK

                _evaluationState.update { currentState ->
                    currentState.copy(
                        positionId = pos.id,
                        positionFen = pos.fen,
                        evaluation = pos.eval,
                        sideToMove = sideToMove,
                        sigmoidEvaluation = evalToSigmoid(pos.eval, sideToMove),
                        positionElo = pos.elo,
                        tags = pos.tags,
                        evalExplanation = getEvalExplanation(pos.eval),
                        isLoading = false
                    )
                }
            } catch(e: Exception) {
                _evaluationState.update { it.copy(isLoading = false) }
            } finally {
                resetTimer()
            }
        }
    }

    fun loadUserEloFromApi() {
        viewModelScope.launch {
            val elo = dbManager.getUserElo(timeControl!!.durationSeconds)
            _evaluationState.update { currentState ->
                currentState.copy(
                    userElo = elo
                )
            }
        }
    }

    // Calculate elo transfer based on user's guess, returns position's change in Elo
    fun calculateEloTransfer(userElo: Int, positionElo: Int, evaluationDifferenceSigmoid: Float): Int {
        fun multiplier(ratingA: Int, ratingB: Int): Double {
            return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0))
        }
        val error = evaluationDifferenceSigmoid - 0.175f
        val merit = 0.1
        val k = 100
        var multiplier = 0.0
        if (error > 0) {
            multiplier = multiplier(userElo, positionElo)
            return ((multiplier* (error+merit) * k).toInt())
        } else {
            multiplier = multiplier(positionElo, userElo)
            return ((multiplier * (error-merit) * k).toInt())
        }
    }

    fun evaluatePosition() {
        // Stop the timer when evaluation is submitted
        timerJob?.cancel()

        // Update the position's elo, and the user's elo
        val evalDiffSigmoid = abs(evaluationState.value.userSigmoidEvaluation - evaluationState.value.sigmoidEvaluation)
        val eloTransfer = calculateEloTransfer(evaluationState.value.userElo, evaluationState.value.positionElo, evalDiffSigmoid)
        val newUserElo = evaluationState.value.userElo - eloTransfer
        val newPositionElo = evaluationState.value.positionElo + eloTransfer
        val positionId = evaluationState.value.positionId
        viewModelScope.launch {
            dbManager.updatePositionElo(positionId, newPositionElo, timeControl!!.durationSeconds)
            dbManager.updateUserElo(newUserElo, timeControl!!.durationSeconds)
        }
        // Set hasSubmitted to true
        _evaluationState.update { currentState ->
            currentState.copy(
                eloTransfer = eloTransfer,
                positionElo = newPositionElo,
                userElo = newUserElo,
                hasSubmitted = true
            )
        }
    }

    // Reset the evaluation state for a new position
    fun resetForNewPosition() {
        loadPositionFromApi()
        loadUserEloFromApi()
        _evaluationState.update { currentState ->
            currentState.copy(
                evaluationText = "0.0",
                userEvaluation = 0f,
                userSigmoidEvaluation = 0.5f,
                hasSubmitted = false,
                eloTransfer = 0,
                )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}