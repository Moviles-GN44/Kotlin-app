package com.uniandesfood.viewmodel

import com.uniandesfood.data.local.PendingQueue
import com.uniandesfood.data.model.Review
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val analyticsRepository = mockk<AnalyticsRepository>(relaxed = true)
    private val restaurantRepository = mockk<RestaurantRepository>(relaxed = true)
    private lateinit var viewModel: ReviewViewModel

    @Before
    fun setUp() {
        // ViewModel needs a Main dispatcher, which does not exist in JVM unit tests
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ReviewViewModel(
            analyticsRepository = analyticsRepository,
            restaurantRepository = restaurantRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitReview_logsCompletedReviewWithRating() {
        viewModel.submitReview(restaurantId = "one_burrito_ml", rating = 4)
        verify(exactly = 1) {
            analyticsRepository.logQrReviewSubmission(
                restaurantId = "one_burrito_ml",
                rating = 4,
                isCompleted = true
            )
        }
    }

    @Test
    fun submitReview_savesReviewWithCommentAndUid() {
        val saved = slot<Review>()
        viewModel.submitReview(
            restaurantId = "one_burrito_ml",
            rating = 4,
            comment = "  Great food  ",
            uid = "user123"
        )
        verify(exactly = 1) { restaurantRepository.saveReview(capture(saved)) }
        assertEquals("one_burrito_ml", saved.captured.restaurantId)
        assertEquals(4, saved.captured.rating)
        assertEquals("Great food", saved.captured.comment)
        assertEquals("user123", saved.captured.uid)
    }

    @Test
    fun cancelReview_logsIncompleteReviewWithZeroRating() {
        viewModel.cancelReview(restaurantId = "one_burrito_ml")
        verify(exactly = 1) {
            analyticsRepository.logQrReviewSubmission(
                restaurantId = "one_burrito_ml",
                rating = 0,
                isCompleted = false
            )
        }
    }

    @Test
    fun submitReview_withoutConnectionQueuesTheReviewInsteadOfSendingIt() = runTest {
        val queue = PendingQueue("test_reviews.json", UnconfinedTestDispatcher())
        val offlineViewModel = ReviewViewModel(
            analyticsRepository = analyticsRepository,
            restaurantRepository = restaurantRepository,
            isOnline = { false },
            onlineFlow = MutableStateFlow(false),
            pendingQueue = queue
        )
        val status = offlineViewModel.submitReview(restaurantId = "one_burrito_ml", rating = 5, comment = "Rico")
        assertEquals(ReviewSubmitStatus.QUEUED, status)
        assertEquals(1, queue.size())
        verify(exactly = 0) { restaurantRepository.saveReview(any()) }
    }

    @Test
    fun pendingReviews_areSentWhenTheConnectionComesBack() = runTest {
        val queue = PendingQueue("test_reviews.json", UnconfinedTestDispatcher())
        val online = MutableStateFlow(false)
        val offlineViewModel = ReviewViewModel(
            analyticsRepository = analyticsRepository,
            restaurantRepository = restaurantRepository,
            isOnline = { online.value },
            onlineFlow = online,
            pendingQueue = queue
        )
        offlineViewModel.submitReview(restaurantId = "one_burrito_ml", rating = 4)
        online.value = true 
        advanceUntilIdle()
        verify(exactly = 1) { restaurantRepository.saveReview(any()) }
        assertEquals(0, queue.size())
    }
}