package com.uniandesfood

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.ui.theme.*
import com.uniandesfood.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: RestaurantViewModel? = null,
    onApplyFilters: () -> Unit = {}
) {

    var categoriaSeleccionada by remember { mutableStateOf("Executive Lunch") }
    var tiempoCaminando by remember { mutableStateOf("< 5 min") }
    var presupuesto by remember { mutableStateOf(5000f..25000f) }

    var vegetariano by remember { mutableStateOf(true) }
    var vegano by remember { mutableStateOf(true) }
    var sinGluten by remember { mutableStateOf(false) }
    var sinNueces by remember { mutableStateOf(false) }

    var metodoPago by remember { mutableStateOf("") }
    var abiertoAhora by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Explore",
            style = MaterialTheme.typography.headlineLarge,
            color = ShadowGrey
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontally scrollable categories row with full clean names
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Executive Lunch", "Fast Food", "Healthy", "Desserts", "Café").forEach { categoria ->
                CategoriaChip(
                    texto = categoria,
                    seleccionado = categoriaSeleccionada == categoria,
                    onClick = {
                        categoriaSeleccionada = categoria
                        viewModel?.filterByCategory(categoria)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Advanced Filters",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ShadowGrey
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Walking Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadowGrey
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "From ML",
                            style = MaterialTheme.typography.bodySmall,
                            color = UniandesAmber
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Elegir edificio",
                            tint = UniandesAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("< 5 min", "< 10 min", "< 15 min", "< 20 min").forEach { tiempo ->
                        TiempoChip(
                            texto = tiempo,
                            seleccionado = tiempoCaminando == tiempo,
                            onClick = { tiempoCaminando = tiempo }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.titleMedium,
                        color = ShadowGrey
                    )
                    Text(
                        text = "$5.000 - $25.000 COP",
                        style = MaterialTheme.typography.bodySmall,
                        color = UniandesAmber
                    )
                }

                RangeSlider(
                    value = presupuesto,
                    onValueChange = { presupuesto = it },
                    valueRange = 0f..30000f,
                    colors = SliderDefaults.colors(
                        thumbColor = UniandesAmber,
                        activeTrackColor = UniandesAmber,
                        inactiveTrackColor = BorderLight
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Dietary Restrictions",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShadowGrey
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DietaChip(texto = "Vegetarian", marcado = vegetariano) {
                        vegetariano = !vegetariano
                    }
                    DietaChip(texto = "Vegan", marcado = vegano) {
                        vegano = !vegano
                    }
                    DietaChip(texto = "Gluten-Free", marcado = sinGluten) {
                        sinGluten = !sinGluten
                    }
                    DietaChip(texto = "Nut-Free", marcado = sinNueces) {
                        sinNueces = !sinNueces
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShadowGrey
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Cash", "Card", "Nequi", "Daviplata").forEach { metodo ->
                        PagoChip(
                            texto = metodo,
                            seleccionado = metodoPago == metodo,
                            onClick = { metodoPago = metodo }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_open_now),
                            contentDescription = "Open Now",
                            tint = StatusFastGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Now",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShadowGrey
                        )
                    }
                    Switch(
                        checked = abiertoAhora,
                        onCheckedChange = { abiertoAhora = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CardSurfaceWhite,
                            checkedTrackColor = StatusFastGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        viewModel?.filterByCategory(categoriaSeleccionada)
                        onApplyFilters()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UniandesAmber),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Apply Filters",
                        style = MaterialTheme.typography.labelLarge,
                        color = ShadowGrey
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CategoriaChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionado) UniandesAmber else CardSurfaceWhite)
            .border(
                width = 1.dp,
                color = if (seleccionado) UniandesAmber else BorderLight,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) ShadowGrey else TextMuted,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun TiempoChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (seleccionado) CardSurfaceWhite else BackgroundOffWhite)
            .border(
                width = if (seleccionado) 1.5.dp else 0.dp,
                color = if (seleccionado) UniandesAmber else BorderLight,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) UniandesAmber else TextMuted,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun DietaChip(texto: String, marcado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (marcado) StatusFastGreen else CardSurfaceWhite)
            .border(
                width = 1.dp,
                color = if (marcado) StatusFastGreen else BorderLight,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (marcado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CardSurfaceWhite,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = texto,
                color = if (marcado) CardSurfaceWhite else TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun PagoChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (seleccionado) UniandesAmber.copy(alpha = 0.15f) else BackgroundOffWhite)
            .border(
                width = 1.dp,
                color = if (seleccionado) UniandesAmber else BorderLight,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) UniandesAmber else TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExploreScreenPreview() {
    UniandesFoodTheme {
        ExploreScreen()
    }
}