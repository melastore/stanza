package io.github.melastore.stanza.domain

sealed interface TimerIntent {
	data class Start(val phase: Phase = Phase.FOCUS, val taskId: Long? = null) : TimerIntent

	data object Pause : TimerIntent

	data object Resume : TimerIntent

	data object Skip : TimerIntent

	data class AddMinutes(val minutes: Int = 5) : TimerIntent

	data object Stop : TimerIntent

	// Sent by the alarm, not the UI.
	data object PhaseCompleted : TimerIntent

	// Sent after boot or process restart to line the saved state up with the real clock.
	data object ReconcileOnBoot : TimerIntent
}
