package com.example

import android.app.Application
import android.util.Log
import com.example.data.remote.FirebaseSafeManager

class SpeakUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseSafeManager.ensureFirebaseInitialized(this)
        } catch (e: Throwable) {
            Log.w("SpeakUpApp", "Firebase safe startup bypassed: ${e.message}")
        }
    }
}
