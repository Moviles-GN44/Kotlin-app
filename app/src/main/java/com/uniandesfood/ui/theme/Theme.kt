package com.uniandesfood.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = UniandesAmber,
    secondary = MintEmerald,
    tertiary = LavenderBlush,
    background = ShadowGrey,
    surface = ShadowGrey,
    onPrimary = ShadowGrey,
    onSecondary = CardSurfaceWhite,
    onBackground = CardSurfaceWhite,
    onSurface = CardSurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = UniandesAmber,
    secondary = MintEmerald,
    tertiary = LavenderBlush,
    background = BackgroundOffWhite,
    surface = CardSurfaceWhite,
    onPrimary = ShadowGrey,
    onSecondary = CardSurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun UniandesFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UniandesFoodTypography,
        content = content
    )
}