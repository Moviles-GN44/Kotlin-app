package com.uniandesfood.data.repository

import com.uniandesfood.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RestaurantRepository {

    private val sampleRestaurants = listOf(
        Restaurant(
            id = "one_burrito_ml",
            name = "One Burrito - ML",
            buildingTag = "ML",
            walkDistancesFromBuilding = mapOf("ML" to 2, "RGD" to 5, "Franco" to 7, "C" to 4, "W" to 8),
            waitTimeCategory = WaitTimeCategory.FAST,
            waitTimeLabel = "< 5 MIN WAIT",
            rating = 4.7,
            reviewCount = 128,
            averagePriceCOP = 16000,
            paymentMethods = listOf("Nequi", "Daviplata", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "d1",
                    name = "Criollo Student Bowl",
                    description = "Rice, red beans, sweet plantains, grilled chicken & fresh garden salad.",
                    priceCOP = 15500,
                    formattedPrice = "$15,500 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "d2",
                    name = "Express Mixed Burrito",
                    description = "Artisanal flour tortilla with seasoned shredded beef, guacamole & pico de gallo.",
                    priceCOP = 17000,
                    formattedPrice = "$17,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "d3",
                    name = "Quinoa & Avocado Bowl",
                    description = "Fresh mixed greens, crispy quinoa, cherry tomatoes & tahini-lime dressing.",
                    priceCOP = 16000,
                    formattedPrice = "$16,000 COP",
                    isVegan = true,
                    isGlutenFree = true
                ),
                MenuItem(
                    id = "d4",
                    name = "Coffee & Baked Empanada Combo",
                    description = "8oz hot Americano coffee with baked spinach & ricotta empanada.",
                    priceCOP = 7500,
                    formattedPrice = "$7,500 COP",
                    isVegan = false,
                    isGlutenFree = false
                )
            )
        ),
        Restaurant(
            id = "wok_express_franco",
            name = "Wok Express - Franco",
            buildingTag = "Franco",
            walkDistancesFromBuilding = mapOf("ML" to 6, "RGD" to 8, "Franco" to 1, "C" to 6, "W" to 10),
            waitTimeCategory = WaitTimeCategory.MODERATE,
            waitTimeLabel = "5-10 MIN WAIT",
            rating = 4.5,
            reviewCount = 94,
            averagePriceCOP = 21000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = false,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "w1",
                    name = "Teriyaki Tofu Stir-Fry",
                    description = "Jasmine rice, stir-fried vegetables, crispy tofu & teriyaki glaze.",
                    priceCOP = 19500,
                    formattedPrice = "$19,500 COP",
                    isVegan = true,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "w2",
                    name = "Crispy Orange Chicken",
                    description = "Tender chicken bites tossed in zesty sweet-and-sour orange sauce.",
                    priceCOP = 22000,
                    formattedPrice = "$22,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                )
            )
        ),
        Restaurant(
            id = "verde_saludable_rgd",
            name = "Verde & Natural - RGD",
            buildingTag = "RGD",
            walkDistancesFromBuilding = mapOf("ML" to 4, "RGD" to 1, "Franco" to 6, "C" to 3, "W" to 7),
            waitTimeCategory = WaitTimeCategory.FAST,
            waitTimeLabel = "< 5 MIN WAIT",
            rating = 4.8,
            reviewCount = 156,
            averagePriceCOP = 14500,
            paymentMethods = listOf("Nequi", "Daviplata", "Cards"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "v1",
                    name = "Mediterranean Chickpea Salad",
                    description = "Kalamata olives, cucumbers, cherry tomatoes, hummus & organic greens.",
                    priceCOP = 14500,
                    formattedPrice = "$14,500 COP",
                    isVegan = true,
                    isGlutenFree = true
                )
            )
        )
    )

    private val _restaurantsFlow = MutableStateFlow(sampleRestaurants)
    val restaurantsFlow: Flow<List<Restaurant>> = _restaurantsFlow.asStateFlow()

    fun getAllRestaurants(): List<Restaurant> = sampleRestaurants

    fun getRestaurantById(id: String): Restaurant? {
        return sampleRestaurants.find { it.id == id } ?: sampleRestaurants.firstOrNull()
    }

    /**
     * Context-aware memory-efficient filter (RAM optimized)
     */
    fun filterRestaurants(criteria: FilterCriteria): List<Restaurant> {
        return sampleRestaurants.filter { restaurant ->
            val walkTime = restaurant.walkDistancesFromBuilding[criteria.selectedBuilding] ?: 15
            val matchesWalkTime = walkTime <= criteria.maxWalkTimeMinutes.toInt()
            
            val matchesBudget = when (criteria.selectedBudget) {
                BudgetRange.CHEAP -> restaurant.averagePriceCOP <= 15000
                BudgetRange.MEDIUM -> restaurant.averagePriceCOP in 10000..25000
                BudgetRange.HIGH -> restaurant.averagePriceCOP >= 20000
            }

            val matchesVegan = !criteria.isVeganSelected || restaurant.isVeganFriendly
            val matchesGlutenFree = !criteria.isGlutenFreeSelected || restaurant.isGlutenFreeFriendly
            val matchesLactoseFree = !criteria.isLactoseFreeSelected || restaurant.isLactoseFreeFriendly

            matchesWalkTime && matchesBudget && matchesVegan && matchesGlutenFree && matchesLactoseFree
        }
    }
}
