package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimaryContainer,
    secondary = SophisticatedSecondary,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = SophisticatedSecondaryContainer,
    onSecondaryContainer = SophisticatedOnSecondaryContainer,
    tertiary = SophisticatedTertiary,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = SophisticatedTertiaryContainer,
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = SophisticatedDarkBg,
    onBackground = TextPrimary,
    surface = SophisticatedCardBg,
    onSurface = TextPrimary,
    surfaceVariant = SophisticatedSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SophisticatedCardBorder
)

private val SophisticatedLightColorScheme = lightColorScheme(
    primary = SophisticatedLightPrimary,
    onPrimary = SophisticatedLightOnPrimary,
    primaryContainer = SophisticatedLightPrimaryContainer,
    onPrimaryContainer = SophisticatedLightOnPrimaryContainer,
    secondary = SophisticatedLightSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SophisticatedLightSecondaryContainer,
    onSecondaryContainer = SophisticatedLightOnSecondaryContainer,
    tertiary = SophisticatedLightTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = SophisticatedLightTertiaryContainer,
    onTertiaryContainer = Color(0xFF31111D),
    background = SophisticatedLightBg,
    onBackground = TextLightPrimary,
    surface = SophisticatedLightSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = SophisticatedLightSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = SophisticatedLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        SophisticatedDarkColorScheme
    } else {
        SophisticatedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

