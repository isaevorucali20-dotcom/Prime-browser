package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiquidGlassBackdrop(
    blurStrength: Float = 25f,
    gradientSpeed: Float = 1.0f,
    accentColor: Color = Color(0xFF38BDF8),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassTransition")
    
    // Animate coordinates for the dynamic ambient glows
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteSpec(15000 / gradientSpeed),
        label = "GlowMovement"
    )

    // Base background colors
    val darkSlate = Color(0xFF020617)
    val blueGlow = Color(0x3D2563EB) // blue-600 with ~24% opacity
    val purpleGlow = Color(0x3D7C3AED) // purple-600 with ~24% opacity
    val indigoGlow = Color(0x296366F1) // indigo-500 with ~16% opacity

    val calculatedX1 = 0.2f + 0.15f * sin(animationProgress.toDouble()).toFloat()
    val calculatedY1 = 0.2f + 0.15f * cos(animationProgress.toDouble()).toFloat()

    val calculatedX2 = 0.8f + 0.15f * cos(animationProgress + 2f).toFloat()
    val calculatedY2 = 0.8f + 0.15f * sin(animationProgress + 2f).toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkSlate)
    ) {
        // Ambient Flow Circles resembling the Tailwind blur filters
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurStrength.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val baseWidth = size.width
                val baseHeight = size.height

                // Circle 1 - Top-Left Blue Glow matching blue-600/30
                drawCircle(
                    color = blueGlow,
                    radius = baseWidth * 0.60f,
                    center = Offset(baseWidth * calculatedX1, baseHeight * calculatedY1)
                )

                // Circle 2 - Bottom-Right Purple Glow matching purple-600/30
                drawCircle(
                    color = purpleGlow,
                    radius = baseWidth * 0.50f,
                    center = Offset(baseWidth * calculatedX2, baseHeight * calculatedY2)
                )

                // Circle 3 - Mid-Right Soft Indigo Glow matching indigo-500/20
                drawCircle(
                    color = indigoGlow,
                    radius = baseWidth * 0.40f,
                    center = Offset(
                        baseWidth * (0.7f + 0.1f * sin(animationProgress - 1f).toFloat()),
                        baseHeight * (0.5f + 0.1f * cos(animationProgress - 1f).toFloat())
                    )
                )

                // Circle 4 - Subtle user chosen Accent colored flow (interactive accent indicator)
                drawCircle(
                    color = accentColor.copy(alpha = 0.12f),
                    radius = baseWidth * 0.35f,
                    center = Offset(
                        baseWidth * (0.4f + 0.12f * cos(animationProgress).toFloat()),
                        baseHeight * (0.3f + 0.12f * sin(animationProgress).toFloat())
                    )
                )
            }
        }

        // Frosted Noise/Glossy Overlay and actual content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

private fun infiniteSpec(durationMs: Float): InfiniteRepeatableSpec<Float> {
    return infiniteRepeatable(
        animation = tween(durationMillis = durationMs.toInt(), easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
}
