package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DhunViolet,
    onPrimary = DhunTextPrimary,
    primaryContainer = DhunVioletDark,
    onPrimaryContainer = DhunTextPrimary,
    secondary = DhunPink,
    onSecondary = DhunTextPrimary,
    secondaryContainer = DhunPinkDeep,
    onSecondaryContainer = DhunTextPrimary,
    tertiary = DhunAmber,
    onTertiary = DhunBackground,
    background = DhunBackground,
    onBackground = DhunTextPrimary,
    surface = DhunSurface,
    onSurface = DhunTextPrimary,
    surfaceVariant = DhunSurfaceVariant,
    onSurfaceVariant = DhunTextSecondary,
    outline = DhunBorder,
    error = DhunError,
    onError = DhunTextPrimary
  )

private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep Dhun's distinct signature palette fixed to ensure pristine brand aesthetics
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
