package io.github.melastore.stanza.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import io.github.melastore.stanza.ui.theme.GlassBorderBottomRight
import io.github.melastore.stanza.ui.theme.GlassBorderTopLeft
import io.github.melastore.stanza.ui.theme.GlassControlFill
import io.github.melastore.stanza.ui.theme.GlassSurfaceFill
import io.github.melastore.stanza.ui.theme.SurfaceDarkElevated

enum class GlassVariant {
	CARD,
	RAISED,
	CONTROL,
}

@Composable
fun GlassSurface(
	modifier: Modifier = Modifier,
	hazeState: HazeState? = null,
	reduceTransparency: Boolean = false,
	variant: GlassVariant = GlassVariant.CARD,
	shape: Shape = RoundedCornerShape(24.dp),
	borderWidth: Dp = 1.dp,
	content: @Composable () -> Unit,
) {
	val fill = when (variant) {
		GlassVariant.CARD -> GlassSurfaceFill

		GlassVariant.RAISED -> Color(0x1FFFFFFF)

		// ~12%
		GlassVariant.CONTROL -> GlassControlFill
	}

	val borderBrush = if (reduceTransparency) {
		Brush.linearGradient(
			listOf(Color(0xFF333847), Color(0xFF1E222D)),
		)
	} else {
		Brush.linearGradient(
			listOf(GlassBorderTopLeft, GlassBorderBottomRight),
		)
	}

	val backgroundModifier = if (reduceTransparency) {
		Modifier.background(SurfaceDarkElevated, shape)
	} else if (hazeState != null) {
		Modifier
			.hazeEffect(state = hazeState)
			.background(fill, shape)
	} else {
		Modifier.background(fill, shape)
	}

	Box(
		modifier = modifier
			.clip(shape)
			.then(backgroundModifier)
			.border(borderWidth, borderBrush, shape),
	) {
		content()
	}
}
