package io.github.melastore.stanza.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.ui.theme.BreakAmbientDark
import io.github.melastore.stanza.ui.theme.BreakPrimary
import io.github.melastore.stanza.ui.theme.BreakSecondary
import io.github.melastore.stanza.ui.theme.DarkBackground
import io.github.melastore.stanza.ui.theme.FocusAmbientDark
import io.github.melastore.stanza.ui.theme.FocusPrimary
import io.github.melastore.stanza.ui.theme.FocusSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshGradientBackdrop(phase: Phase, modifier: Modifier = Modifier,) {
	// Color palettes per phase with smooth 800ms tweening
	val targetColor1 = when (phase) {
		Phase.FOCUS -> FocusPrimary.copy(alpha = 0.45f)
		Phase.SHORT_BREAK, Phase.LONG_BREAK -> BreakPrimary.copy(alpha = 0.40f)
		Phase.IDLE -> Color(0xFF3F4660).copy(alpha = 0.25f)
	}
	val targetColor2 = when (phase) {
		Phase.FOCUS -> FocusSecondary.copy(alpha = 0.50f)
		Phase.SHORT_BREAK, Phase.LONG_BREAK -> BreakSecondary.copy(alpha = 0.45f)
		Phase.IDLE -> Color(0xFF222838).copy(alpha = 0.30f)
	}
	val targetColor3 = when (phase) {
		Phase.FOCUS -> FocusAmbientDark.copy(alpha = 0.60f)
		Phase.SHORT_BREAK, Phase.LONG_BREAK -> BreakAmbientDark.copy(alpha = 0.60f)
		Phase.IDLE -> Color(0xFF131722).copy(alpha = 0.40f)
	}

	val color1 by animateColorAsState(targetColor1, tween(800, easing = FastOutSlowInEasing), label = "c1")
	val color2 by animateColorAsState(targetColor2, tween(800, easing = FastOutSlowInEasing), label = "c2")
	val color3 by animateColorAsState(targetColor3, tween(800, easing = FastOutSlowInEasing), label = "c3")

	val transition = rememberInfiniteTransition(label = "mesh_drift")
	val driftProgress by transition.animateFloat(
		initialValue = 0f,
		targetValue = 6.2831855f, // 2 * PI
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 28_000, easing = LinearEasing),
			repeatMode = RepeatMode.Restart,
		),
		label = "drift",
	)

	Canvas(modifier = modifier.fillMaxSize()) {
		drawRect(color = DarkBackground)

		val w = size.width
		val h = size.height

		// Blob 1: Top-Left to Center drift
		val b1X = w * 0.30f + sin(driftProgress) * (w * 0.20f)
		val b1Y = h * 0.25f + cos(driftProgress * 1.3f) * (h * 0.15f)
		val b1Radius = w * 0.85f

		drawCircle(
			brush = Brush.radialGradient(
				colors = listOf(color1, Color.Transparent),
				center = Offset(b1X, b1Y),
				radius = b1Radius,
			),
			radius = b1Radius,
			center = Offset(b1X, b1Y),
		)

		// Blob 2: Bottom-Right to Center drift
		val b2X = w * 0.70f + cos(driftProgress * 0.8f) * (w * 0.25f)
		val b2Y = h * 0.65f + sin(driftProgress * 1.1f) * (h * 0.20f)
		val b2Radius = w * 0.95f

		drawCircle(
			brush = Brush.radialGradient(
				colors = listOf(color2, Color.Transparent),
				center = Offset(b2X, b2Y),
				radius = b2Radius,
			),
			radius = b2Radius,
			center = Offset(b2X, b2Y),
		)

		// Blob 3: Center ambient base
		val b3X = w * 0.50f + sin(driftProgress * 0.5f) * (w * 0.15f)
		val b3Y = h * 0.45f + cos(driftProgress * 0.7f) * (h * 0.15f)
		val b3Radius = w * 1.10f

		drawCircle(
			brush = Brush.radialGradient(
				colors = listOf(color3, Color.Transparent),
				center = Offset(b3X, b3Y),
				radius = b3Radius,
			),
			radius = b3Radius,
			center = Offset(b3X, b3Y),
		)
	}
}
