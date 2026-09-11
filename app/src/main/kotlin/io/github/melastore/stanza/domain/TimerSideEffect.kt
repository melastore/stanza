package io.github.melastore.stanza.domain

sealed interface TimerSideEffect {
	data class ArmAlarm(val triggerAtWallClock: Long) : TimerSideEffect
	data object CancelAlarm : TimerSideEffect
	data object StartForegroundService : TimerSideEffect
	data object StopForegroundService : TimerSideEffect
	data object UpdateNotification : TimerSideEffect
	data class SessionFinishedAlert(val completedPhase: Phase, val nextPhase: Phase, val taskId: Long?,) : TimerSideEffect
	data class RecordCompletedSession(
		val phase: Phase,
		val durationMinutes: Int,
		val timestamp: Long,
		val taskId: Long?,
	) : TimerSideEffect
}
