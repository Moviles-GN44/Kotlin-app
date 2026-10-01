package com.uniandesfood.viewmodel

import com.uniandesfood.data.model.MenuItem
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelTest {

    private val burgerDishes = listOf(
        MenuItem(id = "bp1", name = "Classic Burger"),
        MenuItem(id = "bp2", name = "Fries")
    )
    private val burger = Restaurant(id = "burger_play_rgd", name = "Burger Play", menu = burgerDishes)
    private val burrito = Restaurant(
        id = "one_burrito_ml",
        name = "One Burrito",
        menu = listOf(MenuItem(id = "ob1", name = "Burrito"))
    )

    private val restaurantRepository = mockk<RestaurantRepository>(relaxed = true)
    private val analyticsRepository = mockk<AnalyticsRepository>(relaxed = true)

    @Before
    fun setUp() {
        // viewModelScope needs a Main dispatcher, which does not exist in JVM unit tests
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        restaurants: List<Restaurant> = listOf(burger, burrito)
    ): RestaurantViewModel {
        every { restaurantRepository.getAllRestaurants() } returns restaurants
        every { restaurantRepository.restaurantsFlow } returns MutableStateFlow(restaurants)
        return RestaurantViewModel(restaurantRepository, analyticsRepository)
    }

    @Test
    fun selectRestaurant_logsMenuInspectionWithoutPhotoCheck() {
        val viewModel = createViewModel()
        every { restaurantRepository.getRestaurantById("one_burrito_ml") } returns burrito

        viewModel.selectRestaurant("one_burrito_ml")

        assertEquals(burrito, viewModel.selectedRestaurant.value)
        verify(exactly = 1) {
            analyticsRepository.logMenuInspection(
                restaurantId = "one_burrito_ml",
                dishCount = 1,
                checkedPhotos = false
            )
        }
    }

    @Test
    fun onDishPhotoOpened_logsMenuInspectionWithPhotoCheckAndDishId() {
        val viewModel = createViewModel()   // first restaurant (burger) is selected by default

        viewModel.onDishPhotoOpened(burgerDishes[0])

        verify(exactly = 1) {
            analyticsRepository.logMenuInspection(
                restaurantId = "burger_play_rgd",
                dishCount = 2,
                checkedPhotos = true,
                dishId = "bp1"
            )
        }
    }

    @Test
    fun onDishPhotoOpened_withoutSelectedRestaurant_logsNothing() {
        val viewModel = createViewModel(restaurants = emptyList())

        viewModel.onDishPhotoOpened(burgerDishes[0])

        verify(exactly = 0) {
            analyticsRepository.logMenuInspection(any(), any(), any(), any())
        }
    }

    @Test
    fun findById_returnsRestaurantEvenWithExtraSpaces() {
        val viewModel = createViewModel()

        assertEquals(burger, viewModel.findById("  burger_play_rgd "))
    }

    @Test
    fun findById_returnsNullForUnknownQr() {
        val viewModel = createViewModel()

        assertNull(viewModel.findById("not_a_restaurant"))
    }
}