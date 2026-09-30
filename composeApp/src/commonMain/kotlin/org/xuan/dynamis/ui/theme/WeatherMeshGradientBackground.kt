package org.xuan.dynamis.ui.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Layers radial color fields into a mesh-like atmospheric background on every target. */
@Composable
fun WeatherMeshGradientBackground(
    pattern: WeatherPattern,
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = animateWeatherColorPalette(weatherColorPalette(pattern, timeOfDay))
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(palette.background)

            val radius = size.maxDimension * 0.86f
            drawRadialBloom(
                color = palette.glow,
                center = Offset(size.width * 0.16f, size.height * 0.12f),
                radius = radius,
            )
            drawRadialBloom(
                color = palette.weatherPrimary,
                center = Offset(size.width * 0.9f, size.height * 0.28f),
                radius = radius,
            )
            drawRadialBloom(
                color = palette.weatherSecondary,
                center = Offset(size.width * 0.14f, size.height * 0.78f),
                radius = radius,
            )
            drawRadialBloom(
                color = palette.shadow,
                center = Offset(size.width * 0.88f, size.height * 0.94f),
                radius = radius,
            )
        }
        CompositionLocalProvider(LocalContentColor provides palette.foreground) {
            content()
        }
    }
}

/** Keeps all color fields on the same timeline, including when a transition is interrupted. */
@Composable
private fun animateWeatherColorPalette(target: WeatherColorPalette): WeatherColorPalette {
    val transition = updateTransition(targetState = target, label = "Weather palette")
    val animationSpec = tween<Color>(durationMillis = 1_200, easing = FastOutSlowInEasing)
    val background by transition.animateColor(
        transitionSpec = { animationSpec }, label = "Background",
    ) { it.background }
    val glow by transition.animateColor(
        transitionSpec = { animationSpec }, label = "Glow",
    ) { it.glow }
    val shadow by transition.animateColor(
        transitionSpec = { animationSpec }, label = "Shadow",
    ) { it.shadow }
    val weatherPrimary by transition.animateColor(
        transitionSpec = { animationSpec }, label = "Weather primary",
    ) { it.weatherPrimary }
    val weatherSecondary by transition.animateColor(
        transitionSpec = { animationSpec }, label = "Weather secondary",
    ) { it.weatherSecondary }
    val foreground by transition.animateColor(
        transitionSpec = { animationSpec }, label = "Foreground",
    ) { it.foreground }

    return WeatherColorPalette(background, glow, shadow, weatherPrimary, weatherSecondary, foreground)
}

private fun DrawScope.drawRadialBloom(
    color: Color,
    center: Offset,
    radius: Float,
) {
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to color.copy(alpha = 0.78f),
                0.48f to color.copy(alpha = 0.34f),
                1f to Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
    )
}
