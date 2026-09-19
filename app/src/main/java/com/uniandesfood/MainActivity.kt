package com.uniandesfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniandesFoodTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
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
                    label = { Text("Filters View", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (currentScreen == 0) R.drawable.ic_explore_filled else R.drawable.ic_explore
                            ),
                            contentDescription = "Filters View",
                            modifier = Modifier.size(24.dp)
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
                    label = { Text("Restaurant Detail", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (currentScreen == 1) R.drawable.ic_dish_photo else R.drawable.ic_dish_photo
                            ),
                            contentDescription = "Restaurant Detail",
                            modifier = Modifier.size(24.dp)
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
            when (currentScreen) {
                0 -> FiltersScreen()
                1 -> RestaurantDetailScreen()
            }
        }
    }
}