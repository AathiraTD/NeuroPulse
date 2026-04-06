package com.neuropulse.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.neuropulse.domain.repository.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ConnectivityNetworkMonitor — Android implementation of [NetworkMonitor].
 *
 * Uses [ConnectivityManager.getNetworkCapabilities] with [NetworkCapabilities.NET_CAPABILITY_INTERNET]
 * (API 23+, safe given minSdk=26). This verifies the network is validated by the OS —
 * captive portals and interfaces with no actual internet route return false.
 *
 * [ConnectivityManager] is obtained via [Context.getSystemService] rather than being
 * constructor-injected — system services are singletons managed by Android and do not
 * need to pass through Hilt's graph. The [ApplicationContext] ensures no Activity leak.
 *
 * Not a suspend function internally — [ConnectivityManager] APIs are synchronous.
 * The [NetworkMonitor] interface declares [isOnline] as suspend for future compatibility
 * if an async check (e.g. ICMP ping) is ever needed.
 */
@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) : NetworkMonitor {

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService()!!

    override suspend fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
