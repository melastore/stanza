package io.github.melastore.stanza.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun StanzaTheme(palette: StanzaPalette = StanzaPalettes.first(), content: @Composable () -> Unit,) {
	val scheme = darkColorScheme(
		primary = palette.focusPrimary,
		secondary = palette.breakPrimary,
		background = palette.background,
		surface = palette.surface,
		surfaceContainer = palette.surfaceElevated,
		onPrimary = palette.ink,
		onSecondary = palette.ink,
		onBackground = Color(0xFFE6E8F0),
		onSurface = Color(0xFFE6E8F0),
	)

	CompositionLocalProvider(LocalPalette provides palette) {
		MaterialTheme(colorScheme = scheme, content = content)
	}
}
