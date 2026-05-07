package com.cosmica.app.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cosmica is always dark — the cosmos doesn't have a light mode.
private val CosmikaColorScheme = darkColorScheme(
    primary              = CosmicBlue,
    onPrimary            = StarWhite,
    primaryContainer     = NebulaBlue,
    onPrimaryContainer   = StarWhite,
    secondary            = NebulaPurple,
    onSecondary          = StarWhite,
    secondaryContainer   = NebulaViolet,
    onSecondaryContainer = StarWhite,
    tertiary             = StellarGold,
    onTertiary           = CosmosBlack,
    tertiaryContainer    = StellarGold.copy(alpha = 0.2f),
    onTertiaryContainer  = StellarGold,
    background           = CosmosBlack,
    onBackground         = StarWhite,
    surface              = DeepNavy,
    onSurface            = StarWhite,
    surfaceVariant       = NightSurface,
    onSurfaceVariant     = MoonGray,
    error                = MeteorRed,
    onError              = StarWhite,
    errorContainer       = MeteorRed.copy(alpha = 0.2f),
    onErrorContainer     = MeteorRed,
    outline              = MoonGray,
    outlineVariant       = NightSurface,
    scrim                = CosmosBlack.copy(alpha = 0.6f),
)

@Composable
fun CosmikaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CosmosBlack.toArgb()
            window.navigationBarColor = CosmosBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CosmikaColorScheme,
        typography  = CosmikaTypography,
        content     = content,
    )
}
