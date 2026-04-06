package com.neuropulse.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neuropulse.ui.brand.NeuroPulseBrand
import com.neuropulse.ui.theme.LocalReduceMotion
import com.neuropulse.ui.theme.NeuroPulseTheme
import kotlinx.coroutines.delay

// ── State machine ─────────────────────────────────────────────────────────────

/**
 * SplashPhase — the four animation stages of the entry experience.
 *
 * Declared at file level so Compose can snapshot/restore the enum value.
 * Never declare this inside a composable function body.
 *
 * Phases advance in sequence via a single [LaunchedEffect] in [SplashScreen].
 * All animation values are derived from the current phase, making the phase
 * the single source of truth for every animated property.
 */
enum class SplashPhase {
    /** Phase 0 (0–400ms): logo scales in, wordmark fades in. Ring not yet visible. */
    LOGO_ENTER,

    /** Phase 1 (400–1200ms): arc rotates 540°, sweep grows from 0° to 270°. */
    RING_SPIN,

    /**
     * Phase 2 (1200–1800ms): ring radius expands outward, ring fades to transparent.
     * White surface overlay begins appearing simultaneously.
     */
    RING_EXPAND,

    /**
     * Phase 3 (1800–2400ms): white overlay fully covers gradient background.
     * Logo animates from center to top-left. Login content slides up from bottom.
     */
    CONTENT_REVEAL,
}

// ── Constants ─────────────────────────────────────────────────────────────────

private val LOGO_SIZE_SPLASH  = 80.dp   // Logo diameter during splash phases
private val LOGO_SIZE_FINAL   = 52.dp   // Logo diameter in the top-left final position
private val RING_STROKE_WIDTH = 3.dp    // Arc stroke weight
private val RING_RING_INSET   = 20.dp   // Gap between logo edge and ring inner edge
private val RING_MAX_RADIUS   = 220.dp  // Ring radius at peak of expansion (Phase 2)
private val CONTENT_SLIDE_PX  = 300     // Login content starting Y offset (px logic via Dp)

// ── Main composable ───────────────────────────────────────────────────────────

