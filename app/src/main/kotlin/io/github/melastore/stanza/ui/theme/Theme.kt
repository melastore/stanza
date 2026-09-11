package io.github.melastore.stanza.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
	primary = FocusPrimary,
	secondary = BreakPrimary,
	background = DarkBackground,
	surface = SurfaceDark,
	surfaceContainer = SurfaceDarkElevated,
	onPrimary = Color.White,
	onSecondary = Color.Black,
	onBackground = Color(0xFFE6E8F0),
	onSurface = Color(0xFFE6E8F0),
)

@Composable
fun StanzaTheme(content: @Composable () -> Unit,) {
	MaterialTheme(
		colorScheme = DarkColorScheme,
		content = content,
	)
}
