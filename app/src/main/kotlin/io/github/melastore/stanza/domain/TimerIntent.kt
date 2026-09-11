package io.github.melastore.stanza.domain

sealed interface TimerIntent {
	/** Start a focus session (or a specific phase) with optional task binding. */
	data class Start(val phase: Phase = Phase.FOCUS, val taskId: Long? = null) : TimerIntent

	/** Pause the current running session. */
	data object Pause : TimerIntent

	/** Resume the paused session. */
	data object Resume : TimerIntent

	/** Skip the current phase and proceed to the next phase in the cycle. */
	data object Skip : TimerIntent

	/** Add extra minutes to the current phase (default +5 minutes). */
	data class AddMinutes(val minutes: Int = 5) : TimerIntent

	/** Abort the session and reset to IDLE. */
	data object Stop : TimerIntent

	/** Fired when the alarm trigger completes the session. */
	data object PhaseCompleted : TimerIntent

	/** Fired upon boot or application restart to reconcile persisted state with current time. */
	data object ReconcileOnBoot : TimerIntent
}
