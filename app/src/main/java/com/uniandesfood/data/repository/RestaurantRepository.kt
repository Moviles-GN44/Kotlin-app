package com.uniandesfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.uniandesfood.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val sampleRestaurants = listOf(
        Restaurant(
            id = "one_burrito_ml",
            name = "One Burrito - ML",
            category = "Fast Food",
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
            category = "Executive Lunch",
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
            category = "Healthy",
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
                ),
                MenuItem(
                    id = "v2",
                    name = "Green Detox Smoothie Bowl",
                    description = "Spinach, banana, spirulina, almond butter, chia seeds and fresh berries.",
                    priceCOP = 13000,
                    formattedPrice = "$13,000 COP",
                    isVegan = true,
                    isGlutenFree = true
                )
            )
        ),
        Restaurant(
            id = "crepes_waffles_c",
            name = "Crepes & Waffles - C",
            category = "Desserts",
            buildingTag = "C",
            walkDistancesFromBuilding = mapOf("ML" to 3, "RGD" to 4, "Franco" to 5, "C" to 1, "W" to 6),
            waitTimeCategory = WaitTimeCategory.MODERATE,
            waitTimeLabel = "5-10 MIN WAIT",
            rating = 4.9,
            reviewCount = 310,
            averagePriceCOP = 18000,
            paymentMethods = listOf("Nequi", "Daviplata", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = false,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "c1",
                    name = "Artisanal Belgian Waffle Nutella",
                    description = "Crispy golden waffle loaded with warm Nutella, fresh strawberries and whipped cream.",
                    priceCOP = 16500,
                    formattedPrice = "$16,500 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "c2",
                    name = "Arequipe & Banana Crepe",
                    description = "Delicate warm crepe folded with traditional Colombian arequipe and sliced sweet bananas.",
                    priceCOP = 14000,
                    formattedPrice = "$14,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "c3",
                    name = "Gelato Passionfruit Coupe",
                    description = "Double scoop of artisanal passionfruit and dark chocolate gelato.",
                    priceCOP = 11500,
                    formattedPrice = "$11,500 COP",
                    isVegan = true,
                    isGlutenFree = true
                )
            )
        ),
        Restaurant(
            id = "juan_valdez_w",
            name = "Café Juan Valdez - W",
            category = "Café",
            buildingTag = "W",
            walkDistancesFromBuilding = mapOf("ML" to 5, "RGD" to 6, "Franco" to 8, "C" to 4, "W" to 1),
            waitTimeCategory = WaitTimeCategory.FAST,
            waitTimeLabel = "< 5 MIN WAIT",
            rating = 4.6,
            reviewCount = 215,
            averagePriceCOP = 9500,
            paymentMethods = listOf("Nequi", "Daviplata", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "j1",
                    name = "Nevado de Arequipe 16oz",
                    description = "Blended cold specialty coffee with Colombian arequipe, milk and chantilly cream.",
                    priceCOP = 13500,
                    formattedPrice = "$13,500 COP",
                    isVegan = false,
                    isGlutenFree = true
                ),
                MenuItem(
                    id = "j2",
                    name = "Tinto Campesino & Pandebono",
                    description = "100% premium Colombian drip coffee infused with panela and cinnamon + warm pandebono.",
                    priceCOP = 7500,
                    formattedPrice = "$7,500 COP",
                    isVegan = false,
                    isGlutenFree = true
                ),
                MenuItem(
                    id = "j3",
                    name = "Almond Milk Flat White",
                    description = "Double shot of premium espresso with steamed organic almond milk.",
                    priceCOP = 8900,
                    formattedPrice = "$8,900 COP",
                    isVegan = true,
                    isGlutenFree = true
                )
            )
        )
    )

    private val _restaurantsFlow = MutableStateFlow(sampleRestaurants)
    val restaurantsFlow: Flow<List<Restaurant>> = _restaurantsFlow.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            seedInitialData()
        }
        listenToFirestoreRestaurants()
    }

    private fun listenToFirestoreRestaurants() {
        try {
            firestore.collection("restaurants")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.data?.let { Restaurant.fromMap(it) }
                        }
                        if (list.isNotEmpty()) {
                            _restaurantsFlow.value = list
                        }
                    } else if (snapshot != null && snapshot.isEmpty) {
                        // Automatically seed initial data to Firestore if empty
                        coroutineScope.launch {
                            seedInitialData()
                        }
                    }
                }
        } catch (_: Exception) {
            // Fallback to local memory cache
        }
    }

    suspend fun seedInitialData() {
        try {
            val batch = firestore.batch()
            for (restaurant in sampleRestaurants) {
                val docRef = firestore.collection("restaurants").document(restaurant.id)
                batch.set(docRef, restaurant.toMap(), SetOptions.merge())
            }
            batch.commit().await()
        } catch (_: Exception) {
            // Non-fatal if offline
        }
    }

    fun getAllRestaurants(): List<Restaurant> = _restaurantsFlow.value

    fun getRestaurantById(id: String): Restaurant? {
        val currentList = _restaurantsFlow.value
        return currentList.find { it.id == id } ?: currentList.firstOrNull()
    }

    /**
     * Filter by category (e.g., "Executive Lunch", "Fast Food", "Healthy", "Desserts", "Café")
     */
    fun filterByCategory(category: String): List<Restaurant> {
        return _restaurantsFlow.value.filter {
            it.category.equals(category, ignoreCase = true)
        }
    }

    /**
     * Context-aware memory-efficient filter (RAM optimized)
     * Filters and sorts by shortest walking distance to the chosen campus building.
     */
    fun filterRestaurants(criteria: FilterCriteria): List<Restaurant> {
        return _restaurantsFlow.value.filter { restaurant ->
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
        }.sortedBy { it.walkDistancesFromBuilding[criteria.selectedBuilding] ?: 99 }
    }
}
