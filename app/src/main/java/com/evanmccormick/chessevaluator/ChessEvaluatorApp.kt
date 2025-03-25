package com.evanmccormick.chessevaluator

import android.app.Application

class ChessEvaluatorApp : Application() {
    companion object {
        lateinit var instance: ChessEvaluatorApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}