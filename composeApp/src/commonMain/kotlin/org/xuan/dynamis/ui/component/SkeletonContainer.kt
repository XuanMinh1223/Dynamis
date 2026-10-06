package org.xuan.dynamis.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor

private val SkeletonColor = Color(0xFFE2E8F0)

private const val RevealMillis = 700

private const val MinAlpha = 0.35f
private const val MaxAlpha = 1f

/** List rows share one period and each lags the previous by [RowLag] of a cycle. */
private const val ListPeriodSeconds = 1.5f
private const val RowLag = 0.08f

/** Standalone blocks draw a period in this range so neighbours drift apart. */
private const val MinPeriodSeconds = 1.2f
private const val MaxPeriodSeconds = 1.9f

/** Irrational steps spread consecutive slots evenly without any coordination between blocks. */
private const val GoldenRatioStep = 0.618034f
private const val PlasticStep = 0.754878f

internal class PulseTiming(val periodSeconds: Float, val offset: Float)

private fun fraction(value: Float) = value - floor(value)

/** Ordered wave for lazy lists: same period, offset grows with [index]. */
internal fun listTiming(index: Int) = PulseTiming(ListPeriodSeconds, -index * RowLag)

/** Stable, well-spread period and offset for the [slot]-th standalone block on a clock. */
internal fun standaloneTiming(slot: Int) = PulseTiming(
    periodSeconds = MinPeriodSeconds + (MaxPeriodSeconds - MinPeriodSeconds) * fraction(slot * PlasticStep),
    offset = fraction(slot * GoldenRatioStep),
)

/** Placeholder alpha at [seconds] on the shared clock. */
internal fun pulseAlpha(seconds: Float, timing: PulseTiming): Float {
    val cycle = fraction(seconds / timing.periodSeconds + timing.offset)
    val wave = 0.5f + 0.5f * cos(2f * PI.toFloat() * cycle)
    return MinAlpha + (MaxAlpha - MinAlpha) * wave
}

/**
 * One frame callback for every skeleton under it. It only ticks while at least one placeholder
 * is visible, so an idle screen costs nothing. Blocks read [seconds] in draw-phase lambdas, so
 * a tick redraws their layers without recomposing anything.
 */
@Stable
internal class SkeletonClock {
    val seconds = mutableFloatStateOf(0f)
    private val consumers = mutableIntStateOf(0)
    private var nextSlot = 0

    val running: Boolean get() = consumers.intValue > 0

    fun claimSlot(): Int = nextSlot++
    fun acquire() { consumers.intValue++ }
    fun release() { consumers.intValue-- }
}

@Composable
private fun SkeletonClockDriver(clock: SkeletonClock) {
    val running by remember(clock) { derivedStateOf { clock.running } }
    LaunchedEffect(clock, running) {
        if (!running) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (true) withFrameNanos { clock.seconds.floatValue = (it - start) / 1_000_000_000f }
    }
}

private val LocalSkeletonClock = compositionLocalOf<SkeletonClock?> { null }

/**
 * Optional: provide one clock for a whole screen or list. Without it each [SkeletonContainer]
 * runs its own, which is fine for a handful of blocks but wasteful for many. In a lazy list it
 * also keeps rows in step as they scroll in. Rows with a higher `index` pulse slightly later,
 * so the fade travels down the list like a loading bar.
 */
@Composable
fun SkeletonClockProvider(content: @Composable () -> Unit) {
    val clock = remember { SkeletonClock() }
    SkeletonClockDriver(clock)
    CompositionLocalProvider(LocalSkeletonClock provides clock, content = content)
}

/**
 * Simple skeleton: while [isLoading] a block in [placeholderShape] fades in and out over the
 * area [content] will occupy, then crossfades to [content] when loading ends.
 *
 * Pass [index] for lazy-list rows to get the ordered, top-to-bottom wave. Leave it null for
 * standalone blocks, which each get their own period and start offset for visual variety.
 *
 * [content] is always composed (invisible and non-interactive while loading) so the placeholder
 * takes its measured size; give [content] or [modifier] a size that holds up without data.
 */
@Composable
fun SkeletonContainer(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    placeholderShape: Shape = RectangleShape,
    index: Int? = null,
    content: @Composable () -> Unit,
) {
    val provided = LocalSkeletonClock.current
    val clock = provided ?: remember { SkeletonClock() }
    if (provided == null) SkeletonClockDriver(clock)
    val timing = remember(clock, index) {
        if (index != null) listTiming(index) else standaloneTiming(clock.claimSlot())
    }
    // 0 = skeleton, 1 = content. Gentle in both directions so nothing pops.
    val reveal = remember { Animatable(if (isLoading) 0f else 1f) }
    LaunchedEffect(isLoading) {
        reveal.animateTo(if (isLoading) 0f else 1f, tween(RevealMillis, easing = FastOutSlowInEasing))
    }
    val skeletonVisible by remember(isLoading) { derivedStateOf { isLoading || reveal.value < 1f } }
    if (skeletonVisible) {
        DisposableEffect(clock) {
            clock.acquire()
            onDispose { clock.release() }
        }
    }
    // The block keeps the size it had when first measured while loading, so it never resizes
    // when the content underneath does. It is drawn, not laid out, so it can't shift anything.
    var blockSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier.drawWithContent {
            drawContent()
            val alpha = pulseAlpha(clock.seconds.floatValue, timing) * (1f - reveal.value)
            if (alpha > 0f) {
                val block = if (blockSize == IntSize.Zero) size else Size(blockSize.width.toFloat(), blockSize.height.toFloat())
                translate((size.width - block.width) / 2f, (size.height - block.height) / 2f) {
                    drawOutline(placeholderShape.createOutline(block, layoutDirection, this), SkeletonColor, alpha)
                }
            }
        },
    ) {
        Box(
            Modifier
                .onSizeChanged { if (isLoading && blockSize == IntSize.Zero) blockSize = it }
                .graphicsLayer { alpha = reveal.value }
                .then(if (isLoading) Modifier.hideAndBlock() else Modifier),
        ) {
            content()
        }
    }
}

/** Keeps the invisible content out of accessibility and away from touches while the skeleton shows. */
private fun Modifier.hideAndBlock(): Modifier =
    clearAndSetSemantics {}.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
            }
        }
    }
