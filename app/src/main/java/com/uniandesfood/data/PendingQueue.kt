package com.uniandesfood.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Queue of actions that could not be sent because there was no connection
 * (reviews and telemetry events). Every item is a JSON object with a unique "id".
 * The queue lives in memory and is copied to an app-specific file, so the pending items
 * survive if the app is closed or the phone runs out of battery.
 */
class PendingQueue(
    private val fileName: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val items = mutableListOf<JSONObject>()
    private var loaded = false

    // Only one flush at a time, so an item is never sent twice
    private val flushing = AtomicBoolean(false)

    suspend fun add(item: JSONObject) {
        withContext(ioDispatcher) {
            val snapshot = synchronized(this@PendingQueue) {
                loadIfNeeded()
                if (items.none { it.optString("id") == item.optString("id") }) items.add(item)
                asJsonString()
            }
            FileStore.write(fileName, snapshot)
        }
    }

    suspend fun all(): List<JSONObject> = withContext(ioDispatcher) {
        synchronized(this@PendingQueue) {
            loadIfNeeded()
            items.toList()
        }
    }

    suspend fun size(): Int = all().size

    private suspend fun remove(id: String) {
        withContext(ioDispatcher) {
            val snapshot = synchronized(this@PendingQueue) {
                items.removeAll { it.optString("id") == id }
                asJsonString()
            }
            FileStore.write(fileName, snapshot)
        }
    }

    /**
     * Sends the pending items one by one, oldest first. If one item fails it stops and keeps
     * the rest in the queue for the next connection.
     */
    suspend fun flush(send: suspend (JSONObject) -> Boolean) {
        if (!flushing.compareAndSet(false, true)) return
        try {
            for (item in all()) {
                if (!send(item)) break
                remove(item.optString("id"))
            }
        } finally {
            flushing.set(false)
        }
    }

    // Must be called inside synchronized(this)
    private fun loadIfNeeded() {
        if (loaded) return
        loaded = true
        val text = FileStore.read(fileName) ?: return
        try {
            val array = JSONArray(text)
            for (i in 0 until array.length()) items.add(array.getJSONObject(i))
        } catch (_: Exception) {
            items.clear() // corrupt file: start with an empty queue instead of crashing
        }
    }

    private fun asJsonString(): String {
        val array = JSONArray()
        items.forEach { array.put(it) }
        return array.toString()
    }
}