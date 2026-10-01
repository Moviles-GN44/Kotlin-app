package com.uniandesfood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

private data class CampusBuildingInfo(
    val id: String,
    val name: String,
    val subtitle: String,
    val normX: Float,
    val normY: Float,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: RestaurantViewModel? = null,
    onViewRestaurant: (Restaurant) -> Unit = {},
    onOpenFilters: () -> Unit = {}
) {
    val restaurants = viewModel?.restaurants?.collectAsState()?.value ?: emptyList()
    val selectedRestaurant = viewModel?.selectedRestaurant?.collectAsState()?.value ?: restaurants.firstOrNull()
    val favorites = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val currentBuilding = viewModel?.currentBuilding ?: "ML"

    // The 6 authenticated Campus Buildings from GPS coordinates
    val buildings = remember {
        listOf(
            CampusBuildingInfo("SD", "Edificio SD", "Salones", 0.46f, 0.26f, Color(0xFF7C3AED)),
            CampusBuildingInfo("ML", "Edificio ML", "Ingeniería", 0.66f, 0.40f, Color(0xFF2563EB)),
            CampusBuildingInfo("W", "Edificio W", "Diseño", 0.80f, 0.52f, Color(0xFF0284C7)),
            CampusBuildingInfo("RGD", "Edificio RGD", "Deportes", 0.42f, 0.52f, Color(0xFFD97706)),
            CampusBuildingInfo("Franco", "Edificio Franco", "Leyes", 0.42f, 0.64f, Color(0xFFE11D48)),
            CampusBuildingInfo("C", "Edificio C", "Ciencias", 0.70f, 0.67f, Color(0xFF059669))
        )
    }

    // Positions for the 7 authenticated restaurants calibrated to campus geometry
    val restaurantPositions = remember {
        mapOf(
            "burger_play_sd" to Triple(0.64f, 0.22f, "🍔"),
            "one_burrito_ml" to Triple(0.74f, 0.33f, "🌯"),
            "la_cabra_sanduchera_rgd" to Triple(0.18f, 0.42f, "🥪"),
            "one_burrito_rgd" to Triple(0.14f, 0.48f, "🌯"),
            "el_toro_rgd" to Triple(0.22f, 0.55f, "🥩"),
            "burger_play_rgd" to Triple(0.14f, 0.60f, "🍔"),
            "la_liebre_franco" to Triple(0.20f, 0.67f, "🐇")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // 1. Campus Map Canvas & Layout
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val mapWidthDp = maxWidth
            val mapHeightDp = maxHeight

            // Canvas: Ground, Pathways, Plazas, Gardens
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Campus Terrain base
                drawRect(
                    color = Color(0xFFF8FAFC),
                    size = size
                )

                // Plazoleta Central (connecting ML, RGD, SD)
                drawRoundRect(
                    color = Color(0xFFF1F5F9),
                    topLeft = Offset(w * 0.30f, h * 0.32f),
                    size = Size(w * 0.48f, h * 0.26f),
                    cornerRadius = CornerRadius(36f, 36f)
                )

                // Plazoleta RGD & Food Terrace
                drawRoundRect(
                    color = Color(0xFFF1F5F9),
                    topLeft = Offset(w * 0.08f, h * 0.42f),
                    size = Size(w * 0.34f, h * 0.24f),
                    cornerRadius = CornerRadius(28f, 28f)
                )

                // Central Campus Green (Jardín Central / El Bobo)
                drawRoundRect(
                    color = Color(0xFFDCFCE7),
                    topLeft = Offset(w * 0.38f, h * 0.32f),
                    size = Size(w * 0.24f, h * 0.16f),
                    cornerRadius = CornerRadius(32f, 32f)
                )

                // South Green Zone (Jardines Franco / Bosque)
                drawRoundRect(
                    color = Color(0xFFE2F5E9),
                    topLeft = Offset(w * 0.48f, h * 0.58f),
                    size = Size(w * 0.18f, h * 0.12f),
                    cornerRadius = CornerRadius(24f, 24f)
                )

                // North Lawn (Around SD)
                drawRoundRect(
                    color = Color(0xFFE8F5E9),
                    topLeft = Offset(w * 0.34f, h * 0.20f),
                    size = Size(w * 0.28f, h * 0.08f),
                    cornerRadius = CornerRadius(20f, 20f)
                )

                // Campus Trees Accent (small lush green circles)
                drawCircle(color = Color(0xFF86EFAC), radius = 9f, center = Offset(w * 0.43f, h * 0.35f))
                drawCircle(color = Color(0xFF86EFAC), radius = 11f, center = Offset(w * 0.52f, h * 0.38f))
                drawCircle(color = Color(0xFF86EFAC), radius = 10f, center = Offset(w * 0.54f, h * 0.63f))
                drawCircle(color = Color(0xFF86EFAC), radius = 9f, center = Offset(w * 0.58f, h * 0.61f))

                // Paved Walkways connecting all 6 buildings
                // Pathway: SD -> ML
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.46f, h * 0.26f),
                    end = Offset(w * 0.66f, h * 0.40f),
                    strokeWidth = 14f
                )
                // Pathway: Central -> RGD
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.66f, h * 0.40f),
                    end = Offset(w * 0.42f, h * 0.52f),
                    strokeWidth = 16f
                )
                // Pathway: ML -> W
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.66f, h * 0.40f),
                    end = Offset(w * 0.80f, h * 0.52f),
                    strokeWidth = 14f
                )
                // Pathway: RGD -> Franco
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.42f, h * 0.52f),
                    end = Offset(w * 0.42f, h * 0.64f),
                    strokeWidth = 14f
                )
                // Pathway: Franco -> Edificio C
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.42f, h * 0.64f),
                    end = Offset(w * 0.70f, h * 0.67f),
                    strokeWidth = 14f
                )
                // Pathway: C -> W
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.70f, h * 0.67f),
                    end = Offset(w * 0.80f, h * 0.52f),
                    strokeWidth = 12f
                )
                // Pathway: RGD -> Food Terrace West
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(w * 0.42f, h * 0.52f),
                    end = Offset(w * 0.16f, h * 0.52f),
                    strokeWidth = 14f
                )
            }

            // 6 Campus Building Footprint Cards
            buildings.forEach { bldg ->
                val isOrigin = bldg.id == currentBuilding
                Box(
                    modifier = Modifier
                        .offset(
                            x = (mapWidthDp * bldg.normX) - 48.dp,
                            y = (mapHeightDp * bldg.normY) - 18.dp
                        )
                        .shadow(elevation = if (isOrigin) 6.dp else 2.dp, shape = RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(
                            width = if (isOrigin) 2.dp else 1.dp,
                            color = if (isOrigin) UniandesAmber else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            viewModel?.updateCurrentBuilding(bldg.id)
                        }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Colored accent indicator
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(bldg.accentColor, RoundedCornerShape(2.dp))
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = bldg.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                if (isOrigin) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "📍",
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = bldg.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }
            }

            // 7 Interactive Restaurant Pins
            restaurants.forEach { rest ->
                val posInfo = restaurantPositions[rest.id] ?: Triple(0.5f, 0.5f, "🍽️")
                val isSelected = rest.id == selectedRestaurant?.id
                val shortName = when (rest.id) {
                    "one_burrito_ml" -> "One Burrito (ML)"
                    "el_toro_rgd" -> "El Toro (RGD)"
                    "one_burrito_rgd" -> "One Burrito (RGD)"
                    "burger_play_rgd" -> "Burger Play (RGD)"
                    "la_cabra_sanduchera_rgd" -> "La Cabra"
                    "la_liebre_franco" -> "La Liebre"
                    "burger_play_sd" -> "Burger Play (SD)"
                    else -> rest.name
                }

                Box(
                    modifier = Modifier
                        .offset(
                            x = (mapWidthDp * posInfo.first) - 44.dp,
                            y = (mapHeightDp * posInfo.second) - 15.dp
                        )
                        .shadow(
                            elevation = if (isSelected) 8.dp else 3.dp,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) UniandesAmber else Color.White)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) UniandesAmber else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel?.selectRestaurant(rest.id) }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = posInfo.third,
                            fontSize = 11.sp
                        )
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) ShadowGrey else TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 2. Top Header: Search Bar & Restaurant Quick Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            // Search Bar Surface
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceWhite,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Buscar en Uniandes Food...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }

                    // Filter Action Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = UniandesAmber.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable { onOpenFilters() }
                            .padding(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_explore),
                            contentDescription = "Filtros",
                            tint = UniandesAmber,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Selection Chips Row for all campus restaurants
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                restaurants.forEach { rest ->
                    val isSelected = rest.id == selectedRestaurant?.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) UniandesAmber else CardSurfaceWhite.copy(alpha = 0.95f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) UniandesAmber else BorderLight
                        ),
                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                        modifier = Modifier.clickable { viewModel?.selectRestaurant(rest.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) ShadowGrey else UniandesAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = rest.name.replace(" - RGD", "").replace(" - ML", "").replace(" - Franco", "").replace(" - SD", ""),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) ShadowGrey else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating Bottom Restaurant Card
        if (selectedRestaurant != null) {
            val isFav = favorites.contains(selectedRestaurant.id)
            val walkMin = selectedRestaurant.walkDistancesFromBuilding[currentBuilding] ?: 2
            val statusColor = when (selectedRestaurant.waitTimeCategory) {
                WaitTimeCategory.FAST -> StatusFastGreen
                WaitTimeCategory.MODERATE -> StatusModerateAmber
                WaitTimeCategory.LONG -> StatusLongRed
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Title + Building + Favorite Icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedRestaurant.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = ShadowGrey,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedRestaurant.category} • Edificio ${selectedRestaurant.buildingTag}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }

                            // Favorite Heart Toggle Button
                            IconButton(
                                onClick = { viewModel?.toggleFavorite(selectedRestaurant.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) StatusLongRed else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Badges Row (Walk time, Rating, Wait queue)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Walk time badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundOffWhite
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_walk_time),
                                        contentDescription = "Walk",
                                        tint = UniandesAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "$walkMin min desde Edificio $currentBuilding",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = ShadowGrey
                                    )
                                }
                            }

                            // Rating badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BackgroundOffWhite
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_rating_filled),
                                        contentDescription = "Rating",
                                        tint = UniandesAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (selectedRestaurant.reviewCount == 0) "N/A ★ (0)" else "${selectedRestaurant.rating} ★ (${selectedRestaurant.reviewCount})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = ShadowGrey
                                    )
                                }
                            }

                            // Wait time traffic badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = selectedRestaurant.waitTimeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Orange CTA Button: "Ver Menú y Detalles"
                        Button(
                            onClick = { onViewRestaurant(selectedRestaurant) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_menu),
                                    contentDescription = null,
                                    tint = ShadowGrey,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Ver Menú y Detalles",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ShadowGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
