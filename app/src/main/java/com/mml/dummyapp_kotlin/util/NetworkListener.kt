package com.mml.dummyapp_kotlin.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkListener : ConnectivityManager.NetworkCallback() {

    private var isNetworkAvailable = MutableStateFlow(false)

    fun checkNetworkAvailability(context: Context): MutableStateFlow<Boolean> {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var isConnected = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(this)
            connectivityManager.allNetworks.forEach { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                networkCapabilities?.let {
                    if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        isConnected = true

                        return@forEach
                    }
                }
            }

            isNetworkAvailable.value = isConnected
        } else {
            val networkChangeFilter = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(networkChangeFilter, this)
            connectivityManager.allNetworks.forEach { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                networkCapabilities?.let {
                    if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        isConnected = true

                        return@forEach
                    }
                }
            }

            isNetworkAvailable.value = isConnected
        }
        return isNetworkAvailable
    }

    override fun onAvailable(network: Network) {
        isNetworkAvailable.value = true
    }

    override fun onLost(network: Network) {
        isNetworkAvailable.value = false
    }
}