package com.uniandesfood

import android.app.Application
import com.uniandesfood.data.local.AppStorage
import com.uniandesfood.data.local.ConnectivityHelper

class UniandesFoodApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppStorage.init(this)
        ConnectivityHelper.init(this)
    }
}