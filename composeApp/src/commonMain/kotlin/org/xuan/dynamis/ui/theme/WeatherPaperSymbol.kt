package org.xuan.dynamis.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val PaperWhite = Color(0xFFF9F7F0)
private val PaperBlue = Color(0xFFB7D7E3)
private val PaperShadow = Color(0xFF102C46)
private val PaperGold = Color(0xFFFFCF72)
private val PaperPaleGold = Color(0xFFFFE9A9)

internal enum class PaperWeatherKind {
    Clear,
    MainlyClear,
    PartlyCloudy,
    Overcast,
    Fog,
    Drizzle,
    Rain,
    Snow,
    SnowGrains,
    RainShowers,
    SnowShowers,
    Thunderstorm,
    Unknown,
}

internal data class PaperWeatherSpec(
    val kind: PaperWeatherKind,
    val amount: Int = 0,
    val icy: Boolean = false,
    val rime: Boolean = false,
    val hail: Boolean = false,
)

/** Open-Meteo WMO codes each select a cutout composition and precipitation detail. */
internal fun paperWeatherSpec(code: Int?): PaperWeatherSpec =
    when (code) {
        0 -> PaperWeatherSpec(PaperWeatherKind.Clear)
        1 -> PaperWeatherSpec(PaperWeatherKind.MainlyClear)
        2 -> PaperWeatherSpec(PaperWeatherKind.PartlyCloudy)
        3 -> PaperWeatherSpec(PaperWeatherKind.Overcast)
        45 -> PaperWeatherSpec(PaperWeatherKind.Fog)
        48 -> PaperWeatherSpec(PaperWeatherKind.Fog, rime = true)
        51 -> PaperWeatherSpec(PaperWeatherKind.Drizzle, amount = 3)
        53 -> PaperWeatherSpec(PaperWeatherKind.Drizzle, amount = 5)
        55 -> PaperWeatherSpec(PaperWeatherKind.Drizzle, amount = 7)
        56 -> PaperWeatherSpec(PaperWeatherKind.Drizzle, amount = 4, icy = true)
        57 -> PaperWeatherSpec(PaperWeatherKind.Drizzle, amount = 7, icy = true)
        61 -> PaperWeatherSpec(PaperWeatherKind.Rain, amount = 4)
        63 -> PaperWeatherSpec(PaperWeatherKind.Rain, amount = 6)
        65 -> PaperWeatherSpec(PaperWeatherKind.Rain, amount = 9)
        66 -> PaperWeatherSpec(PaperWeatherKind.Rain, amount = 5, icy = true)
        67 -> PaperWeatherSpec(PaperWeatherKind.Rain, amount = 9, icy = true)
        71 -> PaperWeatherSpec(PaperWeatherKind.Snow, amount = 4)
        73 -> PaperWeatherSpec(PaperWeatherKind.Snow, amount = 6)
        75 -> PaperWeatherSpec(PaperWeatherKind.Snow, amount = 9)
        77 -> PaperWeatherSpec(PaperWeatherKind.SnowGrains, amount = 11)
        80 -> PaperWeatherSpec(PaperWeatherKind.RainShowers, amount = 4)
        81 -> PaperWeatherSpec(PaperWeatherKind.RainShowers, amount = 6)
        82 -> PaperWeatherSpec(PaperWeatherKind.RainShowers, amount = 9)
        85 -> PaperWeatherSpec(PaperWeatherKind.SnowShowers, amount = 5)
        86 -> PaperWeatherSpec(PaperWeatherKind.SnowShowers, amount = 9)
        95 -> PaperWeatherSpec(PaperWeatherKind.Thunderstorm, amount = 4)
        96 -> PaperWeatherSpec(PaperWeatherKind.Thunderstorm, amount = 3, hail = true)
        99 -> PaperWeatherSpec(PaperWeatherKind.Thunderstorm, amount = 7, hail = true)
        else -> PaperWeatherSpec(PaperWeatherKind.Unknown)
    }

