package com.uniandesfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.AuthViewModel
import com.uniandesfood.viewmodel.FiltersViewModel
import com.uniandesfood.viewmodel.RestaurantViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val filtersViewModel: FiltersViewModel by viewModels()
    private val restaurantViewModel: RestaurantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniandesFoodTheme {
                MainAppHost(
                    authViewModel = authViewModel,
                    filtersViewModel = filtersViewModel,
                    restaurantViewModel = restaurantViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppHost(
    authViewModel: AuthViewModel,
    filtersViewModel: FiltersViewModel,
    restaurantViewModel: RestaurantViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    var isGuestUser by remember { mutableStateOf(false) }

    if (!authState.isAuthenticated && !isGuestUser) {
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = { isGuestUser = true }
        )
    } else {
        MainAppContainer(
            filtersViewModel = filtersViewModel,
            restaurantViewModel = restaurantViewModel
        )
    }
}

@Composable
fun MainAppContainer(
    filtersViewModel: FiltersViewModel,
    restaurantViewModel: RestaurantViewModel
) {
    var currentScreen by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardSurfaceWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == 0,
                    onClick = { currentScreen = 0 },
                    label = { Text("Filters", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (currentScreen == 0) R.drawable.ic_explore_filled else R.drawable.ic_explore
                            ),
                            contentDescription = "Filters View",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = ShadowGrey,
                        selectedIconColor = ShadowGrey,
                        indicatorColor = UniandesAmber
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == 1,
                    onClick = { currentScreen = 1 },
                    label = { Text("Detail", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (currentScreen == 1) R.drawable.ic_dish_photo else R.drawable.ic_dish_photo
                            ),
                            contentDescription = "Restaurant Detail",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = ShadowGrey,
                        selectedIconColor = ShadowGrey,
                        indicatorColor = UniandesAmber
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == 2,
                    onClick = { currentScreen = 2 },
                    label = { Text("Explore", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (currentScreen == 2) R.drawable.ic_explore_filled else R.drawable.ic_explore
                            ),
                            contentDescription = "Explore",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = ShadowGrey,
                        selectedIconColor = ShadowGrey,
                        indicatorColor = UniandesAmber
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == 3,
                    onClick = { currentScreen = 3 },
                    label = { Text("Scan QR", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_scan),
                            contentDescription = "Scan QR",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = ShadowGrey,
                        selectedIconColor = ShadowGrey,
                        indicatorColor = UniandesAmber
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    0 -> FiltersScreen(
                        viewModel = filtersViewModel,
                        onApplyFilters = {
                            val criteria = filtersViewModel.criteria.value
                            restaurantViewModel.filterRestaurants(criteria)
                            currentScreen = 1 // Navigate to Detail
                        }
                    )
                    1 -> RestaurantDetailScreen(
                        viewModel = restaurantViewModel,
                        onBack = { currentScreen = 0 },
                        onScanQR = {
                            currentScreen = 3 // Navigate to Scan QR
                        }
                    )
                    2 -> ExploreScreen(
                        onApplyFilters = {
                            currentScreen = 1
                        }
                    )
                    3 -> ScanQrScreen(
                        onBack = { currentScreen = 1 }
                    )
                }
            }
        }
    }
}