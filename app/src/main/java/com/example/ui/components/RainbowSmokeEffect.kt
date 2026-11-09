package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SunsetGold
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Rainbow particle data for the smoke effect
data class SmokeParticle(
    val id: Long,
    val origin: Offset,
    val angle: Double,
    val speed: Float,
    val size: Float,
    val color: Color,
    val anim: Animatable<Float, *>
)

private val RainbowColors = listOf(
    NeonViolet,
    ElectricCyan,
    NeonPink,
    SunsetGold,
    RoyalBlue,
    Color(0xFF7000FF),
    Color(0xFF00F0FF)
)

/**
 * Rainbow Smoke Touch Effect Container.
 * Tapping triggers subtle swirling multi-colored smoke/puff particles
 * strictly contained inside the bounds of this component.
 */
@Composable
fun RainbowSmokeBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val particles = remember { mutableStateListOf<SmokeParticle>() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clipToBounds() // Strictly contained inside bounds
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onTap = { offset ->
                            // Spawn 12 rainbow smoke particles at tap location
                            val count = 14
                            val newParticles = (0 until count).map { i ->
                                val angle = (i.toDouble() / count) * 2 * Math.PI + Random.nextDouble(-0.3, 0.3)
                                val speed = Random.nextFloat() * 70f + 30f
                                val size = Random.nextFloat() * 32f + 18f
                                val color = RainbowColors[Random.nextInt(RainbowColors.size)]
                                SmokeParticle(
                                    id = System.nanoTime() + i,
                                    origin = offset,
                                    angle = angle,
                                    speed = speed,
                                    size = size,
                                    color = color,
                                    anim = Animatable(0f)
                                )
                            }
                            particles.addAll(newParticles)

                            newParticles.forEach { p ->
                                scope.launch {
                                    p.anim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 650,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    particles.remove(p)
                                }
                            }
                            onClick()
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()

        // Canvas for rendering the swirling smoke particles
        if (particles.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    val progress = p.anim.value
                    val currentRadius = p.size * (1f + progress * 1.4f)
                    val distance = p.speed * progress
                    // Add slight curling/vorticity to smoke
                    val curlAngle = p.angle + (progress * 0.8)
                    val cx = (p.origin.x + cos(curlAngle) * distance).toFloat()
                    val cy = (p.origin.y + sin(curlAngle) * distance).toFloat()
                    val alpha = ((1f - progress) * 0.75f).coerceIn(0f, 1f)

                    // Draw soft blurred smoke puff with radial brush
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                p.color.copy(alpha = alpha),
                                p.color.copy(alpha = alpha * 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = currentRadius
                        ),
                        radius = currentRadius,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
