package io.github.melastore.stanza.domain

import kotlinx.serialization.Serializable

enum class DigitStyle { SINGLE_LINE, STACKED }

enum class BlockAlign { START, CENTER, END }

enum class BlockGravity { TOP, CENTER, BOTTOM }

enum class ProgressStyle { PERIMETER, RING, BAR, NONE }

// Changing any field after picking a preset turns it into CUSTOM.
@Serializable
data class TimerLayout(
	val presetId: String = VERSE,
	val digits: DigitStyle = DigitStyle.SINGLE_LINE,
	val align: BlockAlign = BlockAlign.CENTER,
	val gravity: BlockGravity = BlockGravity.CENTER,
	val progress: ProgressStyle = ProgressStyle.PERIMETER,
	val showTask: Boolean = true,
	val showPhaseLabel: Boolean = true,
	val showStanzaLines: Boolean = true,
	val showAddFive: Boolean = true,
	val showSkip: Boolean = true,
	val showReset: Boolean = true,
) {
	companion object {
		const val VERSE = "verse"
		const val POSTER = "poster"
		const val DIAL = "dial"
		const val MINIMAL = "minimal"
		const val MARQUEE = "marquee"
		const val CUSTOM = "custom"

		val presets: List<TimerLayout> = listOf(
			TimerLayout(presetId = VERSE),
			TimerLayout(
				presetId = POSTER,
				digits = DigitStyle.STACKED,
				align = BlockAlign.START,
				gravity = BlockGravity.BOTTOM,
			),
			TimerLayout(
				presetId = DIAL,
				digits = DigitStyle.STACKED,
				progress = ProgressStyle.RING,
				showStanzaLines = true,
			),
			TimerLayout(
				presetId = MINIMAL,
				showTask = false,
				showPhaseLabel = false,
				showStanzaLines = false,
				showAddFive = false,
				showSkip = false,
			),
			TimerLayout(
				presetId = MARQUEE,
				align = BlockAlign.END,
				gravity = BlockGravity.TOP,
				progress = ProgressStyle.BAR,
			),
		)

		fun preset(id: String): TimerLayout = presets.firstOrNull { it.presetId == id } ?: presets.first()

		fun labelFor(id: String): String = when (id) {
			VERSE -> "Verse"
			POSTER -> "Poster"
			DIAL -> "Dial"
			MINIMAL -> "Minimal"
			MARQUEE -> "Marquee"
			else -> "Custom"
		}
	}
}
