package com.uniandesfood.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.model.FilterCriteria
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.repository.AnalyticsRepository
import com.uniandesfood.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestaurantViewModel(
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(),
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _restaurants = MutableStateFlow(restaurantRepository.getAllRestaurants())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(restaurantRepository.getAllRestaurants().firstOrNull())
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()

    var currentBuilding by mutableStateOf("ML")
        private set

    private var currentCriteria: FilterCriteria? = null

    init {
        viewModelScope.launch {
            restaurantRepository.restaurantsFlow.collect { list ->
                if (currentCriteria != null) {
                    _restaurants.value = restaurantRepository.filterRestaurants(currentCriteria!!)
                } else {
                    _restaurants.value = list
                }
                if (_selectedRestaurant.value == null || !_restaurants.value.any { it.id == _selectedRestaurant.value?.id }) {
                    _selectedRestaurant.value = _restaurants.value.firstOrNull()
                }
            }
        }
    }

    fun filterRestaurants(criteria: FilterCriteria) {
        currentCriteria = criteria
        currentBuilding = criteria.selectedBuilding
        val filtered = restaurantRepository.filterRestaurants(criteria)
        _restaurants.value = filtered
        _selectedRestaurant.value = filtered.firstOrNull() ?: restaurantRepository.getAllRestaurants().firstOrNull()
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