/** A transparent, layered paper weather symbol over the existing sky. */
@Composable
fun WeatherPaperSymbol(
    weatherCode: Int?,
    timeOfDay: TimeOfDay,
    moonPhase: Float,
    modifier: Modifier = Modifier,
) {
    val spec = paperWeatherSpec(weatherCode)
    Canvas(modifier.fillMaxWidth().height(230.dp)) {
        val unit = min(size.width / 360f, size.height / 230f)
        val left = (size.width - 360f * unit) / 2f
        val top = (size.height - 230f * unit) / 2f

        fun point(
            x: Float,
            y: Float,
        ) = Offset(left + x * unit, top + y * unit)
        val night = timeOfDay == TimeOfDay.Night
        val clear = spec.kind == PaperWeatherKind.Clear || spec.kind == PaperWeatherKind.MainlyClear
        val hasCelestialBody =
            clear || spec.kind == PaperWeatherKind.PartlyCloudy ||
                spec.kind == PaperWeatherKind.RainShowers || spec.kind == PaperWeatherKind.SnowShowers

        if (hasCelestialBody) {
            val x = if (clear) 180f else 232f
            val y = if (clear) 99f else 75f
            if (night) {
                drawPaperMoon(::point, x, y, 48f, moonPhase, unit)
            } else {
                drawPaperSun(::point, x, y, 45f, unit)
            }
        }

        when (spec.kind) {
            PaperWeatherKind.Clear -> {
                Unit
            }

            PaperWeatherKind.MainlyClear -> {
                drawPaperCloud(::point, 64f, 139f, 0.68f, PaperWhite, unit)
            }

            PaperWeatherKind.Fog -> {
                drawTissueFog(::point, spec.rime, unit)
            }

            PaperWeatherKind.PartlyCloudy, PaperWeatherKind.RainShowers,
            PaperWeatherKind.SnowShowers,
            -> {
                drawPaperCloud(::point, 114f, 115f, 1.08f, PaperWhite, unit)
            }

            else -> {
                val rear = if (spec.kind == PaperWeatherKind.Thunderstorm) Color(0xFF8095AD) else PaperBlue
                drawPaperCloud(::point, 73f, 82f, 1.14f, rear, unit)
                drawPaperCloud(
                    ::point,
                    137f,
                    114f,
                    1.11f,
                    if (night) Color(0xFFE0E8EC) else PaperWhite,
                    unit,
                )
            }
        }

        when (spec.kind) {
            PaperWeatherKind.Drizzle, PaperWeatherKind.Rain, PaperWeatherKind.RainShowers -> {
                drawPaperRain(::point, spec.amount, spec.kind == PaperWeatherKind.Drizzle, spec.icy, unit)
            }

            PaperWeatherKind.Snow, PaperWeatherKind.SnowShowers, PaperWeatherKind.SnowGrains -> {
                drawPaperSnow(::point, spec.amount, spec.kind == PaperWeatherKind.SnowGrains, unit)
            }

            PaperWeatherKind.Thunderstorm -> {
                if (spec.hail) {
                    drawPaperHail(::point, spec.amount, unit)
                } else {
                    drawPaperRain(::point, spec.amount, false, false, unit)
                }
                val bolt =
                    Path().apply {
                        moveTo(point(192f, 150f).x, point(192f, 150f).y)
                        lineTo(point(166f, 187f).x, point(166f, 187f).y)
                        lineTo(point(184f, 184f).x, point(184f, 184f).y)
                        lineTo(point(175f, 219f).x, point(175f, 219f).y)
                        lineTo(point(213f, 177f).x, point(213f, 177f).y)
                        lineTo(point(193f, 180f).x, point(193f, 180f).y)
                        close()
                    }
                drawPath(bolt, PaperGold)
            }

            else -> {
                Unit
            }
        }
    }
}

private fun DrawScope.drawPaperSun(
    point: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    radius: Float,
    unit: Float,
) {
    for (ray in 0 until 10) {
        val angle = ray * (2f * PI.toFloat() / 10f)
        drawLine(
            PaperShadow.copy(alpha = 0.22f),
            point(x + cos(angle) * 57f + 3f, y + sin(angle) * 57f + 4f),
            point(x + cos(angle) * 68f + 3f, y + sin(angle) * 68f + 4f),
            strokeWidth = 7f * unit,
            cap = StrokeCap.Round,
        )
        drawLine(
            if (ray % 2 == 0) PaperGold else PaperPaleGold,
            point(x + cos(angle) * 57f, y + sin(angle) * 57f),
            point(x + cos(angle) * 68f, y + sin(angle) * 68f),
            strokeWidth = 7f * unit,
            cap = StrokeCap.Round,
        )
    }
    drawCircle(PaperShadow.copy(alpha = 0.27f), radius * unit, point(x + 5f, y + 7f))
    drawCircle(PaperPaleGold, radius * unit, point(x, y))
    drawCircle(PaperGold, (radius - 6f) * unit, point(x, y))
}

