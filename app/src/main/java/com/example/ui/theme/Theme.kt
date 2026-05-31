package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProfessionalLightColorScheme = lightColorScheme(
    primary = LinkedInBlue,
    onPrimary = Color.White,
    primaryContainer = MutedBlue,
    onPrimaryContainer = DeepTealBlue,
    secondary = DeepTealBlue,
    onSecondary = Color.White,
    tertiary = AcademicGray,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = DarkText,
    surface = LightSurface,
    onSurface = DarkText,
    surfaceVariant = LightBackground,
    onSurfaceVariant = MediumText,
    outline = SoftGray
)

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary = LinkedInBlue,
    onPrimary = Color.White,
    primaryContainer = DeepTealBlue,
    onPrimaryContainer = Color.White,
    secondary = DeepTealBlue,
    onSecondary = Color.White,
    tertiary = AcademicGray,
    onTertiary = Color.White,
    background = Color(0xFF1D2226), // LinkedIn dark background
    onBackground = Color(0xFFF3F2EF),
    surface = Color(0xFF293138), // LinkedIn dark surface
    onSurface = Color(0xFFF3F2EF),
    surfaceVariant = Color(0xFF1D2226),
    onSurfaceVariant = Color(0xFFC9CCD1),
    outline = AcademicGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Avoid dynamic color to guarantee we stick to requested professional white, blue, and light gray styling.
    val colorScheme = if (darkTheme) ProfessionalDarkColorScheme else ProfessionalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
