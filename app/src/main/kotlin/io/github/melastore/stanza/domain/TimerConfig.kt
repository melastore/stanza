package io.github.melastore.stanza.domain

import kotlinx.serialization.Serializable

@Serializable
data class TimerConfig(
	val focusMinutes: Int = 25,
	val shortBreakMinutes: Int = 5,
	val longBreakMinutes: Int = 15,
	val pomodorosPerCycle: Int = 4,
	val autoStartBreaks: Boolean = false,
	val autoStartFocus: Boolean = false,
) {
	fun durationMsFor(phase: Phase): Long = when (phase) {
		Phase.FOCUS -> focusMinutes * 60_000L
		Phase.SHORT_BREAK -> shortBreakMinutes * 60_000L
		Phase.LONG_BREAK -> longBreakMinutes * 60_000L
		Phase.IDLE -> 0L
	}
}
