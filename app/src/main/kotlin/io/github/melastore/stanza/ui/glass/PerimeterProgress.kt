package io.github.melastore.stanza.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Progress drawn around the screen edge, visible on every tab.
@Composable
fun PerimeterProgress(progress: Float, accent: Color, visible: Boolean, modifier: Modifier = Modifier) {
	val sweep by animateFloatAsState(
		targetValue = if (visible) progress.coerceIn(0f, 1f) else 0f,
		animationSpec = spring(stiffness = 90f),
		label = "perimeter_sweep",
	)
	val fade by animateFloatAsState(
		targetValue = if (visible) 1f else 0f,
		animationSpec = spring(stiffness = 140f),
		label = "perimeter_fade",
	)

	Canvas(modifier = modifier.fillMaxSize()) {
		if (fade <= 0.01f) return@Canvas

		val inset = 7.dp.toPx()
		val stroke = 3.dp.toPx()
		val radius = 34.dp.toPx()
		val path = perimeterPath(
			bounds = Rect(inset, inset, size.width - inset, size.height - inset),
			radius = radius,
		)

		drawPath(
			path = path,
			color = Color.White.copy(alpha = 0.05f * fade),
			style = Stroke(width = stroke),
		)

		if (sweep <= 0.0001f) return@Canvas

		val measure = PathMeasure().apply { setPath(path, false) }
		val travelled = measure.length * sweep
		val lit = Path()
		measure.getSegment(0f, travelled, lit, true)

		drawPath(
			path = lit,
			color = accent.copy(alpha = 0.22f * fade),
			style = Stroke(width = stroke * 4f, cap = StrokeCap.Round),
		)
		drawPath(
			path = lit,
			brush = Brush.linearGradient(
				colors = listOf(accent.copy(alpha = 0.35f * fade), accent.copy(alpha = fade)),
				start = Offset.Zero,
				end = Offset(size.width, size.height),
			),
			style = Stroke(width = stroke, cap = StrokeCap.Round),
		)

		val head = measure.getPosition(travelled)
		drawCircle(color = accent.copy(alpha = 0.35f * fade), radius = stroke * 3f, center = head)
		drawCircle(color = Color.White.copy(alpha = fade), radius = stroke * 0.9f, center = head)
	}
}

// Starts at top centre and goes clockwise.
private fun perimeterPath(bounds: Rect, radius: Float): Path {
	val r = radius.coerceAtMost(minOf(bounds.width, bounds.height) / 2f)
	val d = r * 2f
	return Path().apply {
		moveTo(bounds.center.x, bounds.top)
		lineTo(bounds.right - r, bounds.top)
		arcTo(Rect(Offset(bounds.right - d, bounds.top), Size(d, d)), -90f, 90f, false)
		lineTo(bounds.right, bounds.bottom - r)
		arcTo(Rect(Offset(bounds.right - d, bounds.bottom - d), Size(d, d)), 0f, 90f, false)
		lineTo(bounds.left + r, bounds.bottom)
		arcTo(Rect(Offset(bounds.left, bounds.bottom - d), Size(d, d)), 90f, 90f, false)
		lineTo(bounds.left, bounds.top + r)
		arcTo(Rect(Offset(bounds.left, bounds.top), Size(d, d)), 180f, 90f, false)
		lineTo(bounds.center.x, bounds.top)
	}
}
