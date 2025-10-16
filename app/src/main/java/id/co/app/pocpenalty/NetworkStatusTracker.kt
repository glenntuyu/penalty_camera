package id.co.app.pocpenalty

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import javax.inject.Inject

class NetworkStatusTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    val networkStatus = callbackFlow {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                this@callbackFlow.trySend(NetworkStatus.Unavailable).isSuccess
            }

            override fun onAvailable(network: Network) {
                try {
                    val timeoutMs = 3000
                    val sock = Socket()
                    val socketAddress: SocketAddress =
                        InetSocketAddress(ConstantVariable.DEFAULT_ADDRESS, 8443)
                    sock.connect(socketAddress, timeoutMs)
                    sock.close()
                    this@callbackFlow.trySend(NetworkStatus.Available).isSuccess
                } catch (e: IOException) {
                    this@callbackFlow.trySend(NetworkStatus.Unavailable).isSuccess
                }
            }

            override fun onLost(network: Network) {
                this@callbackFlow.trySend(NetworkStatus.Unavailable).isSuccess
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }

    fun triggerEventClick(): Flow<Boolean> = flow {
        val netInfo = connectivityManager.activeNetworkInfo
        if (netInfo != null && netInfo.isConnectedOrConnecting) {
            try {
                val timeoutMs = 3000
                val sock = Socket()
                val socketAddress: SocketAddress =
                    InetSocketAddress(ConstantVariable.DEFAULT_ADDRESS, ConstantVariable.DEFAULT_PORT_INTERNET)
                sock.connect(socketAddress, timeoutMs)
                sock.close()
                emit(true)
            } catch (e: IOException) {
                emit(false)
            }
        } else emit(false)
    }.flowOn(Dispatchers.IO)

    fun isInternetNetwork(): Boolean {
        return try {
            val timeoutMs = 3000
            val sock = Socket()
            val socketAddress: SocketAddress =
                InetSocketAddress(ConstantVariable.DEFAULT_ADDRESS, ConstantVariable.DEFAULT_PORT_INTERNET)
            sock.connect(socketAddress, timeoutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }
}

inline fun <Result> Flow<NetworkStatus>.map(
    crossinline onUnavailable: suspend () -> Result,
    crossinline onAvailable: suspend () -> Result,
): Flow<Result> = map { status ->
    when (status) {
        NetworkStatus.Unavailable -> onUnavailable()
        NetworkStatus.Available -> onAvailable()
    }
}

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Unavailable : NetworkStatus()
}