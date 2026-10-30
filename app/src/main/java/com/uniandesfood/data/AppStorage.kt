package com.uniandesfood.data.local

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * Holds the two private storage locations of the app:
 *  - the internal files directory (app-specific files)
 *  - the SharedPreferences file (key-value data)
 * It is initialized once from UniandesFoodApp. Until then (for example in JVM unit tests)
 * both values are null and the local stores simply do nothing.
 */
object AppStorage {
    @Volatile
    var filesDir: File? = null
        private set

    @Volatile
    var prefs: SharedPreferences? = null
        private set

    fun init(context: Context) {
        val app = context.applicationContext
        filesDir = app.filesDir
        prefs = app.getSharedPreferences("uniandes_food_prefs", Context.MODE_PRIVATE)
    }
}