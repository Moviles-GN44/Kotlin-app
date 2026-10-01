package com.uniandesfood

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.uniandesfood.data.model.Restaurant
import com.uniandesfood.data.model.WaitTimeCategory
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

/**
 * JavaScript Interface Bridge between Leaflet/CartoDB Map and Compose
 */
class MapWebBridge(
    private val onSelectRestaurant: (String) -> Unit,
    private val onSelectBuilding: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun selectRestaurant(id: String) {
        mainHandler.post {
            onSelectRestaurant(id)
        }
    }

    @JavascriptInterface
    fun selectBuilding(buildingId: String) {
        mainHandler.post {
            onSelectBuilding(buildingId)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
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

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    // Sync selected restaurant to map highlighting
    LaunchedEffect(selectedRestaurant?.id, isMapReady) {
        if (isMapReady && selectedRestaurant != null) {
            webViewInstance?.evaluateJavascript("highlightRestaurant('${selectedRestaurant.id}')", null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // 1. Real Interactive Campus Map (Leaflet + CartoDB Voyager / Esri Satellite)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        builtInZoomControls = false
                        displayZoomControls = false
                    }

                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.d("MapWebView", "${consoleMessage?.message()} [line ${consoleMessage?.lineNumber()}]")
                            return true
                        }
                    }

                    addJavascriptInterface(
                        MapWebBridge(
                            onSelectRestaurant = { restId ->
                                viewModel?.selectRestaurant(restId)
                            },
                            onSelectBuilding = { buildingId ->
                                viewModel?.updateCurrentBuilding(buildingId)
                            }
                        ),
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapReady = true
                            view?.evaluateJavascript("ensureMapSized();", null)
                            view?.evaluateJavascript("""
                                (function() {
                                    var el = document.getElementById('map');
                                    var rect = el ? { w: el.clientWidth, h: el.clientHeight, top: el.offsetTop } : null;
                                    return JSON.stringify({
                                        hasL: typeof L !== 'undefined',
                                        hasMap: typeof map !== 'undefined',
                                        mapRect: rect,
                                        markersCount: Object.keys(typeof restaurantMarkers !== 'undefined' ? restaurantMarkers : {}).length
                                    });
                                })()
                            """.trimIndent()) { result ->
                                android.util.Log.e("MapDiagnostic", "DIAGNOSTIC: " + result)
                            }
                            view?.postDelayed({
                                view.evaluateJavascript("ensureMapSized();", null)
                                selectedRestaurant?.let { rest ->
                                    view.evaluateJavascript("highlightRestaurant('${rest.id}')", null)
                                }
                            }, 250)
                        }
                    }

                    val htmlContent = try {
                        context.assets.open("campus_map.html").bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        ""
                    }
                    loadDataWithBaseURL("https://campus.uniandes.edu.co/", htmlContent, "text/html", "UTF-8", null)
                    webViewInstance = this
                }
            },
            update = { view ->
                webViewInstance = view
            }
        )

        // 2. Top Header: Search Bar & Restaurant Quick Selection Chips
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
                shadowElevation = 6.dp,
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
                            text = "Buscar en campus Uniandes...",
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
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier.clickable {
                            viewModel?.selectRestaurant(rest.id)
                            webViewInstance?.evaluateJavascript("highlightRestaurant('${rest.id}')", null)
                        }
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
