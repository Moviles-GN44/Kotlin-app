package com.uniandesfood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.MenuItem
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    viewModel: RestaurantViewModel? = null,
    onBack: () -> Unit = {},
    onScanQR: () -> Unit = {}
) {
    val allMatchingRestaurants = viewModel?.restaurants?.collectAsState()?.value ?: emptyList()
    val selectedRestaurant = viewModel?.selectedRestaurant?.collectAsState()?.value

    val currentBuilding = viewModel?.currentBuilding ?: (selectedRestaurant?.buildingTag ?: "ML")
    val walkMinutes = selectedRestaurant?.walkDistancesFromBuilding?.get(currentBuilding) ?: 2
    val restaurantName = selectedRestaurant?.name ?: ""
    val waitTimeLabel = selectedRestaurant?.waitTimeLabel ?: "< 5 MIN WAIT"
    val waitTimeCategory = selectedRestaurant?.waitTimeCategory ?: WaitTimeCategory.FAST
    val ratingText = "${selectedRestaurant?.rating ?: 4.7} (${selectedRestaurant?.reviewCount ?: 128} verified student reviews)"
    val walkTimeText = "$walkMinutes min walk from $currentBuilding Building"
    val paymentsText = "Accepts: ${(selectedRestaurant?.paymentMethods ?: listOf("Nequi", "Daviplata", "Cards", "Cash")).joinToString(", ")}"

    val menuDishes = selectedRestaurant?.menu ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Campus Dining Options",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShadowGrey
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurfaceWhite)
            )
        },
        bottomBar = {
            Surface(
                color = CardSurfaceWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onScanQR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_qr_review),
                            contentDescription = "Scan QR",
                            tint = ShadowGrey,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Scan In-Store QR & Review",
                            style = MaterialTheme.typography.labelLarge,
                            color = ShadowGrey
                        )
                    }
                }
            }
        },
        containerColor = BackgroundOffWhite
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Horizontal Restaurant Selector Row
            if (allMatchingRestaurants.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available Restaurants (${allMatchingRestaurants.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = ShadowGrey
                            )
                            Text(
                                text = "Tap to view menu",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allMatchingRestaurants) { restaurant ->
                                val isSelected = restaurant.id == selectedRestaurant?.id
                                val walkMin = restaurant.walkDistancesFromBuilding[currentBuilding] ?: 2
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) UniandesAmber.copy(alpha = 0.15f) else CardSurfaceWhite
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) UniandesAmber else BorderLight
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .width(170.dp)
                                        .clickable { viewModel?.selectRestaurant(restaurant.id) }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = restaurant.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ShadowGrey,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_rating_filled),
                                                contentDescription = "Rating",
                                                tint = UniandesAmber,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "${restaurant.rating}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = UniandesAmber
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$walkMin min",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = restaurant.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusFastGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedRestaurant == null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Restaurants Found",
                                style = MaterialTheme.typography.headlineSmall,
                                color = ShadowGrey
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No dining options match this category or filter combination. Try selecting a different category or adjusting walking distance.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                // Restaurant Header Card
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = restaurantName,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp),
                                    color = ShadowGrey
                                )
                                // Traffic-light Wait Time Badge
                                val statusColor = when (waitTimeCategory) {
                                    WaitTimeCategory.FAST -> StatusFastGreen
                                    WaitTimeCategory.MODERATE -> StatusModerateAmber
                                    WaitTimeCategory.LONG -> StatusLongRed
                                }
                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_wait_fast_filled),
                                            contentDescription = "Wait time queue",
                                            tint = statusColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = waitTimeLabel,
                                            color = statusColor,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_rating_filled),
                                    contentDescription = "Rating",
                                    tint = UniandesAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = ratingText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = UniandesAmber
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_verified),
                                    contentDescription = "Verified",
                                    tint = MintEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_walk_time),
                                    contentDescription = "Walk Time",
                                    tint = UniandesAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = walkTimeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_payment),
                                    contentDescription = "Payment Methods",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = paymentsText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // Menu Section Header
                item {
                    Text(
                        text = "Available Menu Dishes (${menuDishes.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ShadowGrey
                    )
                }

                // Menu List
                items(menuDishes) { dish ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ShadowGrey
                                )

                                // Clean Dietary Badges Row with vector icons
                                if (dish.isVegan || dish.isGlutenFree) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (dish.isVegan) {
                                            Surface(
                                                color = MintEmerald.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_vegan_filled),
                                                        contentDescription = "Vegan",
                                                        tint = MintEmerald,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = "VEGAN",
                                                        color = MintEmerald,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                        if (dish.isGlutenFree) {
                                            Surface(
                                                color = TagAllergenPurple.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_allergen_filled),
                                                        contentDescription = "Gluten Free",
                                                        tint = TagAllergenPurple,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = "GLUTEN-FREE",
                                                        color = TagAllergenPurple,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dish.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    maxLines = 2
                                )
                            }

                            Text(
                                text = dish.formattedPrice,
                                style = MaterialTheme.typography.titleLarge,
                                color = UniandesAmber
                            )
                        }
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantDetailScreenPreview() {
    UniandesFoodTheme {
        RestaurantDetailScreen()
    }
}