/**
 * SplashScreen — the animated 4-phase entry sequence for NeuroPulse.
 *
 * Animation sequence (total ~2400ms):
 * 1. Logo scales in on a horizontal gradient background (primary → primaryTint).
 * 2. A white arc appears around the logo and rotates 540° while sweeping to 270°.
 * 3. The arc expands outward and fades. A white surface overlay dissolves in.
 * 4. Logo animates from screen centre to top-left. Content slides up from below.
 *
 * When [LocalReduceMotion] is true, the entire sequence is skipped — [onSplashComplete]
 * is called immediately on first composition with zero delay (WCAG 2.3.3).
 *
 * This composable contains NO business logic. Navigation decisions are made by the
 * caller ([com.neuropulse.ui.navigation.NeuroPulseNavGraph]) after [onSplashComplete] fires.
 *
 * @param onSplashComplete Invoked after the animation finishes. Use to trigger navigation.
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val colors       = NeuroPulseTheme.colors
    val spacing      = NeuroPulseTheme.spacing
    val reduceMotion = LocalReduceMotion.current

    var phase by remember { mutableStateOf(SplashPhase.LOGO_ENTER) }

    // Single coroutine drives all phase transitions.
    // reduceMotion: skip immediately to avoid any animation delay for vestibular sensitivity.
    LaunchedEffect(Unit) {
        if (reduceMotion) {
            onSplashComplete()
            return@LaunchedEffect
        }
        delay(400);  phase = SplashPhase.RING_SPIN
        delay(800);  phase = SplashPhase.RING_EXPAND
        delay(600);  phase = SplashPhase.CONTENT_REVEAL
        delay(600);  onSplashComplete()
    }

    // ── Animated values — all derived from [phase] ────────────────────────────

    // Phase 0: logo entrance
    val logoScale by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.RING_SPIN) 1f else 0.8f,
        animationSpec  = tween(400, easing = FastOutSlowInEasing),
        label          = "logoScale",
    )
    val wordmarkAlpha by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.RING_SPIN) 1f else 0f,
        animationSpec  = tween(400, easing = FastOutSlowInEasing),
        label          = "wordmarkAlpha",
    )

    // Phase 1: ring spin (rotation + sweep)
    val ringRotation by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.RING_EXPAND) 540f else 0f,
        animationSpec  = tween(800, easing = FastOutSlowInEasing),
        label          = "ringRotation",
    )
    val ringSweepAngle by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.RING_EXPAND) 270f else 0f,
        animationSpec  = tween(800, easing = FastOutSlowInEasing),
        label          = "ringSweepAngle",
    )

    // Phase 2: ring expand + fade + white overlay
    val ringRadius by animateDpAsState(
        targetValue    = if (phase >= SplashPhase.RING_EXPAND) RING_MAX_RADIUS
                         else (LOGO_SIZE_SPLASH / 2 + RING_RING_INSET),
        animationSpec  = tween(600, easing = FastOutSlowInEasing),
        label          = "ringRadius",
    )
    val ringAlpha by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.CONTENT_REVEAL) 0f else 0.75f,
        animationSpec  = tween(600, easing = LinearEasing),
        label          = "ringAlpha",
    )
    val overlayAlpha by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.RING_EXPAND) 1f else 0f,
        animationSpec  = tween(600, easing = FastOutSlowInEasing),
        label          = "overlayAlpha",
    )

    // Phase 3: content reveal
    val contentOffsetY by animateDpAsState(
        targetValue    = if (phase >= SplashPhase.CONTENT_REVEAL) 0.dp else CONTENT_SLIDE_PX.dp,
        animationSpec  = tween(600, easing = FastOutSlowInEasing),
        label          = "contentOffsetY",
    )
    val contentAlpha by animateFloatAsState(
        targetValue    = if (phase >= SplashPhase.CONTENT_REVEAL) 1f else 0f,
        animationSpec  = tween(600, easing = FastOutSlowInEasing),
        label          = "contentAlpha",
    )

    // ── Layout ────────────────────────────────────────────────────────────────

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = maxWidth
        val maxH = maxHeight

        // Logo position: animates from screen centre → top-left (spacing.globalPadding)
        val logoOffsetX by animateDpAsState(
            targetValue   = if (phase >= SplashPhase.CONTENT_REVEAL) spacing.globalPadding
                            else (maxW / 2 - LOGO_SIZE_SPLASH / 2),
            animationSpec = tween(600, easing = FastOutSlowInEasing),
            label         = "logoOffsetX",
        )
        val logoOffsetY by animateDpAsState(
            targetValue   = if (phase >= SplashPhase.CONTENT_REVEAL) spacing.globalPadding
                            else (maxH / 2 - LOGO_SIZE_SPLASH / 2),
            animationSpec = tween(600, easing = FastOutSlowInEasing),
            label         = "logoOffsetY",
        )
        val logoSizeAnimated by animateDpAsState(
            targetValue   = if (phase >= SplashPhase.CONTENT_REVEAL) LOGO_SIZE_FINAL
                            else LOGO_SIZE_SPLASH,
            animationSpec = tween(600, easing = FastOutSlowInEasing),
            label         = "logoSize",
        )

        // Layer 1 — horizontal gradient background (static; overlay dissolves in above it)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(colors.primary, colors.primaryTint),
                    ),
                ),
        )

        // Layer 2 — white surface overlay (alpha 0→1 during Phase 2)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(overlayAlpha)
                .background(colors.surface),
        )

        // Layer 3 — logo + ring (absolutely positioned via offset)
        LogoAndRingLayer(
            logoRes       = NeuroPulseBrand.logoRes,
            appName       = NeuroPulseBrand.APP_NAME,
            logoSize      = logoSizeAnimated,
            logoScale     = logoScale,
            wordmarkAlpha = wordmarkAlpha,
            ringRotation  = ringRotation,
            ringSweepAngle= ringSweepAngle,
            ringRadius    = ringRadius,
            ringAlpha     = ringAlpha,
            offsetX       = logoOffsetX,
            offsetY       = logoOffsetY,
            phase         = phase,
        )

        // Layer 4 — login content slide placeholder (cosmetic; real LoginScreen loads post-nav)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = contentOffsetY)
                .alpha(contentAlpha),
        )
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

/**
 * LogoAndRingLayer — renders the logo, wordmark, and animated ring arc.
 *
 * Extracted from [SplashScreen] to keep the parent under 30 lines (CLAUDE.md).
 * All animation values are passed in as parameters — this composable is stateless.
 */
