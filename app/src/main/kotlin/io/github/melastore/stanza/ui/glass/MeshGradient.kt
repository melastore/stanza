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
import io.github.melastore.stanza.ui.theme.LocalPalette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshGradientBackdrop(phase: Phase, modifier: Modifier = Modifier,) {
	val palette = LocalPalette.current

	val targetColor1 = when (phase) {
		Phase.FOCUS -> palette.focusPrimary.copy(alpha = 0.45f)
		Phase.SHORT_BREAK, Phase.LONG_BREAK -> palette.breakPrimary.copy(alpha = 0.40f)
		Phase.IDLE -> palette.focusPrimary.copy(alpha = 0.20f)
	}
	val targetColor2 = when (phase) {
		Phase.FOCUS -> palette.focusSecondary.copy(alpha = 0.50f)
		Phase.SHORT_BREAK, Phase.LONG_BREAK -> palette.breakSecondary.copy(alpha = 0.45f)
		Phase.IDLE -> palette.focusSecondary.copy(alpha = 0.26f)
	}
	val targetColor3 = when (phase) {
		Phase.FOCUS -> palette.focusAmbient.copy(alpha = 0.60f)
		Phase.SHORT_BREAK, Phase.LONG_BREAK -> palette.breakAmbient.copy(alpha = 0.60f)
		Phase.IDLE -> palette.focusAmbient.copy(alpha = 0.50f)
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
		drawRect(color = palette.background)

		val w = size.width
		val h = size.height

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
