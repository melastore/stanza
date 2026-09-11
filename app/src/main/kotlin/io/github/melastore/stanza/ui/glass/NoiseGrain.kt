package io.github.melastore.stanza.ui.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import kotlin.random.Random

// Dither overlay. Without it the big radial gradients band badly on OLED.
@Composable
fun NoiseGrainOverlay(modifier: Modifier = Modifier, alpha: Float = 0.025f,) {
	val points = remember {
		val list = ArrayList<Offset>(1200)
		val random = Random(42)
		for (i in 0 until 1200) {
			list.add(Offset(random.nextFloat(), random.nextFloat()))
		}
		list
	}

	Canvas(modifier = modifier.fillMaxSize()) {
		val w = size.width
		val h = size.height

		val scaledPoints = ArrayList<Offset>(points.size)
		for (p in points) {
			scaledPoints.add(Offset(p.x * w, p.y * h))
		}

		drawPoints(
			points = scaledPoints,
			pointMode = PointMode.Points,
			color = Color.White.copy(alpha = alpha),
			strokeWidth = 1.2f,
		)
	}
}
