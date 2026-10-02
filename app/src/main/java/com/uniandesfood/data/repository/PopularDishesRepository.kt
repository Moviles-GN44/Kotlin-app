package com.uniandesfood.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PopularDishesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Counts how many distinct sessions opened the photo of each dish of a restaurant.
    // It reads the same telemetry events used by BQ4 (checked_photos = true).
    // If the read fails (no internet, rules), it returns an empty map and the UI shows no labels.
    suspend fun getDishPhotoViewCounts(restaurantId: String): Map<String, Int> =
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

    companion object {
        private const val MAX_EVENTS = 500L
    }
}