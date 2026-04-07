package com.neuropulse.data.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.neuropulse.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseAuthRepositoryImpl — Firebase Auth implementation of [AuthRepository].
 *
 * All Firebase calls are wrapped in [runCatching] and returned as [Result],
 * so no raw exceptions escape to the presentation layer. The ViewModel's
 * error handler receives a typed [Result.failure] and maps it to a user string.
 *
 * [withContext(Dispatchers.IO)] is used for all network calls so the ViewModel
 * can call these functions from [kotlinx.coroutines.CoroutineScope.viewModelScope]
 * without specifying a dispatcher.
 *
 * Privacy: no raw biometric data passes through this class. Firebase only receives
 * auth credentials (token, email/password). UIDs are opaque identifiers (ADR-002).
 */
@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously(): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val result = auth.signInAnonymously().await()
                result.user?.uid
                    ?: error("Anonymous sign-in returned null user")
            }.also { result ->
                result.fold(
                    onSuccess = { Timber.tag("NeuroPulse").d("Auth: anonymous sign-in success") },
                    onFailure = { Timber.tag("NeuroPulse").w(it, "Auth: anonymous sign-in failed") },
                )
            }
        }

    override fun isCurrentUserAnonymous(): Boolean =
        auth.currentUser?.isAnonymous == true

    /**
     * [activity] is typed as [Any] in the domain interface (ADR-001 — no Android in domain).
     * Cast to [Activity] here in the data layer. Safe because the NavGraph always passes
     * `LocalContext.current as FragmentActivity`.
     *
     * Apple requires `email` and `name` scopes — other providers use default scopes.
     * `startActivityForSignInWithProvider` must run on the Main dispatcher (launches Activity).
     * `await()` suspends without blocking once the browser tab opens.
     */
    override suspend fun signInWithOAuthProvider(activity: Any, providerId: String): Result<String> =
        withContext(Dispatchers.Main) {
            runCatching {
                val builder = OAuthProvider.newBuilder(providerId)
                if (providerId == "apple.com") {
                    builder.setScopes(listOf("email", "name"))
                }
                val result = auth
                    .startActivityForSignInWithProvider(activity as Activity, builder.build())
                    .await()
                result.user?.uid
                    ?: error("OAuth sign-in returned null user for provider: $providerId")
            }.also { result ->
                result.fold(
                    onSuccess = { Timber.tag("NeuroPulse").d("Auth: OAuth sign-in success ($providerId)") },
                    onFailure = { Timber.tag("NeuroPulse").w(it, "Auth: OAuth sign-in failed ($providerId)") },
                )
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.uid
                    ?: error("Firebase returned null user after Google sign-in")
            }.also { result ->
                result.fold(
                    onSuccess = { Timber.tag("NeuroPulse").d("Auth: Google sign-in success") },
                    onFailure = { Timber.tag("NeuroPulse").w(it, "Auth: Google sign-in failed") },
                )
            }
        }

    override suspend fun signInWithEmail(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.uid
                    ?: error("Firebase returned null user after email sign-in")
            }.also { result ->
                result.fold(
                    onSuccess = { Timber.tag("NeuroPulse").d("Auth: email sign-in success") },
                    onFailure = { Timber.tag("NeuroPulse").w(it, "Auth: email sign-in failed") },
                )
            }
        }

    override suspend fun createAccountWithEmail(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.uid
                    ?: error("Firebase returned null user after account creation")
            }.also { result ->
                result.fold(
                    onSuccess = { Timber.tag("NeuroPulse").d("Auth: account created") },
                    onFailure = { Timber.tag("NeuroPulse").w(it, "Auth: account creation failed") },
                )
            }
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                auth.sendPasswordResetEmail(email).await()
                Unit
            }.also { result ->
                result.fold(
                    onSuccess = { Timber.tag("NeuroPulse").d("Auth: password reset email sent") },
                    onFailure = { Timber.tag("NeuroPulse").w(it, "Auth: password reset email failed") },
                )
            }
        }

    /**
     * Checks token validity using local expiry first (forceRefresh = false).
     *
     * Using forceRefresh=true on every app start would add a network round-trip
     * to the cold-start critical path. Local check is sufficient for the auth gate —
     * if the token is locally valid it will be accepted by Firebase on the next
     * authenticated API call. Only falls back to network on FirebaseAuthException.
     */
    override suspend fun isTokenValid(): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                // forceRefresh = false: reads local token expiry without a network call
                auth.currentUser?.getIdToken(false)?.await() != null
            }.getOrDefault(false)
        }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        auth.signOut()
        Timber.tag("NeuroPulse").d("Auth: signed out")
    }

    override fun currentUser(): Any? = auth.currentUser

    companion object {
        /**
         * Maps a Firebase auth exception to a user-readable string.
         *
         * Called from [com.neuropulse.ui.onboarding.LoginViewModel.mapAuthError].
         * Centralised here so the ViewModel does not import Firebase exception classes.
         */
        fun mapFirebaseError(exception: Throwable): String = when (exception) {
            is FirebaseAuthInvalidCredentialsException ->
                "Check your email address or password and try again"
            is FirebaseAuthInvalidUserException ->
                "No account found for this email — set one up below"
            is FirebaseAuthUserCollisionException ->
                "An account already exists for this email — try signing in"
            else ->
                "Sign-in failed — check your connection and try again"
        }
    }
}
