package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.BuildConfig
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed class AuthStateResult {
    data class Success(val uid: String, val email: String, val displayName: String, val photoUrl: String?, val isNewUser: Boolean) : AuthStateResult()
    data class Error(val message: String) : AuthStateResult()
    data object Cancelled : AuthStateResult()
}

class FirebaseAuthService(private val context: Context) {
    private val auth: FirebaseAuth? get() = FirebaseSafeManager.getAuth(context)
    private val credentialManager: CredentialManager? = try {
        CredentialManager.create(context)
    } catch (e: Throwable) {
        Log.w("AuthService", "CredentialManager creation failed: ${e.message}")
        null
    }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Throwable) {
            null
        }

    val isUserLoggedIn: Boolean
        get() = currentUser != null

    suspend fun signInWithGoogle(activityContext: Context? = null): AuthStateResult {
        val currentAuth = auth
        if (currentAuth == null) {
            return AuthStateResult.Error("Google Sign-In is unavailable on this device. You can use 'Continue as Guest' or 'Email'.")
        }
        val cm = credentialManager
        if (cm == null) {
            return AuthStateResult.Error("Credential Manager is not supported on this device. Please continue as Guest or with Email.")
        }

        return try {
            // Compute nonce
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val serverClientId = getWebClientId()
            if (serverClientId.isNullOrBlank()) {
                return AuthStateResult.Error(
                    "Google Sign-In configuration missing: 'google-services.json' is not yet linked or does not contain 'default_web_client_id'. Please add google-services.json to the app/ folder and register SHA-1 in Firebase Console."
                )
            }

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val effectiveContext = activityContext ?: context
            val result = cm.getCredential(
                request = request,
                context = effectiveContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = currentAuth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw Exception("Firebase user is null after sign in")
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                AuthStateResult.Success(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: googleIdTokenCredential.displayName ?: "Learner",
                    photoUrl = user.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                    isNewUser = isNewUser
                )
            } else {
                AuthStateResult.Error("Unsupported credential response. You can continue as Guest or with Email.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("AuthService", "Google Sign In cancelled by user")
            AuthStateResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.e("AuthService", "Google Sign In Credential Error", e)
            AuthStateResult.Error("Google Sign-In notice: ${e.localizedMessage ?: "Could not complete Google Sign-In"}. You can also continue as Guest.")
        } catch (e: Throwable) {
            Log.e("AuthService", "Authentication Error", e)
            AuthStateResult.Error(e.localizedMessage ?: "Authentication failed. You can continue as Guest.")
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthStateResult {
        val currentAuth = auth
        if (currentAuth == null) {
            // Local fallback login
            val cleanEmail = email.trim()
            val pseudoUid = "local_" + cleanEmail.replace(Regex("[^a-zA-Z0-9]"), "_")
            return AuthStateResult.Success(
                uid = pseudoUid,
                email = cleanEmail,
                displayName = cleanEmail.substringBefore("@"),
                photoUrl = null,
                isNewUser = false
            )
        }
        return try {
            val result = currentAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User not found")
            AuthStateResult.Success(
                uid = user.uid,
                email = user.email ?: email,
                displayName = user.displayName ?: email.substringBefore("@"),
                photoUrl = user.photoUrl?.toString(),
                isNewUser = false
            )
        } catch (e: Throwable) {
            AuthStateResult.Error(e.localizedMessage ?: "Email sign-in failed")
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): AuthStateResult {
        val currentAuth = auth
        if (currentAuth == null) {
            // Local fallback registration
            val cleanEmail = email.trim()
            val pseudoUid = "local_" + cleanEmail.replace(Regex("[^a-zA-Z0-9]"), "_")
            return AuthStateResult.Success(
                uid = pseudoUid,
                email = cleanEmail,
                displayName = name.trim().ifBlank { cleanEmail.substringBefore("@") },
                photoUrl = null,
                isNewUser = true
            )
        }
        return try {
            val result = currentAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User registration failed")
            AuthStateResult.Success(
                uid = user.uid,
                email = user.email ?: email,
                displayName = name.trim().ifBlank { user.displayName ?: "Learner" },
                photoUrl = user.photoUrl?.toString(),
                isNewUser = true
            )
        } catch (e: Throwable) {
            AuthStateResult.Error(e.localizedMessage ?: "Account creation failed")
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase service unavailable"))
        return try {
            currentAuth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            Log.e("AuthService", "Error signing out", e)
        }
    }

    private fun getWebClientId(): String {
        return try {
            val stringResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            val resId = if (stringResId == 0) {
                context.resources.getIdentifier("default_web_client_id", "string", "com.example")
            } else {
                stringResId
            }
            if (resId != 0) {
                val value = context.getString(resId).trim()
                if (value.isNotBlank()) value else "363612891218-680iop7okt4m31jtveesughfuhjbans9.apps.googleusercontent.com"
            } else {
                "363612891218-680iop7okt4m31jtveesughfuhjbans9.apps.googleusercontent.com"
            }
        } catch (e: Exception) {
            "363612891218-680iop7okt4m31jtveesughfuhjbans9.apps.googleusercontent.com"
        }
    }
}