private fun DrawScope.drawPaperMoon(
    point: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    radius: Float,
    phase: Float,
    unit: Float,
) {
    val age = ((phase % 1f) + 1f) % 1f
    drawCircle(PaperShadow.copy(alpha = 0.28f), radius * unit, point(x + 5f, y + 7f))
    drawCircle(Color(0xFF536982), radius * unit, point(x, y))

    val waxing = age <= 0.5f
    val terminator = cos(2.0 * PI * age).toFloat() * if (waxing) 1f else -1f
    val lit = Path()
    val steps = 40
    for (step in 0..steps) {
        val dy = -radius + 2f * radius * step / steps
        val halfWidth = sqrt((radius * radius - dy * dy).coerceAtLeast(0f))
        val outerX = x + if (waxing) halfWidth else -halfWidth
        val position = point(outerX, y + dy)
        if (step == 0) {
            lit.moveTo(position.x, position.y)
        } else {
            lit.lineTo(position.x, position.y)
        }
    }
    for (step in steps downTo 0) {
        val dy = -radius + 2f * radius * step / steps
        val halfWidth = sqrt((radius * radius - dy * dy).coerceAtLeast(0f))
        val position = point(x + terminator * halfWidth, y + dy)
        lit.lineTo(position.x, position.y)
    }
    lit.close()
    drawPath(lit, Color(0xFFFFF0C3))
}

private fun DrawScope.drawPaperCloud(
    point: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    scale: Float,
    color: Color,
    unit: Float,
) {
    fun layer(
        dx: Float,
        dy: Float,
        tint: Color,
    ) {
        val u = unit * scale
        drawCircle(tint, 29f * u, point(x + dx + 31f * scale, y + dy + 12f * scale))
        drawCircle(tint, 37f * u, point(x + dx + 73f * scale, y + dy))
        drawCircle(tint, 27f * u, point(x + dx + 113f * scale, y + dy + 14f * scale))
        drawRoundRect(
            tint,
            point(x + dx + 12f * scale, y + dy + 10f * scale),
            Size(122f * u, 42f * u),
            CornerRadius(20f * u),
        )
    }
    layer(6f, 8f, PaperShadow.copy(alpha = 0.25f))
    layer(0f, 0f, color)
}

private fun DrawScope.drawTissueFog(
    point: (Float, Float) -> Offset,
    rime: Boolean,
    unit: Float,
) {
    for (band in 0..2) {
        val y = 65f + band * 44f
        val left = 55f + band * 11f
        val right = 305f - band * 9f
        val tissue =
            Path().apply {
                moveTo(point(left, y + 8f).x, point(left, y + 8f).y)
                cubicTo(
                    point(112f, y - 7f).x,
                    point(112f, y - 7f).y,
                    point(225f, y + 13f).x,
                    point(225f, y + 13f).y,
                    point(right, y).x,
                    point(right, y).y,
                )
                lineTo(point(right - 8f, y + 34f).x, point(right - 8f, y + 34f).y)
                cubicTo(
                    point(220f, y + 43f).x,
                    point(220f, y + 43f).y,
                    point(115f, y + 22f).x,
                    point(115f, y + 22f).y,
                    point(left + 5f, y + 39f).x,
                    point(left + 5f, y + 39f).y,
                )
                close()
            }
        // The transparent wash lets the live sky remain visible through each layer.
        drawPath(tissue, PaperShadow.copy(alpha = 0.09f))
        drawPath(
            tissue,
            if (band % 2 == 0) {
                PaperWhite.copy(alpha = 0.28f)
            } else {
                PaperBlue.copy(alpha = 0.24f)
            },
        )
        for (fiber in 0..12) {
            val x = left + 13f + fiber * 17f
            val yy = y + 13f + ((fiber * 7 + band * 3) % 13)
            drawLine(
                PaperWhite.copy(alpha = 0.32f),
                point(x, yy),
                point(x + 8f, yy - 2f),
                strokeWidth = 1.2f * unit,
                cap = StrokeCap.Round,
            )
        }
    }
    if (rime) drawPaperHail(point, 4, unit)
}

private fun DrawScope.drawPaperRain(
    point: (Float, Float) -> Offset,
    count: Int,
    drizzle: Boolean,
    icy: Boolean,
    unit: Float,
) {
    val length = if (drizzle) 12f else 21f
    val width = if (drizzle) 3.5f else 5f
    for (index in 0 until count) {
        val x = 180f + (index - (count - 1) / 2f) * 24f
        val y = 165f + (index % 3) * 9f
        drawLine(
            PaperShadow.copy(alpha = 0.27f),
            point(x + 3f, y + 4f),
            point(x - 5f, y + length + 4f),
            strokeWidth = width * unit,
            cap = StrokeCap.Round,
        )
        drawLine(
            PaperBlue,
            point(x, y),
            point(x - 8f, y + length),
            strokeWidth = width * unit,
            cap = StrokeCap.Round,
        )
    }
    if (icy) drawPaperHail(point, if (count > 5) 4 else 2, unit)
}

