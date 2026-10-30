package com.uniandesfood.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandesfood.data.local.FileStore
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

class PopularDishesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val memoryCache = mutableMapOf<String, CachedCounts>()

    data class CachedCounts(
        val counts: Map<String, Int>,
        val savedAt: Long
    )

    suspend fun getDishPhotoViewCounts(restaurantId: String): Map<String, Int> {
        readCache(restaurantId)?.let { cached ->
            if (isCacheFresh(restaurantId)) {
                return cached.counts
            }
        }

        Log.d("Cache", "Network read for $restaurantId")
        val fromNetwork = readFromFirestore(restaurantId)
        saveCache(restaurantId, fromNetwork)
        return fromNetwork
    }

    private fun readCache(restaurantId: String): CachedCounts? {
        memoryCache.get(restaurantId)?.let {
            Log.d("Cache", "Memory hit for $restaurantId")
            return it
        }

        try {
            val fileContent = FileStore.read(getCacheFileName(restaurantId)) ?: return null
            val entry = JSONObject(fileContent)
            val countsObj = entry.getJSONObject("counts")
            val counts = keysToMap(countsObj)
            
            Log.d("Cache", "File hit for $restaurantId")
            return CachedCounts(counts, entry.getLong("savedAt")).also { memoryCache.put(restaurantId, it) }
        } catch (_: Exception) {
            return null
        }
    }

    fun isCacheFresh(restaurantId: String): Boolean {
        val cached = memoryCache[restaurantId] ?: readCache(restaurantId) ?: return false
        val age = System.currentTimeMillis() - cached.savedAt
        return age < CACHE_TTL_MS
    }

    private suspend fun readFromFirestore(restaurantId: String): Map<String, Int> =
        suspendCancellableCoroutine { continuation ->
            firestore.collection("telemetry_events")
                .whereEqualTo("eventName", "karin_bq_menu_inspection")
                .whereEqualTo("params.restaurant_id", restaurantId)
                .whereEqualTo("params.checked_photos", true)
                .limit(MAX_EVENTS)
                .get()
                .addOnSuccessListener { snapshot ->
                    val sessionsByDish = mutableMapOf<String, MutableSet<String>>()
                    snapshot.documents.forEach { doc ->
                        val params = doc.get("params") as? Map<*, *>
                        val dishId = params?.get("dish_id") as? String ?: return@forEach
                        val sessionId = params["session_id"] as? String ?: doc.id
                        sessionsByDish.getOrPut(dishId) { mutableSetOf() }.add(sessionId)
                    }
                    continuation.resume(sessionsByDish.mapValues { it.value.size })
                }
                .addOnFailureListener { e ->
                    Log.w("PopularDishes", "Failed to read telemetry", e)
                    continuation.resume(emptyMap())
                }
        }

    private fun saveCache(restaurantId: String, counts: Map<String, Int>) {
        val savedAt = System.currentTimeMillis()
        val cached = CachedCounts(counts, savedAt)
        memoryCache[restaurantId] = cached

        try {
            val entry = JSONObject().apply {
                put("counts", JSONObject(counts as Map<String, Any>))
                put("savedAt", savedAt)
            }
            FileStore.write(getCacheFileName(restaurantId), entry.toString())
        } catch (e: Exception) {
            Log.w("PopularDishesRepository", "Could not save cache", e)
        }
    }

    private fun getCacheFileName(restaurantId: String) = "popular_dishes_$restaurantId.json"

    private fun keysToMap(jsonObj: JSONObject): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for (key in jsonObj.keys()) {
            map[key] = jsonObj.getInt(key)
        }
        return map
    }

    companion object {
        private const val MAX_EVENTS = 500L
        private const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes
    }
}