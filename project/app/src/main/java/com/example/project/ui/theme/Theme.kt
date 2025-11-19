package com.example.project.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = TurquoiseBlue,
    onPrimary = Color.White,
    primaryContainer = DarkTeal,
    onPrimaryContainer = LightCyan,

    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkBlue,
    onSecondaryContainer = LightCyan,

    tertiary = LightCyan,
    onTertiary = DarkBlue,

    background = Color(0xFF0A1929),
    onBackground = OffWhite,

    surface = Color(0xFF0D2847),
    onSurface = OffWhite,

    surfaceVariant = DarkBlue,
    onSurfaceVariant = LightGray,

    error = Color(0xFFCF6679),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = LightCyan,
    onPrimaryContainer = DeepBlue,

    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = LightGray,
    onSecondaryContainer = DarkBlue,

    tertiary = TurquoiseBlue,
    onTertiary = Color.White,

    background = OffWhite,
    onBackground = DarkGray,

    surface = PureWhite,
    onSurface = DarkGray,

    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,

    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
      typography = Typography,
      content = content
    )
}