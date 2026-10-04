package com.dhun.music.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dhun Brand Colors - Indian-Modern Palette
val DhunViolet = Color(0xFF8B5CF6)
val DhunVioletDark = Color(0xFF6D28D9)
val DhunPink = Color(0xFFEC4899)
val DhunPinkDeep = Color(0xFFDB2777)
val DhunAmber = Color(0xFFF59E0B)

// Surfaces & Backgrounds - Deep OLED Black & Charcoal
val DhunBackground = Color(0xFF0A0A0E)
val DhunSurface = Color(0xFF13131A)
val DhunSurfaceVariant = Color(0xFF1C1C26)
val DhunCard = Color(0xFF161622)
val DhunCardElevated = Color(0xFF20202F)
val DhunBorder = Color(0xFF28283A)

// Typography & Content
val DhunTextPrimary = Color(0xFFF9FAFB)
val DhunTextSecondary = Color(0xFF9CA3AF)
val DhunTextTertiary = Color(0xFF6B7280)

// Accents & Functional
val DhunSuccess = Color(0xFF10B981)
val DhunError = Color(0xFFEF4444)

// Signature Gradients
val DhunBrandGradient = Brush.horizontalGradient(
    colors = listOf(DhunViolet, DhunPink)
)

val DhunCardGradient = Brush.verticalGradient(
    colors = listOf(DhunSurfaceVariant, DhunSurface)
)

val DhunGlowGradient = Brush.radialGradient(
    colors = listOf(DhunViolet.copy(alpha = 0.35f), Color.Transparent)
)

// Material Theme compatibility mapping
val Purple80 = DhunViolet
val PurpleGrey80 = DhunTextSecondary
val Pink80 = DhunPink
val Purple40 = DhunVioletDark
val PurpleGrey40 = DhunTextTertiary
val Pink40 = DhunPinkDeep