@Composable
private fun LogoAndRingLayer(
    logoRes: Int,
    appName: String,
    logoSize: Dp,
    logoScale: Float,
    wordmarkAlpha: Float,
    ringRotation: Float,
    ringSweepAngle: Float,
    ringRadius: Dp,
    ringAlpha: Float,
    offsetX: Dp,
    offsetY: Dp,
    phase: SplashPhase,
) {
    val colors = NeuroPulseTheme.colors

    Box(modifier = Modifier.offset(x = offsetX, y = offsetY)) {

        // Logo image — scale applied via graphicsLayer (no recomposition on scale change)
        Image(
            painter           = painterResource(logoRes),
            contentDescription = appName,
            contentScale      = ContentScale.Fit,
            modifier          = Modifier
                .size(logoSize)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                },
        )

        // Wordmark — visible only during splash, fades with wordmarkAlpha
        if (phase < SplashPhase.CONTENT_REVEAL) {
            Text(
                text      = appName,
                style     = MaterialTheme.typography.titleMedium,
                color     = Color.White,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .offset(y = logoSize + 8.dp)
                    .alpha(wordmarkAlpha),
            )
        }

        // Ring arc — drawn on Canvas to allow independent control of rotation + sweep
        SplashRingCanvas(
            rotation   = ringRotation,
            sweepAngle = ringSweepAngle,
            radius     = ringRadius,
            alpha      = ringAlpha,
            logoSize   = logoSize,
        )
    }
}

/**
 * SplashRingCanvas — draws the animated arc ring around the logo.
 *
 * Uses [DrawScope.rotate] + [DrawScope.drawArc] so rotation and sweep angle
 * are animated independently:
 * - [rotation] controls the clockwise spin of the entire arc as a unit.
 * - [sweepAngle] controls how much of the circle the arc spans (0° → 270°).
 * Animating only one of these produces an incomplete effect — both are needed.
 *
 * The arc is centred on the logo by using [Modifier.drawBehind] relative to
 * an invisible [Box] whose size matches the ring diameter.
 *
 * @param rotation    Current rotation of the arc in degrees (0–540 over Phase 1).
 * @param sweepAngle  Arc sweep in degrees (0–270 over Phase 1).
 * @param radius      Ring radius in Dp — grows during Phase 2 expansion.
 * @param alpha       Ring opacity — fades to 0 during Phase 2.
 * @param logoSize    Logo size Dp — used to centre the ring box.
 */
@Composable
private fun SplashRingCanvas(
    rotation: Float,
    sweepAngle: Float,
    radius: Dp,
    alpha: Float,
    logoSize: Dp,
) {
    val ringDiameter = radius * 2
    val centreOffset = (logoSize / 2) - radius  // negative = ring extends beyond logo

    Box(
        modifier = Modifier
            .size(ringDiameter)
            .offset(x = centreOffset, y = centreOffset)
            .drawBehind {
                drawRingArc(
                    rotation   = rotation,
                    sweepAngle = sweepAngle,
                    strokeWidth = RING_STROKE_WIDTH.toPx(),
                    alpha      = alpha,
                )
            },
    )
}

/**
 * Draws the ring arc within a [DrawScope].
 *
 * Separated from the composable to satisfy the 30-line function rule (CLAUDE.md).
 * [rotate] pivots around the DrawScope centre (the ring centre), creating the
 * spin effect as [rotation] increases from 0° to 540°.
 */
private fun DrawScope.drawRingArc(
    rotation: Float,
    sweepAngle: Float,
    strokeWidth: Float,
    alpha: Float,
) {
    if (sweepAngle < 0.5f || alpha < 0.01f) return  // avoid drawing invisible arcs

    rotate(degrees = rotation) {
        drawArc(
            color      = Color.White.copy(alpha = alpha),
            startAngle = -90f,  // 12 o'clock position
            sweepAngle = sweepAngle,
            useCenter  = false,
            topLeft    = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size       = androidx.compose.ui.geometry.Size(
                width  = size.width - strokeWidth,
                height = size.height - strokeWidth,
            ),
            style      = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Splash — Phase 0 Light")
@Composable
private fun SplashScreenPreview() {
    NeuroPulseTheme(reduceMotion = true) {
        // reduceMotion=true skips animation; preview shows the initial state visually
        SplashScreen(onSplashComplete = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Splash — Phase 0 Dark")
@Composable
private fun SplashScreenDarkPreview() {
    NeuroPulseTheme(darkTheme = true, reduceMotion = true) {
        SplashScreen(onSplashComplete = {})
    }
}
