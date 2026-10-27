package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Safely unwraps a [Context] to find its hosting [Activity].
 */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authStatusMessage = MutableStateFlow<String?>(null)
    val authStatusMessage: StateFlow<String?> = _authStatusMessage.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun clearAuthStatusMessage() {
        _authStatusMessage.value = null
    }

    fun getConfiguredWebClientId(): String? {
        return try {
            val id = context.getString(R.string.default_web_client_id).trim()
            if (id.isNotBlank() && !id.startsWith("YOUR_")) id else null
        } catch (e: Exception) {
            Log.w("AuthManager", "default_web_client_id string resource not found", e)
            null
        }
    }

    fun attemptAutoSignIn(scope: CoroutineScope) {
        if (auth.currentUser != null) {
            _currentUser.value = auth.currentUser
            return
        }

        val clientId = getConfiguredWebClientId() ?: return

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                handleCredentialResponse(result, onSuccess = {}, onError = {})
            } catch (e: Exception) {
                Log.d("AuthManager", "Silent auto sign-in skipped: ${e.message}")
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = getConfiguredWebClientId()
        if (clientId == null) {
            val msg = "Google Sign-In configuration missing: default_web_client_id not found in google-services.json."
            _authStatusMessage.value = msg
            onError(msg)
            return
        }

        _isAuthenticating.value = true
        _authStatusMessage.value = null

        scope.launch {
            try {
                // Primary attempt: Explicit Sign in with Google bottom sheet
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                val primaryRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()

                val result = try {
                    credentialManager.getCredential(activity, primaryRequest)
                } catch (primaryErr: NoCredentialException) {
                    Log.w("AuthManager", "GetSignInWithGoogleOption returned NoCredentialException, trying GetGoogleIdOption", primaryErr)
                    val fallbackOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(clientId)
                        .setAutoSelectEnabled(false)
                        .build()
                    val fallbackRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(fallbackOption)
                        .build()
                    credentialManager.getCredential(activity, fallbackRequest)
                }

                handleCredentialResponse(
                    result = result,
                    onSuccess = { user ->
                        _authStatusMessage.value = "Signed in as ${user.displayName ?: user.email ?: "Cricket Captain"}"
                        onSuccess(user)
                    },
                    onError = { err ->
                        _authStatusMessage.value = err
                        onError(err)
                    }
                )
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthManager", "Google Sign-In flow was cancelled: ${e.message}", e)
                val msg = "Google Sign-In was cancelled. Ensure a Google account is logged in on this device and your signing SHA-1 is registered in Firebase."
                _authStatusMessage.value = msg
                onError(msg)
            } catch (e: NoCredentialException) {
                Log.e("AuthManager", "No Google credentials available on device", e)
                val msg = "No Google account found or SHA-1 fingerprint mismatch. Add a Google account in Android Settings > Accounts, and verify your SHA-1 in Firebase Console."
                _authStatusMessage.value = msg
                onError(msg)
            } catch (e: GetCredentialException) {
                Log.e("AuthManager", "CredentialManager error (${e.type}): ${e.message}", e)
                val msg = buildFriendlyAuthError(e.message ?: e.type)
                _authStatusMessage.value = msg
                onError(msg)
            } catch (e: Exception) {
                Log.e("AuthManager", "Google Sign-In failed", e)
                val msg = buildFriendlyAuthError(e.localizedMessage ?: "Google Sign-In failed")
                _authStatusMessage.value = msg
                onError(msg)
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    private suspend fun handleCredentialResponse(
        result: GetCredentialResponse,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(authCredential).await()
            val user = authResult.user
            if (user != null) {
                _currentUser.value = user
                onSuccess(user)
            } else {
                onError("Firebase Authentication succeeded, but returned a null user profile.")
            }
        } else {
            onError("Unexpected credential format received from Google Identity Services.")
        }
    }

    private fun buildFriendlyAuthError(rawMessage: String): String {
        return when {
            rawMessage.contains("10:") ||
                rawMessage.contains("28444") ||
                rawMessage.contains("DEVELOPER_ERROR", ignoreCase = true) ||
                rawMessage.contains("Developer console is not set up", ignoreCase = true) -> {
                "OAuth SHA-1 Fingerprint Mismatch: Add your Android Studio debug/release keystore SHA-1 fingerprint to Firebase Console > Project Settings > Your Apps (com.rajpootcricketboys.glassedition) and re-download google-services.json."
            }
            rawMessage.contains("network", ignoreCase = true) -> {
                "Network error while connecting to Google Sign-In. Check your internet connection and try again."
            }
            else -> "Google Sign-In error: $rawMessage"
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        _authStatusMessage.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthManager", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
