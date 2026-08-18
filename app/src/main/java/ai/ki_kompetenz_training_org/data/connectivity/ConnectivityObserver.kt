package ai.ki_kompetenz_training_org.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes network connectivity state via ConnectivityManager.
 * Emits `true` when internet is available, `false` otherwise.
 */
class ConnectivityObserver(context: Context) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        // Emit current state
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        trySend(caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
