package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import com.uniandesfood.data.model.FilterCriteria
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RestaurantViewModel(
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _restaurants = MutableStateFlow(restaurantRepository.getAllRestaurants())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(restaurantRepository.getAllRestaurants().firstOrNull())
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()

    fun filterRestaurants(criteria: FilterCriteria) {
        _restaurants.value = restaurantRepository.filterRestaurants(criteria)
    }

    fun selectRestaurant(restaurantId: String) {
        val found = restaurantRepository.getRestaurantById(restaurantId)
        _selectedRestaurant.value = found
        
        found?.let {
            analyticsRepository.logMenuInspection(
                restaurantId = it.id,
                dishCount = it.menu.size,
                checkedPhotos = true
            )
        }
    }
}
