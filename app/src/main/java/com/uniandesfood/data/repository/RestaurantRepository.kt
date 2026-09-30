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
            id = "el_toro_rgd",
            name = "El Toro - RGD",
            category = "Executive Lunch",
            buildingTag = "RGD",
            walkDistancesFromBuilding = mapOf("RGD" to 1, "ML" to 4, "Franco" to 6, "C" to 3, "W" to 7, "SD" to 5),
            waitTimeCategory = WaitTimeCategory.FAST,
            waitTimeLabel = "< 5 MIN WAIT",
            rating = 4.8,
            reviewCount = 184,
            averagePriceCOP = 17500,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "et1",
                    name = "De la Casa (Carne Sudada)",
                    description = "Bowl de carne sudada casera, arroz con pasta, plátano dulce, papa criolla y limonada natural.",
                    priceCOP = 17500,
                    formattedPrice = "$17,500 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "et2",
                    name = "Bowl Vegetariano Fresco",
                    description = "Bowl con selección de verduras totalmente frescas, aderezo especial de la casa y limonada natural.",
                    priceCOP = 15500,
                    formattedPrice = "$15,500 COP",
                    isVegan = true,
                    isGlutenFree = true
                ),
                MenuItem(
                    id = "et3",
                    name = "Bandeja Paisa Criolla",
                    description = "Arroz blanco, frijoles caseros, chicharrón crocante, plátano maduro y limonada natural.",
                    priceCOP = 17500,
                    formattedPrice = "$17,500 COP",
                    isVegan = false,
                    isGlutenFree = true
                )
            )
        ),
        Restaurant(
            id = "one_burrito_ml",
            name = "One Burrito - ML",
            category = "Fast Food",
            buildingTag = "ML",
            walkDistancesFromBuilding = mapOf("ML" to 1, "RGD" to 5, "Franco" to 7, "C" to 4, "W" to 8, "SD" to 8),
            waitTimeCategory = WaitTimeCategory.MODERATE,
            waitTimeLabel = "5-10 MIN WAIT",
            rating = 4.7,
            reviewCount = 210,
            averagePriceCOP = 25000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "ob1",
                    name = "Burrito Personalizado",
                    description = "Tortilla de harina con arroz, frijol, proteína al gusto o verduras salteadas, pico de gallo, queso, guacamole y salsas.",
                    priceCOP = 25000,
                    formattedPrice = "$25,000 COP",
                    isVegan = true,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "ob2",
                    name = "Quesadilla Queso & Proteína",
                    description = "Quesadilla dorada con queso fundido, proteína o vegetales, 3 toppings a elección y salsas mexicanas.",
                    priceCOP = 24000,
                    formattedPrice = "$24,000 COP",
                    isVegan = true,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "ob3",
                    name = "Sopa de Tortilla / Birria",
                    description = "Sopa tradicional mexicana de tortilla o birria con proteína, aguacate, queso y toppings a elección.",
                    priceCOP = 22000,
                    formattedPrice = "$22,000 COP",
                    isVegan = true,
                    isGlutenFree = true
                )
            )
        ),
        Restaurant(
            id = "one_burrito_rgd",
            name = "One Burrito - RGD",
            category = "Fast Food",
            buildingTag = "RGD",
            walkDistancesFromBuilding = mapOf("RGD" to 1, "ML" to 4, "Franco" to 6, "C" to 3, "W" to 7, "SD" to 5),
            waitTimeCategory = WaitTimeCategory.MODERATE,
            waitTimeLabel = "5-10 MIN WAIT",
            rating = 4.7,
            reviewCount = 165,
            averagePriceCOP = 25000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "obr1",
                    name = "Burrito Personalizado",
                    description = "Tortilla artesanal con arroz, frijol, proteínas seleccionadas o verduras, pico de gallo, guacamole y sour cream.",
                    priceCOP = 25000,
                    formattedPrice = "$25,000 COP",
                    isVegan = true,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "obr2",
                    name = "Quesadilla Especial",
                    description = "Quesadilla con queso derretido, proteína o vegetales, maíz, pico de gallo y salsas de la casa.",
                    priceCOP = 24000,
                    formattedPrice = "$24,000 COP",
                    isVegan = true,
                    isGlutenFree = false
                )
            )
        ),
        Restaurant(
            id = "burger_play_rgd",
            name = "Burger Play - RGD",
            category = "Fast Food",
            buildingTag = "RGD",
            walkDistancesFromBuilding = mapOf("RGD" to 1, "ML" to 4, "Franco" to 6, "C" to 3, "W" to 7, "SD" to 5),
            waitTimeCategory = WaitTimeCategory.LONG,
            waitTimeLabel = "> 10 MIN WAIT",
            rating = 4.6,
            reviewCount = 142,
            averagePriceCOP = 22000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = false,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = false,
            menu = listOf(
                MenuItem(
                    id = "bp1",
                    name = "Hamburguesa Súper Play",
                    description = "Carne artesanal, pollo desmechado, tocineta crocante, tomate, lechuga, cebolla y bebida (limonada o té).",
                    priceCOP = 22000,
                    formattedPrice = "$22,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "bp2",
                    name = "Bacon Burger Clásica",
                    description = "Hamburguesa con carne a la parrilla, tocineta crocante, queso, vegetales frescos y bebida.",
                    priceCOP = 18000,
                    formattedPrice = "$18,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "bp3",
                    name = "Mazorcada Mixta Especial",
                    description = "Base de maíz tierno con carne de res, pollo, tocineta, papa ripio, queso fundido y bebida.",
                    priceCOP = 20000,
                    formattedPrice = "$20,000 COP",
                    isVegan = false,
                    isGlutenFree = true
                )
            )
        ),
        Restaurant(
            id = "burger_play_sd",
            name = "Burger Play - SD",
            category = "Fast Food",
            buildingTag = "SD",
            walkDistancesFromBuilding = mapOf("SD" to 1, "W" to 3, "Franco" to 4, "C" to 5, "RGD" to 6, "ML" to 7),
            waitTimeCategory = WaitTimeCategory.LONG,
            waitTimeLabel = "> 10 MIN WAIT",
            rating = 4.5,
            reviewCount = 98,
            averagePriceCOP = 22000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = false,
            isGlutenFreeFriendly = true,
            isLactoseFreeFriendly = false,
            menu = listOf(
                MenuItem(
                    id = "bps1",
                    name = "Hamburguesa Súper Play",
                    description = "Carne artesanal, pollo, tocineta, tomate, lechuga, cebolla y bebida incluida.",
                    priceCOP = 22000,
                    formattedPrice = "$22,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "bps2",
                    name = "Mazorcada Mixta Especial",
                    description = "Maíz tierno desgranado con carnes mixtas, papa ripio y queso fundido.",
                    priceCOP = 20000,
                    formattedPrice = "$20,000 COP",
                    isVegan = false,
                    isGlutenFree = true
                )
            )
        ),
        Restaurant(
            id = "la_cabra_sanduchera_rgd",
            name = "La Cabra Sanduchera - RGD",
            category = "Fast Food",
            buildingTag = "RGD",
            walkDistancesFromBuilding = mapOf("RGD" to 1, "ML" to 4, "Franco" to 6, "C" to 3, "W" to 7, "SD" to 5),
            waitTimeCategory = WaitTimeCategory.FAST,
            waitTimeLabel = "< 5 MIN WAIT",
            rating = 4.8,
            reviewCount = 175,
            averagePriceCOP = 25000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = true,
            isGlutenFreeFriendly = false,
            isLactoseFreeFriendly = true,
            menu = listOf(
                MenuItem(
                    id = "cs1",
                    name = "Sándwich de Pollo Especial",
                    description = "Pechuga de pollo a la plancha, lechuga fresca y salsa de la casa en pan artesanal.",
                    priceCOP = 23000,
                    formattedPrice = "$23,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "cs2",
                    name = "Sándwich Jamón Serrano & Rúgula",
                    description = "Láminas de jamón serrano, salsa pomodoro, queso y rúgula fresca.",
                    priceCOP = 21000,
                    formattedPrice = "$21,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "cs3",
                    name = "Choripán Artesanal",
                    description = "Chorizo artesanal a la parrilla con chimichurri y salsas en pan baguette.",
                    priceCOP = 15000,
                    formattedPrice = "$15,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "cs4",
                    name = "Sándwich Vegano Champiñones & Hummus",
                    description = "Champiñones salteados, rúgula, tomate fresco, cebolla, aguacate y hummus artesanal.",
                    priceCOP = 20000,
                    formattedPrice = "$20,000 COP",
                    isVegan = true,
                    isGlutenFree = false
                )
            )
        ),
        Restaurant(
            id = "la_liebre_franco",
            name = "La Liebre - Franco",
            category = "Fast Food",
            buildingTag = "Franco",
            walkDistancesFromBuilding = mapOf("Franco" to 1, "ML" to 6, "RGD" to 7, "C" to 5, "W" to 9, "SD" to 4),
            waitTimeCategory = WaitTimeCategory.LONG,
            waitTimeLabel = "> 10 MIN WAIT",
            rating = 4.9,
            reviewCount = 230,
            averagePriceCOP = 30000,
            paymentMethods = listOf("Nequi", "Cards", "Cash"),
            isVeganFriendly = false,
            isGlutenFreeFriendly = false,
            isLactoseFreeFriendly = false,
            menu = listOf(
                MenuItem(
                    id = "ll1",
                    name = "Classic Cheeseburger",
                    description = "Carne madurada a la parrilla, queso cheddar americano fundido y cebolla caramelizada.",
                    priceCOP = 30000,
                    formattedPrice = "$30,000 COP",
                    isVegan = false,
                    isGlutenFree = false
                ),
                MenuItem(
                    id = "ll2",
                    name = "Double Smash Burger & Blue Cheese",
                    description = "Doble carne smash crocante, salsa artesanal de queso azul y cebolla caramelizada.",
                    priceCOP = 35000,
                    formattedPrice = "$35,000 COP",
                    isVegan = false,
                    isGlutenFree = false
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
            val matchesPayment = criteria.selectedPayments.isEmpty() || restaurant.paymentMethods.any { it in criteria.selectedPayments }

            matchesWalkTime && matchesBudget && matchesVegan && matchesGlutenFree && matchesLactoseFree && matchesPayment
        }.sortedBy { it.walkDistancesFromBuilding[criteria.selectedBuilding] ?: 99 }
    }
}
