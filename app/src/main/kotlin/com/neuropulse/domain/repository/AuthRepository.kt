package com.neuropulse.domain.repository

/**
 * AuthRepository — authentication operations for NeuroPulse.
 *
 * This interface is implemented in the data layer ([com.neuropulse.data.auth.FirebaseAuthRepositoryImpl]).
 * The domain layer references this interface only — zero Firebase imports here (ADR-001).
 *
 * All sign-in methods return [Result] so the caller handles success/failure
 * without catching raw exceptions. The UID returned on success is the stable
 * Firebase user identifier, stored in DataStore for session persistence.
 *
 * [currentUser] returns [Any]? deliberately — returning `FirebaseUser?` here
 * would require importing Firebase in the domain layer. Callers use this
 * as a presence check (`!= null`) only.
 */
interface AuthRepository {

    /**
     * Signs in via an OAuth browser-redirect flow for Yahoo, Microsoft, or Apple (DD-012).
     *
     * Uses Firebase's `startActivityForSignInWithProvider` which launches a CustomTab,
     * completes the provider's OAuth handshake, and returns the authenticated Firebase user.
     *
     * [activity] is typed as [Any] to avoid importing [android.app.Activity] in the domain
     * layer (ADR-001). The data layer casts it to [android.app.Activity] internally.
     *
     * Supported [providerId] values: `"yahoo.com"`, `"microsoft.com"`, `"apple.com"`.
     * Apple Sign-In automatically adds the `email` and `name` scopes (required by Apple).
     *
     * @param activity   The current foreground [android.app.Activity] (passed from NavGraph).
     * @param providerId Firebase OAuth provider ID string.
     * @return [Result.success] with the Firebase UID, or [Result.failure] with the cause.
     */
    suspend fun signInWithOAuthProvider(activity: Any, providerId: String): Result<String>

    /**
     * Signs in with a Google ID token obtained from the Google One-Tap UI.
     *
     * @param idToken The ID token string from [com.google.android.gms.auth.api.signin.GoogleSignInAccount].
     * @return [Result.success] with the Firebase UID, or [Result.failure] with the cause.
     */
    suspend fun signInWithGoogle(idToken: String): Result<String>

    /**
     * Signs in anonymously without requiring any credentials.
     *
     * Used for guest/try-without-account flow (E-005, DD-015). The returned UID is
     * intentionally NOT persisted to DataStore — the session expires when the app is killed,
     * routing the user back to the login screen on next launch. This is by design.
     *
     * @return [Result.success] with a temporary Firebase UID, or [Result.failure] with the cause.
     */
    suspend fun signInAnonymously(): Result<String>

    /**
     * Returns true if the currently signed-in Firebase user is anonymous (guest session).
     *
     * Used by [com.neuropulse.ui.home.HomeViewModel] to show the persistent guest banner
     * on HomeScreen reminding the user that their progress won't be saved (E-005).
     */
    fun isCurrentUserAnonymous(): Boolean

    /**
     * Signs in with an email and password combination.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return [Result.success] with the Firebase UID, or [Result.failure] with the cause.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<String>

    /**
     * Creates a new email/password account.
     *
     * @param email    The user's email address.
     * @param password The chosen password.
     * @return [Result.success] with the new Firebase UID, or [Result.failure] with the cause.
     */
    suspend fun createAccountWithEmail(email: String, password: String): Result<String>

    /**
     * Sends a password reset email to the given address.
     *
     * If an account exists for that email, Firebase sends a reset link. If no account exists,
     * Firebase still succeeds (to prevent email enumeration attacks). The ViewModel shows
     * a success message in both cases ("Check your email for reset instructions").
     *
     * @param email The email address to send the reset link to.
     * @return [Result.success] (Unit) on success, or [Result.failure] with the cause (e.g., network error).
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Checks whether the current Firebase token is still valid.
     *
     * Uses local expiry check (forceRefresh = false) to avoid a network call on cold start.
     * Only force-refreshes on auth exception, reducing startup latency for returning users.
     *
     * @return true if a valid, unexpired token exists; false otherwise.
     */
    suspend fun isTokenValid(): Boolean

    /**
     * Signs the current user out and clears the local Firebase token cache.
     */
    suspend fun signOut()

    /**
     * Returns the currently signed-in user object, or null if not signed in.
     *
     * Typed as [Any]? to avoid importing Firebase classes in the domain layer (ADR-001).
     * Use only as a null check — cast in the data layer if the concrete type is needed.
     */
    fun currentUser(): Any?
}
