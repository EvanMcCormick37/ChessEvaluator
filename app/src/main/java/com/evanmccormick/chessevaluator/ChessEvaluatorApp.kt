package com.evanmccormick.chessevaluator

import android.app.Application
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.evanmccormick.chessevaluator.ui.theme.AppSettingsController

class ChessEvaluatorApp : Application() {
    companion object {
        lateinit var instance: ChessEvaluatorApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Load saved settings
        AppSettingsController.loadFromPreferences(applicationContext)

        // Then initialize Firebase (if not already done elsewhere)
        FirebaseApp.initializeApp(this)
    }

    private fun installLatestSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(applicationContext)
        } catch (e: GooglePlayServicesRepairableException) {
            // Google Play Services is out of date, prompt user to update
            GoogleApiAvailability.getInstance().showErrorNotification(applicationContext, e.connectionStatusCode)
            Log.e("SecurityProvider", "Google Play Services needs update", e)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e("SecurityProvider", "Google Play Services not available", e)
        }
    }
}