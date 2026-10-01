package com.uniandesfood

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.AuthViewModel
import com.uniandesfood.viewmodel.FiltersViewModel
import com.uniandesfood.viewmodel.RestaurantViewModel
import com.uniandesfood.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay

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
            authViewModel = authViewModel,
            filtersViewModel = filtersViewModel,
            restaurantViewModel = restaurantViewModel,
            onLogout = {
                isGuestUser = false
                authViewModel.logout()
            }
        )
    }
}

private const val SCREEN_MAP = 0
private const val SCREEN_EXPLORE = 1
private const val SCREEN_FAVORITES = 2
private const val SCREEN_PROFILE = 3
private const val SCREEN_DETAIL = 4
private const val SCREEN_SCAN_QR = 5
private const val SCREEN_REVIEW = 6

@Composable
fun MainAppContainer(
    authViewModel: AuthViewModel,
    filtersViewModel: FiltersViewModel,
    restaurantViewModel: RestaurantViewModel,
    onLogout: () -> Unit = {}
) {
    var currentScreen by remember { mutableIntStateOf(SCREEN_MAP) }
    var previousTab by remember { mutableIntStateOf(SCREEN_MAP) }
    val reviewViewModel: ReviewViewModel = viewModel()
    val context = LocalContext.current
    var scannedRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    LaunchedEffect(scannedRestaurant) {
        if (scannedRestaurant != null) {
            delay(800)
            currentScreen = SCREEN_REVIEW
        }
    }

    val showBottomBar = currentScreen in listOf(SCREEN_MAP, SCREEN_EXPLORE, SCREEN_FAVORITES, SCREEN_PROFILE)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = CardSurfaceWhite,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(68.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Map Tab
                        BottomNavItem(
                            label = "Map",
                            selected = currentScreen == SCREEN_MAP,
                            iconRes = if (currentScreen == SCREEN_MAP) R.drawable.ic_map_filled else R.drawable.ic_map,
                            onClick = {
                                currentScreen = SCREEN_MAP
                                previousTab = SCREEN_MAP
                            }
                        )

                        // 2. Explore Tab
                        BottomNavItem(
                            label = "Explore",
                            selected = currentScreen == SCREEN_EXPLORE,
                            iconRes = if (currentScreen == SCREEN_EXPLORE) R.drawable.ic_explore_filled else R.drawable.ic_explore,
                            onClick = {
                                currentScreen = SCREEN_EXPLORE
                                previousTab = SCREEN_EXPLORE
                            }
                        )

                        // 3. Center Elevated Action Button: Scan QR
                        Box(
                            modifier = Modifier
                                .offset(y = (-10).dp)
                                .size(56.dp)
                                .shadow(elevation = 6.dp, shape = CircleShape)
                                .clip(CircleShape)
                                .background(UniandesAmber)
                                .clickable {
                                    previousTab = currentScreen
                                    currentScreen = SCREEN_SCAN_QR
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_qr_scan),
                                contentDescription = "Scan QR",
                                tint = ShadowGrey,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // 4. Favorites Tab
                        BottomNavItem(
                            label = "Favorites",
                            selected = currentScreen == SCREEN_FAVORITES,
                            iconRes = if (currentScreen == SCREEN_FAVORITES) R.drawable.ic_favorites_filled else R.drawable.ic_favorites,
                            onClick = {
                                currentScreen = SCREEN_FAVORITES
                                previousTab = SCREEN_FAVORITES
                            }
                        )

                        // 5. Profile Tab
                        BottomNavItem(
                            label = "Profile",
                            selected = currentScreen == SCREEN_PROFILE,
                            iconRes = if (currentScreen == SCREEN_PROFILE) R.drawable.ic_profile_filled else R.drawable.ic_profile,
                            onClick = {
                                currentScreen = SCREEN_PROFILE
                                previousTab = SCREEN_PROFILE
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    SCREEN_MAP -> MapScreen(
                        viewModel = restaurantViewModel,
                        onViewRestaurant = { restaurant ->
                            restaurantViewModel.selectRestaurant(restaurant.id)
                            previousTab = SCREEN_MAP
                            currentScreen = SCREEN_DETAIL
                        },
                        onOpenFilters = {
                            previousTab = SCREEN_MAP
                            currentScreen = SCREEN_EXPLORE
                        }
                    )

                    SCREEN_EXPLORE -> ExploreScreen(
                        viewModel = restaurantViewModel,
                        onApplyFilters = {
                            previousTab = SCREEN_EXPLORE
                            currentScreen = SCREEN_DETAIL
                        }
                    )

                    SCREEN_FAVORITES -> FavoritesScreen(
                        viewModel = restaurantViewModel,
                        onSelectRestaurant = { restaurant ->
                            restaurantViewModel.selectRestaurant(restaurant.id)
                            previousTab = SCREEN_FAVORITES
                            currentScreen = SCREEN_DETAIL
                        },
                        onExplore = {
                            currentScreen = SCREEN_EXPLORE
                            previousTab = SCREEN_EXPLORE
                        }
                    )

                    SCREEN_PROFILE -> ProfileScreen(
                        authViewModel = authViewModel,
                        restaurantViewModel = restaurantViewModel,
                        onLogout = onLogout
                    )

                    SCREEN_DETAIL -> RestaurantDetailScreen(
                        viewModel = restaurantViewModel,
                        onBack = { currentScreen = previousTab },
                        onScanQR = {
                            previousTab = SCREEN_DETAIL
                            currentScreen = SCREEN_SCAN_QR
                        }
                    )

                    SCREEN_SCAN_QR -> ScanQrScreen(
                        onBack = { currentScreen = previousTab },
                        isValidQr = { restaurantViewModel.findById(it) != null },
                        onQrScanned = { value -> scannedRestaurant = restaurantViewModel.findById(value) }
                    )

                    SCREEN_REVIEW -> scannedRestaurant?.let { r ->
                        ReviewScreen(
                            restaurantName = r.name,
                            onSubmit = { rating ->
                                reviewViewModel.submitReview(r.id, rating)
                                Toast.makeText(context, "¡Gracias por calificar $rating estrellas!", Toast.LENGTH_SHORT).show()
                                scannedRestaurant = null
                                currentScreen = SCREEN_DETAIL
                            },
                            onCancel = {
                                reviewViewModel.cancelReview(r.id)
                                scannedRestaurant = null
                                currentScreen = SCREEN_DETAIL
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (selected) UniandesAmber else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (selected) ShadowGrey else TextMuted
        )
    }
}