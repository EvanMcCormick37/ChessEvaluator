package com.evanmccormick.chessevaluator.ui.survival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evanmccormick.chessevaluator.ui.evaluation.TimeControl
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
import kotlin.math.max

data class SurvivalSettings(
    val evalType: EvalType,
    val darkMode: Boolean,
)

data class SurvivalState(
    val pos: Position,
    val userEvaluation: Float,
    val hasSubmitted: Boolean,
    val isLoading: Boolean,
    val currentHealth: Int,
    val positionsEvaluated: Int,
    val gameOver: Boolean = false,
    val settings: SurvivalSettings,
)

class SurvivalViewModel : ViewModel() {
    private val _survivalState = MutableStateFlow(
        SurvivalState(
            pos = Position(
                id = "",
                fen = "",
                eval = 0f,
                elo = 1500,
                tags = emptyList()
            ),
            userEvaluation = 0f,
            hasSubmitted = false,
            isLoading = true,
            currentHealth = 1000,
            positionsEvaluated = 0,
            gameOver = false,
            settings = SurvivalSettings(
                evalType = AppSettingsController.evalType.value,
                darkMode = AppSettingsController.isDarkTheme.value,
            )
        )
    )
    val survivalState: StateFlow<SurvivalState> = _survivalState.asStateFlow()

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
                if (_timerRemaining.value == 0 && !survivalState.value.hasSubmitted) {
                    evaluatePosition()
                }
            }
        }
    }

    // Update the slider position and sync with text field
    fun updateSliderPosition(position: Float) {
        _survivalState.update { currentState ->
            val clampedPosition = position.coerceIn(0.001f, 0.999f)
            val sideToMove = getSideToMove(currentState.pos.fen)
            val userEval = sigmoidToEval(clampedPosition, sideToMove)
            currentState.copy(
                userEvaluation = userEval
            )
        }
    }

    fun sigmoidToEval(position: Float, sideToMove: Side, stretch: Float = 2f): Float {
        // Prevent division by zero or log of negative number
        val clampedPosition = position.coerceIn(0.001f, 0.999f)

        // Inverse sigmoid: -ln(1/y - 1). Stretch multiplies the Eval output relative to the Sigmoid input
        val result = -stretch * ln((1f / clampedPosition - 1).toDouble()).toFloat()
        return if (sideToMove == Side.WHITE) result else -result
    }

    fun evalToSigmoid(value: Float, sideToMove: Side, squish: Float = 0.5f): Float {
        // Sigmoid function: 1 / (1 + e^(-x)). Squish shrinks the output range relative to the Eval input
        val result =
            (1f / (1f + exp(-((if (sideToMove == Side.BLACK) -value else value) * squish).toDouble()))).toFloat()

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
            _survivalState.update { currentState ->
                currentState.copy(isLoading = true)
            }
            try {
                val centerpoint = 1250 + (20 * survivalState.value.positionsEvaluated)
                val minElo = centerpoint - 100
                val maxElo = centerpoint + 100
                val pos = dbManager.getPositionInEloRange(minElo, maxElo, timeControl.durationSeconds)

                _survivalState.update { currentState ->
                    currentState.copy(
                        pos = pos,
                        isLoading = false,
                        userEvaluation = 0f,
                        hasSubmitted = false
                    )
                }
            } catch (e: Exception) {
                _survivalState.update { it.copy(isLoading = false) }
            } finally {
                resetTimer()
            }
        }
    }

    fun getHealthLost() : Int {
        // Calculate health loss based on evaluation difference
        val sideToMove = getSideToMove(survivalState.value.pos.fen)
        val sliderEval = evalToSigmoid(survivalState.value.pos.eval, sideToMove)
        val userSliderEval = evalToSigmoid(survivalState.value.userEvaluation, sideToMove)

        val evalDiffSigmoid = abs(userSliderEval - sliderEval)

        val error = max(evalDiffSigmoid - 0.125f, 0f)

        // Health loss formula: Scale based on difference
        return (error * 1000).toInt()
    }

    fun evaluatePosition() {
        // Stop the timer when evaluation is submitted
        timerJob?.cancel()

        val newHealth = (survivalState.value.currentHealth - getHealthLost()).coerceAtLeast(0)
        val gameOver = newHealth <= 0

        // Update state
        _survivalState.update { currentState ->
            currentState.copy(
                hasSubmitted = true,
                currentHealth = newHealth,
                positionsEvaluated = currentState.positionsEvaluated + 1,
                gameOver = gameOver
            )
        }
    }

    // Reset the evaluation state for a new position
    fun resetForNewPosition() {
        loadPositionFromApi()
    }

    fun restartGame() {
        _survivalState.update { currentState ->
            currentState.copy(
                userEvaluation = 0f,
                hasSubmitted = false,
                currentHealth = 1000,
                positionsEvaluated = 0,
                gameOver = false
            )
        }
        loadPositionFromApi()
    }

    fun getSideToMove(fen: String): Side {
        if (fen.isBlank()) return Side.WHITE
        return if (fen.split(" ")[1] == "w") Side.WHITE else Side.BLACK
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}