package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSafeManager {

    fun ensureFirebaseInitialized(context: Context): Boolean {
        return try {
            val appContext = context.applicationContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(appContext)
                } catch (e: Throwable) {
                    val apiKey = "AIzaSyCpX4pNmwMc-DQnvXJ08wImBO2QiEnQRUw"
                    try {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:363612891218:android:f51c10999dc1ae359fd78b")
                            .setProjectId("gen-lang-client-0722682307")
                            .setApiKey(apiKey)
                            .setStorageBucket("gen-lang-client-0722682307.firebasestorage.app")
                            .build()
                        FirebaseApp.initializeApp(appContext, options)
                    } catch (e2: Throwable) {
                        Log.w("FirebaseSafeManager", "Fallback FirebaseOptions failed: ${e2.message}")
                    }
                }
            }
            FirebaseApp.getApps(appContext).isNotEmpty()
        } catch (e: Throwable) {
            Log.w("FirebaseSafeManager", "FirebaseApp init check failed: ${e.message}")
            false
        }
    }

    fun getAuth(context: Context? = null): FirebaseAuth? {
        return try {
            if (context != null) {
                ensureFirebaseInitialized(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseSafeManager", "FirebaseAuth is not available: ${e.message}")
            null
        }
    }

    fun getFirestore(context: Context? = null): FirebaseFirestore? {
        return try {
            if (context != null) {
                ensureFirebaseInitialized(context)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseSafeManager", "FirebaseFirestore is not available: ${e.message}")
            null
        }
    }
}
