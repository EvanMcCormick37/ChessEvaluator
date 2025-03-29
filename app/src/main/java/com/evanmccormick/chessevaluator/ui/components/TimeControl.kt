package com.evanmccormick.chessevaluator.ui.evaluation

import java.time.Duration

sealed class TimeControl(val durationSeconds: Int, val displayText: String, val titleText: String) {
    object FiveSeconds : TimeControl(5, "0:05", titleText = "5 Seconds")
    object FifteenSeconds : TimeControl(15, "0:15", titleText = "15 Seconds")
    object ThirtySeconds : TimeControl(30, "0:30", titleText = "30 Seconds")
    object OneMinute : TimeControl(60, "1:00", titleText = "1 Minute")
    object TwoMinutes : TimeControl(120, "2:00", titleText = "2 Minutes")
    object FiveMinutes : TimeControl(300, "5:00", titleText = "5 Minutes")

    companion object {
        val allOptions = listOf(
            FiveSeconds,
            FifteenSeconds,
            ThirtySeconds,
            OneMinute,
            TwoMinutes,
            FiveMinutes
        )
    }

    // Format the remaining time as MM:SS
    fun formatTime(remainingSeconds: Int): String {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}