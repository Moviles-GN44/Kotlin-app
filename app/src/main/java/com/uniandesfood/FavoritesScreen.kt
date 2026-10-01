package com.uniandesfood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: RestaurantViewModel? = null,
    onSelectRestaurant: (Restaurant) -> Unit = {},
    onExplore: () -> Unit = {}
) {
    val allRestaurants = viewModel?.restaurants?.collectAsState()?.value ?: emptyList()
    val favoriteIds = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val favoriteRestaurants = allRestaurants.filter { favoriteIds.contains(it.id) }
    val currentBuilding = viewModel?.currentBuilding ?: "ML"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Restaurantes Favoritos",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ShadowGrey
                        )
                        if (favoriteRestaurants.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = UniandesAmber.copy(alpha = 0.2f),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "${favoriteRestaurants.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ShadowGrey,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurfaceWhite)
            )
        },
        containerColor = BackgroundOffWhite
    ) { paddingValues ->
        if (favoriteRestaurants.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(UniandesAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = UniandesAmber,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "No tienes favoritos aún",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ShadowGrey,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Toca el corazón en cualquier restaurante en el Mapa o en Explorar para guardarlo aquí y acceder rápidamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onExplore,
                            colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text(
                                text = "Explorar Campus",
                                style = MaterialTheme.typography.labelLarge,
                                color = ShadowGrey
                            )
                        }
                    }
                }
            }
        } else {
            // Favorites List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteRestaurants, key = { it.id }) { restaurant ->
                    val walkMin = restaurant.walkDistancesFromBuilding[currentBuilding] ?: 2
                    val ratingDisplay = if (restaurant.reviewCount == 0) {
                        "N/A ★ (0)"
                    } else {
                        "${restaurant.rating} ★ (${restaurant.reviewCount})"
                    }
                    val statusColor = when (restaurant.waitTimeCategory) {
                        WaitTimeCategory.FAST -> StatusFastGreen
                        WaitTimeCategory.MODERATE -> StatusModerateAmber
                        WaitTimeCategory.LONG -> StatusLongRed
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.selectRestaurant(restaurant.id)
                                onSelectRestaurant(restaurant)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = restaurant.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ShadowGrey,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${restaurant.category} • Edificio ${restaurant.buildingTag}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Rating
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_rating_filled),
                                            contentDescription = "Rating",
                                            tint = UniandesAmber,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = ratingDisplay,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = UniandesAmber
                                        )
                                    }

                                    // Walk Time
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_walk_time),
                                            contentDescription = "Walk Time",
                                            tint = TextMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "$walkMin min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }

                                    // Wait status
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = restaurant.waitTimeLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Heart button
                            IconButton(
                                onClick = { viewModel?.toggleFavorite(restaurant.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Eliminar de favoritos",
                                    tint = StatusLongRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
