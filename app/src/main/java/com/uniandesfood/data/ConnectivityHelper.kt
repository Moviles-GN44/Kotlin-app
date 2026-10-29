package com.uniandesfood.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Detects the connectivity events of the app:
 *  - on demand: isOnline() is checked right before an action that needs internet
 *  - on change: a BroadcastReceiver updates isOnlineFlow, so the UI shows the
 *    "no connection" banner and the pending queues are sent again.
 * Before init() (JVM unit tests) it assumes the device is online.
 */
object ConnectivityHelper {

    private var appContext: Context? = null

    private val _isOnlineFlow = MutableStateFlow(true)
    val isOnlineFlow: StateFlow<Boolean> = _isOnlineFlow.asStateFlow()

    fun init(context: Context) {
        if (appContext != null) return
        val app = context.applicationContext
        appContext = app
        _isOnlineFlow.value = checkNow()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                refresh()
            }
        }
        @Suppress("DEPRECATION")
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        ContextCompat.registerReceiver(app, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun isOnline(): Boolean = if (appContext == null) true else checkNow()

    // Also called from MainActivity.onResume, in case a broadcast was missed in the background
    fun refresh() {
        if (appContext != null) _isOnlineFlow.value = checkNow()
    }

    private fun checkNow(): Boolean {
        val manager = appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}