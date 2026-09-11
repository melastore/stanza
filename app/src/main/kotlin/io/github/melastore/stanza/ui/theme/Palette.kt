package io.github.melastore.stanza.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// Screens take colours from here, not from constants, so switching theme also changes the
// background and glass.
@Immutable
data class StanzaPalette(
	val id: String,
	val name: String,
	val background: Color,
	val surface: Color,
	val surfaceElevated: Color,
	val focusPrimary: Color,
	val focusSecondary: Color,
	val focusAmbient: Color,
	val breakPrimary: Color,
	val breakSecondary: Color,
	val breakAmbient: Color,
	val ink: Color,
) {
	fun primaryFor(isBreak: Boolean): Color = if (isBreak) breakPrimary else focusPrimary

	fun secondaryFor(isBreak: Boolean): Color = if (isBreak) breakSecondary else focusSecondary
}

const val CUSTOM_THEME_ID = "custom"
const val DEFAULT_THEME_ID = "amethyst"
const val DEFAULT_CUSTOM_HUE = 265f

val StanzaPalettes: List<StanzaPalette> = listOf(
	StanzaPalette(
		id = DEFAULT_THEME_ID,
		name = "Amethyst",
		background = Color(0xFF07080B),
		surface = Color(0xFF11131A),
		surfaceElevated = Color(0xFF171A24),
		focusPrimary = Color(0xFF8B64FF),
		focusSecondary = Color(0xFF6337EA),
		focusAmbient = Color(0xFF1C133F),
		breakPrimary = Color(0xFF38DEC0),
		breakSecondary = Color(0xFF16836E),
		breakAmbient = Color(0xFF092923),
		ink = Color(0xFF0B0616),
	),
	StanzaPalette(
		id = "ember",
		name = "Ember",
		background = Color(0xFF0B0705),
		surface = Color(0xFF19100C),
		surfaceElevated = Color(0xFF241611),
		focusPrimary = Color(0xFFFF8A4C),
		focusSecondary = Color(0xFFD4451F),
		focusAmbient = Color(0xFF3A1608),
		breakPrimary = Color(0xFF6FD6E8),
		breakSecondary = Color(0xFF1E7C93),
		breakAmbient = Color(0xFF07262E),
		ink = Color(0xFF200A02),
	),
	StanzaPalette(
		id = "tide",
		name = "Tide",
		background = Color(0xFF04070C),
		surface = Color(0xFF0D141F),
		surfaceElevated = Color(0xFF131D2B),
		focusPrimary = Color(0xFF4FA8FF),
		focusSecondary = Color(0xFF1E5CD6),
		focusAmbient = Color(0xFF0A2145),
		breakPrimary = Color(0xFFFFC978),
		breakSecondary = Color(0xFFC2812A),
		breakAmbient = Color(0xFF2E2008),
		ink = Color(0xFF031225),
	),
	StanzaPalette(
		id = "moss",
		name = "Moss",
		background = Color(0xFF050906),
		surface = Color(0xFF0E1712),
		surfaceElevated = Color(0xFF15211A),
		focusPrimary = Color(0xFF7BE08A),
		focusSecondary = Color(0xFF2F8F49),
		focusAmbient = Color(0xFF0D2C17),
		breakPrimary = Color(0xFFE0A6FF),
		breakSecondary = Color(0xFF8A44C4),
		breakAmbient = Color(0xFF250B33),
		ink = Color(0xFF04160A),
	),
	StanzaPalette(
		id = "rose",
		name = "Rosé",
		background = Color(0xFF0B060A),
		surface = Color(0xFF180E16),
		surfaceElevated = Color(0xFF23151F),
		focusPrimary = Color(0xFFFF7FA8),
		focusSecondary = Color(0xFFC72F63),
		focusAmbient = Color(0xFF390E21),
		breakPrimary = Color(0xFF9CE8C4),
		breakSecondary = Color(0xFF2C8F68),
		breakAmbient = Color(0xFF0A2A1E),
		ink = Color(0xFF23060F),
	),
	StanzaPalette(
		id = "slate",
		name = "Slate",
		background = Color(0xFF07080A),
		surface = Color(0xFF121418),
		surfaceElevated = Color(0xFF1A1D22),
		focusPrimary = Color(0xFFCBD3E1),
		focusSecondary = Color(0xFF7C8799),
		focusAmbient = Color(0xFF1B2029),
		breakPrimary = Color(0xFF9FB4C9),
		breakSecondary = Color(0xFF5C7085),
		breakAmbient = Color(0xFF141A21),
		ink = Color(0xFF0A0C10),
	),
)

// Custom theme from a single hue. Break colour is the opposite hue so the phases are easy to tell
// apart.
fun paletteFromHue(hue: Float): StanzaPalette {
	val h = ((hue % 360f) + 360f) % 360f
	val breakHue = (h + 155f) % 360f
	return StanzaPalette(
		id = CUSTOM_THEME_ID,
		name = "Custom",
		background = hsl(h, 0.26f, 0.035f),
		surface = hsl(h, 0.22f, 0.08f),
		surfaceElevated = hsl(h, 0.20f, 0.12f),
		focusPrimary = hsl(h, 0.85f, 0.70f),
		focusSecondary = hsl((h - 14f + 360f) % 360f, 0.75f, 0.46f),
		focusAmbient = hsl(h, 0.55f, 0.16f),
		breakPrimary = hsl(breakHue, 0.70f, 0.66f),
		breakSecondary = hsl(breakHue, 0.68f, 0.36f),
		breakAmbient = hsl(breakHue, 0.50f, 0.12f),
		ink = hsl(h, 0.60f, 0.06f),
	)
}

fun paletteFor(id: String, customHue: Float): StanzaPalette = when (id) {
	CUSTOM_THEME_ID -> paletteFromHue(customHue)
	else -> StanzaPalettes.firstOrNull { it.id == id } ?: StanzaPalettes.first()
}

private fun hsl(hue: Float, saturation: Float, lightness: Float): Color {
	val c = (1f - abs(2f * lightness - 1f)) * saturation
	val x = c * (1f - abs((hue / 60f) % 2f - 1f))
	val m = lightness - c / 2f
	val (r, g, b) = when {
		hue < 60f -> Triple(c, x, 0f)
		hue < 120f -> Triple(x, c, 0f)
		hue < 180f -> Triple(0f, c, x)
		hue < 240f -> Triple(0f, x, c)
		hue < 300f -> Triple(x, 0f, c)
		else -> Triple(c, 0f, x)
	}
	return Color(r + m, g + m, b + m)
}

val LocalPalette = staticCompositionLocalOf { StanzaPalettes.first() }
