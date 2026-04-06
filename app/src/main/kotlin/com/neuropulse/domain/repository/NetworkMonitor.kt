package com.neuropulse.domain.repository

/**
 * NetworkMonitor — connectivity check for the domain layer.
 *
 * Pure Kotlin interface — zero Android imports (ADR-001).
 * Implemented by [com.neuropulse.data.network.ConnectivityNetworkMonitor]
 * using [android.net.ConnectivityManager].
 *
 * Called at the start of each auth operation in [com.neuropulse.ui.onboarding.LoginViewModel]
 * to surface an immediate offline error instead of a ~10-second Firebase timeout (E-004).
 *
 * Note: this is a point-in-time check (best-effort). A connection that drops
 * mid-auth will still result in a Firebase network error — that path is handled
 * by the existing [FirebaseAuthRepositoryImpl] runCatching + mapFirebaseError chain.
 */
interface NetworkMonitor {

    /**
     * Returns true if the device currently has an active internet-capable network.
     *
     * Uses [android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET] — checks that
     * the network is validated and has a route to the internet (not just that a
     * network interface exists). Wi-Fi, mobile, and VPN are all considered.
     */
    suspend fun isOnline(): Boolean
}