private fun DrawScope.drawPaperSnow(
    point: (Float, Float) -> Offset,
    count: Int,
    grains: Boolean,
    unit: Float,
) {
    for (index in 0 until count) {
        if (grains) {
            val x = 180f + (index - (count - 1) / 2f) * 18f
            val y = 168f + (index % 3) * 16f
            drawCircle(PaperShadow.copy(alpha = 0.23f), 2.8f * unit, point(x + 3f, y + 4f))
            drawCircle(PaperWhite, 2.8f * unit, point(x, y))
        } else {
            val spacing = min(42f, 170f / (count - 1))
            val x = 188f + (index - (count - 1) / 2f) * spacing
            val y = 176f + (index % 3) * 12f
            val radius =
                0.7f *
                    when {
                        count >= 9 -> 13f
                        count >= 6 -> 16f
                        else -> 19f
                    }
            drawCutPaperSnowflake(point, x, y, radius, index, unit)
        }
    }
}

/** Four folded-paper motifs with varied cuts, repeated around six axes. */
internal fun DrawScope.drawCutPaperSnowflake(
    point: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    radius: Float,
    design: Int,
    unit: Float,
) {
    val motif = design % 4
    val variation = design / 4

    fun layer(
        dx: Float,
        dy: Float,
        color: Color,
    ) {
        val centerX = x + dx
        val centerY = y + dy

        fun onArm(
            angle: Float,
            distance: Float,
            sideways: Float = 0f,
        ): Offset =
            point(
                centerX + cos(angle) * distance - sin(angle) * sideways,
                centerY + sin(angle) * distance + cos(angle) * sideways,
            )
        drawCircle(color, radius * 0.24f * unit, point(centerX, centerY))
        for (arm in 0 until 6) {
            val angle = -PI.toFloat() / 2f + arm * PI.toFloat() / 3f
            drawLine(
                color,
                onArm(angle, 0f),
                onArm(angle, radius),
                strokeWidth = (if (motif == 1) 3.6f else 2.8f) * unit,
                cap = StrokeCap.Square,
            )

            val branches =
                when (motif) {
                    0 -> floatArrayOf(0.52f, 0.77f)

                    // Fern-like V cuts.
                    1 -> floatArrayOf(0.58f)

                    // Broad diamonds at the tips.
                    2 -> floatArrayOf(0.39f, 0.68f)

                    // Fine double branching.
                    else -> floatArrayOf(0.46f, 0.73f) // Wide paper spokes.
                }
            for ((branch, fraction) in branches.withIndex()) {
                val base = radius * (fraction + variation * 0.035f)
                val reach =
                    radius *
                        when (motif) {
                            1 -> 0.37f
                            2 -> if (branch == 0) 0.28f else 0.22f
                            else -> if (branch == 0) 0.31f else 0.21f
                        } * (1f + variation * 0.13f)
                for (side in listOf(-1f, 1f)) {
                    drawLine(
                        color,
                        onArm(angle, base),
                        onArm(angle, base + reach * 0.55f, side * reach),
                        strokeWidth = (if (motif == 3) 2.8f else 2.2f) * unit,
                        cap = StrokeCap.Square,
                    )
                }
            }
            if (motif == 1) {
                drawCircle(color, radius * 0.16f * unit, onArm(angle, radius))
            }
        }
    }

    layer(2.5f, 3.5f, PaperShadow.copy(alpha = 0.28f))
    layer(0f, 0f, PaperWhite)

    // Small punched centers make the flakes read as folded and cut paper.
    when (motif) {
        0 -> {
            drawCircle(PaperBlue, radius * 0.09f * unit, point(x, y))
        }

        1 -> {
            drawCircle(PaperBlue, radius * 0.12f * unit, point(x, y))
        }

        2 -> {
            drawCircle(PaperBlue, radius * 0.08f * unit, point(x, y))
            for (hole in 0 until 6) {
                val angle = hole * PI.toFloat() / 3f
                drawCircle(
                    PaperBlue,
                    radius * 0.07f * unit,
                    point(x + cos(angle) * radius * 0.45f, y + sin(angle) * radius * 0.45f),
                )
            }
        }

        else -> {
            drawCircle(PaperBlue, radius * 0.15f * unit, point(x, y))
        }
    }
}

private fun DrawScope.drawPaperHail(
    point: (Float, Float) -> Offset,
    count: Int,
    unit: Float,
) {
    for (index in 0 until count) {
        val x = 180f + (index - (count - 1) / 2f) * 37f
        val y = 207f + (index % 2) * 8f
        val diamond =
            Path().apply {
                moveTo(point(x, y - 6f).x, point(x, y - 6f).y)
                lineTo(point(x + 6f, y).x, point(x + 6f, y).y)
                lineTo(point(x, y + 6f).x, point(x, y + 6f).y)
                lineTo(point(x - 6f, y).x, point(x - 6f, y).y)
                close()
            }
        drawCircle(PaperShadow.copy(alpha = 0.2f), 7f * unit, point(x + 3f, y + 4f))
        drawPath(diamond, PaperWhite)
    }
}
