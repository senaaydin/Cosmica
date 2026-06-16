package com.cosmica.app.presentation.moonphase

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min

// Lit side — warm off-white with subtle highlight
private val LitHighlight = Color(0xFFFFFEF8)
private val LitMidtone   = Color(0xFFE8DFC6)
private val LitShadow    = Color(0xFFB8AB87)

// Dark side — desaturated blue-grey so the disc stays visible against deep-space background
private val DarkSide     = Color(0xFF2A3142)
private val DarkSideEdge = Color(0xFF161B28)

// Glow halo around the moon
private val HaloWarm     = Color(0xFFFFE9B0)

// Crater shading
private val CraterDark   = Color(0xFF8A7E5D).copy(alpha = 0.55f)

@Composable
fun MoonCanvas(
    phase: Double,
    modifier: Modifier = Modifier,
) {
    val glowTransition = rememberInfiniteTransition(label = "moonGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.18f,
        targetValue  = 0.32f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "moonGlowAlpha",
    )

    val animatedPhase by animateFloatAsState(
        targetValue   = phase.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label         = "moonPhase",
    )

    Canvas(modifier = modifier) {
        drawMoon(animatedPhase.toDouble(), glowAlpha)
    }
}

private fun DrawScope.drawMoon(phase: Double, glowAlpha: Float) {
    val radius = min(size.width, size.height) / 2f * 0.72f
    val center = Offset(size.width / 2f, size.height / 2f)

    // 1. Warm halo
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(HaloWarm.copy(alpha = glowAlpha), Color.Transparent),
            center = center,
            radius = radius * 2.2f,
        ),
        radius = radius * 2.2f,
        center = center,
    )

    // 2. Dark side base — drawn FIRST so the lit half overlays on top
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(DarkSide, DarkSideEdge),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )

    val p = (phase % 1.0).let { if (it < 0) it + 1 else it }

    when {
        p < 0.005 || p > 0.995 -> {
            // New moon — leave dark base as-is, plus a faint earthshine ring
        }
        p in 0.495..0.505 -> {
            drawLitDisc(center, radius)
            drawCraters(center, radius)
        }
        else -> {
            drawLitRegion(center, radius, p)
            drawCraters(center, radius)
        }
    }

    // 3. Rim outline — always visible so the moon disc is defined against space
    drawCircle(
        color  = LitHighlight.copy(alpha = 0.35f),
        radius = radius,
        center = center,
        style  = Stroke(width = 1.6f),
    )
}

private fun DrawScope.drawLitDisc(center: Offset, radius: Float) {
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(LitHighlight, LitMidtone, LitShadow),
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
            radius = radius * 1.3f,
        ),
        radius = radius,
        center = center,
    )
}

/**
 * Cosine-ellipse terminator:
 *   1. Paint the entire lit half (left or right) over the dark base disc.
 *   2. Overlay an ellipse whose width is |cos(2π·phase)| · 2R.
 *      Crescent → ellipse is DARK (carves into the lit half).
 *      Gibbous  → ellipse is LIT (extends light into the dark half).
 */
private fun DrawScope.drawLitRegion(center: Offset, radius: Float, phase: Double) {
    val waxing   = phase < 0.5
    val crescent = phase < 0.25 || phase > 0.75

    // Lit half — waxing lights the right side first, waning leaves the left lit
    val litHalfStart = if (waxing) 270f else 90f
    drawArc(
        brush      = Brush.radialGradient(
            colors = listOf(LitHighlight, LitMidtone, LitShadow),
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
            radius = radius * 1.3f,
        ),
        startAngle = litHalfStart,
        sweepAngle = 180f,
        useCenter  = true,
        topLeft    = Offset(center.x - radius, center.y - radius),
        size       = Size(radius * 2f, radius * 2f),
    )

    val ellipseWidth = (abs(cos(2.0 * PI * phase)) * radius * 2.0).toFloat()

    if (crescent) {
        // Carve dark ellipse into the lit half
        drawOval(
            brush   = Brush.radialGradient(
                colors = listOf(DarkSide, DarkSideEdge),
                center = center,
                radius = radius,
            ),
            topLeft = Offset(center.x - ellipseWidth / 2f, center.y - radius),
            size    = Size(ellipseWidth, radius * 2f),
        )
    } else {
        // Push lit ellipse into the dark half
        drawOval(
            brush   = Brush.radialGradient(
                colors = listOf(LitHighlight, LitMidtone, LitShadow),
                center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                radius = radius * 1.3f,
            ),
            topLeft = Offset(center.x - ellipseWidth / 2f, center.y - radius),
            size    = Size(ellipseWidth, radius * 2f),
        )
    }
}

private fun DrawScope.drawCraters(center: Offset, radius: Float) {
    // A handful of plausible craters; positioned by hand for visual interest
    val craters = listOf(
        Triple(-0.34f, -0.12f, 0.11f),
        Triple( 0.26f,  0.22f, 0.07f),
        Triple( 0.04f, -0.38f, 0.09f),
        Triple(-0.12f,  0.32f, 0.06f),
        Triple( 0.40f, -0.20f, 0.05f),
        Triple(-0.42f,  0.18f, 0.04f),
    )
    craters.forEach { (dx, dy, r) ->
        drawCircle(
            color  = CraterDark,
            radius = radius * r,
            center = Offset(center.x + radius * dx, center.y + radius * dy),
        )
    }
}
