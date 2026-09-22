package com.uniandesfood

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniandesfood.ui.theme.*


@Composable
fun ScanQrScreen(
    onBack: () -> Unit = {},
    visitaVerificada: Boolean = true
) {
    val fondoCamara = Color(0xFF1A1A1A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoCamara)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = CardSurfaceWhite
                    )
                }
                Text(
                    text = "Scan QR Code",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CardSurfaceWhite
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .border(
                            width = 3.dp,
                            color = UniandesAmber,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(StatusFastGreen)
                    )
                }

                Text(
                    text = "Point at the restaurant's QR code",
                    color = CardSurfaceWhite,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                BotonCircularOscuro {
                    Text(text = "⚡", fontSize = 20.sp)
                }

                BotonCircularOscuro {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_dish_photo),
                        contentDescription = "Galería",
                        tint = CardSurfaceWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (visitaVerificada) {
                Surface(
                    color = StatusFastGreen,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_verified),
                            contentDescription = "Verificado",
                            tint = CardSurfaceWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "¡Visit verified!",
                                color = CardSurfaceWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "At El Corral Uniandes, Opening review...",
                                color = CardSurfaceWhite,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotonCircularOscuro(contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color(0xFF333333)),
        contentAlignment = Alignment.Center
    ) {
        contenido()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScanQrScreenPreview() {
    UniandesFoodTheme {
        ScanQrScreen()
    }
}