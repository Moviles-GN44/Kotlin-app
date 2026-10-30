package com.uniandesfood

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import com.uniandesfood.data.local.AppStorage
import com.uniandesfood.data.local.ConnectivityHelper

class UniandesFoodApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStorage.init(this)
        ConnectivityHelper.init(this)
        
        // Para ver: logs a warning if the main thread does disk or network work
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
        }
    }
}