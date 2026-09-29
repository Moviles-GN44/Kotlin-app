package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import com.uniandesfood.data.repository.AnalyticsRepository

class ReviewViewModel(
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    fun submitReview(restaurantId: String, rating: Int) {
        analyticsRepository.logQrReviewSubmission(
            restaurantId = restaurantId,
            rating = rating,
            isCompleted = true
        )
    }

    // Cancelled reviews are logged too, so the completion rate can be computed
    fun cancelReview(restaurantId: String) {
        analyticsRepository.logQrReviewSubmission(
            restaurantId = restaurantId,
            rating = 0,
            isCompleted = false
        )
    }
}