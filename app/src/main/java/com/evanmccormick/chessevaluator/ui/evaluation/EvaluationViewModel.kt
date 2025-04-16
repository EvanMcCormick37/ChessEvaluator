package com.evanmccormick.chessevaluator.ui.evaluation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.settings.EvalType
import com.evanmccormick.chessevaluator.ui.theme.AppSettingsController
import com.evanmccormick.chessevaluator.ui.utils.db.DatabaseManager
import com.evanmccormick.chessevaluator.ui.utils.db.Position
import com.github.bhlangonijr.chesslib.Side
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

// Elo Calculation Constants
private const val ELO_ERROR_OFFSET = 0.18f
private const val ELO_MERIT_BONUS = 0.1f
private const val ELO_K_FACTOR = 100.0
private const val ELO_SCALE_FACTOR = 400.0

data class EvalSettings(
    val evalType: EvalType,
    val updateElo: Boolean,
    val darkMode: Boolean,
)

data class EvaluationState(
    val pos: Position,
    val userEvaluation: Float,
    val hasSubmitted: Boolean,
    val isLoading: Boolean,
    val userElo: Int,
    val eloTransfer: Int,
    val settings: EvalSettings,
)

class EvaluationViewModel : ViewModel() {

    private val _evaluationState = MutableStateFlow(EvaluationState(
        pos = Position(
            id = "",
            fen = "",
            eval = 0f,
            elo = 1500,
            tags = emptyList()
        ),
        userEvaluation = 0f, //Not necessary in state?
        hasSubmitted = false,
        isLoading = true,
        userElo = 1500,
        eloTransfer = 0,
        settings = EvalSettings(
            evalType = AppSettingsController.evalType.value,
            updateElo = AppSettingsController.updateElo.value,
            darkMode = AppSettingsController.isDarkTheme.value
        )
    ))
    val evaluationState: StateFlow<EvaluationState> = _evaluationState.asStateFlow()

    // Database Handling
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dbManager = DatabaseManager(auth, db)

    // Timer functionality
    private val _timerRemaining = MutableStateFlow(0)
    val timerRemaining: StateFlow<Int> = _timerRemaining.asStateFlow()

    private var timerJob: Job? = null
    private lateinit var timeControl: TimeControl

    // Set time control from navigation parameter
    fun setTimeControl(timeControl: TimeControl) {
        this.timeControl = timeControl
        resetTimer()
    }

    // Reset and start timer
    private fun resetTimer() {
        timerJob?.cancel()
        timeControl.let { tc ->
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
            val sideToMove = getSideToMove(currentState.pos.fen)
            val userEval = sigmoidToEval(clampedPosition, sideToMove)
            currentState.copy(
                userEvaluation = userEval,
            )
        }
    }

    fun sigmoidToEval(position: Float, sideToMove: Side, stretch: Float = 2f): Float {
        // Prevent division by zero or log of negative number
        val clampedPosition = position.coerceIn(0.001f, 0.999f)

        // Inverse sigmoid: -ln(1/y - 1). Stretch multiplies the Eval output relative to the Sigmoid input
        val result = -stretch* ln((1f / clampedPosition - 1).toDouble()).toFloat()
        return if (sideToMove == Side.WHITE) result else -result
    }

    fun evalToSigmoid(value: Float, sideToMove: Side, squish: Float = 0.5f): Float {
        // Sigmoid function: 1 / (1 + e^(-x)). Squish shrinks the output range relative to the Eval input
        val result = (1f / (1f + exp(-((if (sideToMove == Side.BLACK) -value else value) * squish).toDouble()))).toFloat()

        // Optional: ensure output is strictly in [0,1]
        return result.coerceIn(0.001f, 0.999f)
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

    // Loads the position info of a random position from the Firebase Firestore API
    fun loadPositionFromApi() {
        viewModelScope.launch {
            _evaluationState.update { currentState ->
                currentState.copy(isLoading = true)
            }
            try{

                val pos = dbManager.getRandomPosition(timeControl.durationSeconds)
                val sideToMove = if (pos.fen.split(" ")[1] == "w") Side.WHITE else Side.BLACK

                _evaluationState.update { currentState ->
                    currentState.copy(
                        pos = pos,
                        isLoading = false,
                        userEvaluation = 0f,
                        hasSubmitted = false,
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
            val elo = dbManager.getUserElo(timeControl.durationSeconds)
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
            return 1.0 / (1.0 + 10.0.pow((ratingB - ratingA) / ELO_SCALE_FACTOR))
        }
        val error = evaluationDifferenceSigmoid - ELO_ERROR_OFFSET
        val merit = ELO_MERIT_BONUS
        val k = ELO_K_FACTOR
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

        // Calculate the elo transfer based on the user's guess
        val sideToMove = getSideToMove(evaluationState.value.pos.fen)
        val sliderEval = evalToSigmoid(evaluationState.value.pos.eval, sideToMove)
        val userSliderEval = evalToSigmoid(evaluationState.value.userEvaluation, sideToMove)

        // Update the position's elo, and the user's elo
        val evalDiffSigmoid = abs(userSliderEval - sliderEval)
        val eloTransfer = calculateEloTransfer(evaluationState.value.userElo, evaluationState.value.pos.elo, evalDiffSigmoid)
        val newUserElo = evaluationState.value.userElo - eloTransfer
        val newPosElo = evaluationState.value.pos.elo + eloTransfer
        val posId = evaluationState.value.pos.id

        viewModelScope.launch {
            dbManager.updatePositionElo(posId, newPosElo, timeControl.durationSeconds)
            dbManager.updateUserElo(newUserElo, timeControl.durationSeconds)
        }

        // Set hasSubmitted to true
        _evaluationState.update { currentState ->
            currentState.copy(
                pos = currentState.pos.copy(
                    elo = newPosElo
                ),
                userElo = newUserElo,
                eloTransfer = eloTransfer,
                hasSubmitted = true
            )
        }
    }

    // Reset the evaluation state for a new position
    fun resetForNewPosition() {
        loadPositionFromApi()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun getSideToMove(fen: String): Side {
        if (fen.isBlank()) return Side.WHITE
        return if (fen.split(" ")[1] == "w") Side.WHITE else Side.BLACK
    }
}