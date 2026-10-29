package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.local.ConnectivityHelper
import com.uniandesfood.data.local.PendingQueue
import com.uniandesfood.data.model.Review
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

enum class ReviewSubmitStatus { SENT, QUEUED }

class ReviewViewModel(
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository(),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val isOnline: () -> Boolean = { ConnectivityHelper.isOnline() },
    onlineFlow: Flow<Boolean> = ConnectivityHelper.isOnlineFlow,
    private val pendingQueue: PendingQueue = sharedPendingQueue
) : ViewModel() {

    init {
        viewModelScope.launch {
            onlineFlow.collect { online ->
                if (online) sendPendingReviews()
            }
        }
    }

    fun submitReview(
        restaurantId: String,
        rating: Int,
        comment: String = "",
        uid: String = ""
    ): ReviewSubmitStatus {
        analyticsRepository.logQrReviewSubmission(
            restaurantId = restaurantId,
            rating = rating,
            isCompleted = true
        )
        val review = Review(
            id = UUID.randomUUID().toString(),
            restaurantId = restaurantId,
            rating = rating,
            comment = comment.trim(),
            uid = uid
        )

        if (!isOnline()) {
            viewModelScope.launch { pendingQueue.add(review.toJson()) }
            return ReviewSubmitStatus.QUEUED
        }

        restaurantRepository.saveReview(review)
        viewModelScope.launch {
            restaurantRepository.submitReview(restaurantId, rating.toFloat())
        }
        return ReviewSubmitStatus.SENT
    }

    // Cancelled reviews are logged too, so the completion rate can be computed
    fun cancelReview(restaurantId: String) {
        analyticsRepository.logQrReviewSubmission(
            restaurantId = restaurantId,
            rating = 0,
            isCompleted = false
        )
    }

    private suspend fun sendPendingReviews() {
        pendingQueue.flush { json ->
            val review = json.toReview()
            restaurantRepository.saveReview(review)
            viewModelScope.launch {
                restaurantRepository.submitReview(
                    review.restaurantId,
                    review.rating.toFloat()
                )
            }
            true
        }
    }

    companion object {
        val sharedPendingQueue: PendingQueue by lazy { PendingQueue("pending_reviews.json") }
    }
}

private fun Review.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("restaurantId", restaurantId)
    .put("rating", rating)
    .put("comment", comment)
    .put("uid", uid)
    .put("timestamp", timestamp)

private fun JSONObject.toReview(): Review = Review(
    id = getString("id"),
    restaurantId = getString("restaurantId"),
    rating = getInt("rating"),
    comment = optString("comment"),
    uid = optString("uid"),
    timestamp = optLong("timestamp", System.currentTimeMillis())
)