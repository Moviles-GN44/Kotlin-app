package com.uniandesfood.data.local

import java.io.File

/**
 * Reads and writes small text/JSON files in the app-specific internal storage.
 * These calls touch the disk, so callers must run them inside withContext(Dispatchers.IO).
 */
object FileStore {

    @Synchronized
    fun read(fileName: String): String? {
        val dir = AppStorage.filesDir ?: return null
        val file = File(dir, fileName)
        return if (file.exists()) file.readText() else null
    }

    // Writes to a temporary file first and then renames it, so a crash or a dead battery
    // in the middle of a write never leaves a half-written (corrupt) file.
    @Synchronized
    fun write(fileName: String, text: String) {
        val dir = AppStorage.filesDir ?: return
        val tmp = File(dir, "$fileName.tmp")
        tmp.writeText(text)
        tmp.renameTo(File(dir, fileName))
    }
}