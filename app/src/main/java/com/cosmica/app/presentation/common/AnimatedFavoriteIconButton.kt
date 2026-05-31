package com.cosmica.app.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cosmica.app.presentation.theme.GlassSurface
import com.cosmica.app.presentation.theme.MeteorRed
import kotlinx.coroutines.delay

/**
 * Heart icon button that animates both color and scale on toggle.
 *
 * Scale uses a brief "pop" — bouncing up to ~1.35× then settling back, so each
 * favorite/unfavorite action gives clear haptic-style feedback even without vibration.
 */
@Composable
fun AnimatedFavoriteIconButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    favoritedTint: Color = MeteorRed,
    unfavoritedTint: Color = Color.White,
    contentDescription: String? = null,
) {
    var popping by remember { mutableStateOf(false) }
    LaunchedEffect(isFavorite) {
        popping = true
        delay(180)
        popping = false
    }

    val scale by animateFloatAsState(
        targetValue   = if (popping) 1.35f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "favScale",
    )

    val tint by animateColorAsState(
        targetValue   = if (isFavorite) favoritedTint else unfavoritedTint,
        animationSpec = tween(durationMillis = 280),
        label         = "favTint",
    )

    val icon: ImageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder

    androidx.compose.foundation.layout.Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GlassSurface)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)),
                CircleShape,
            )
            .clickable(onClick = onToggle)
            .semantics {
                role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            },
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint               = tint,
            modifier           = Modifier
                .size(size)
                .scale(scale),
        )
    }
}
