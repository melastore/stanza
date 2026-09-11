package io.github.melastore.stanza.domain

import kotlinx.serialization.Serializable

@Serializable
data class TimerState(
	val phase: Phase = Phase.IDLE,
	val endsAtRealtime: Long = 0L,
	val endsAtWallClock: Long = 0L,
	val pausedRemainingMs: Long? = null,
	val completedInCycle: Int = 0,
	val currentTaskId: Long? = null,
	val totalDurationMs: Long = 0L,
) {
	val isRunning: Boolean
		get() = phase != Phase.IDLE && pausedRemainingMs == null

	val isPaused: Boolean
		get() = phase != Phase.IDLE && pausedRemainingMs != null

	val isIdle: Boolean
		get() = phase == Phase.IDLE

	/**
	 * Computes remaining time in milliseconds given the current [nowRealtime].
	 */
	fun remainingMs(nowRealtime: Long): Long = when {
		phase == Phase.IDLE -> 0L
		pausedRemainingMs != null -> pausedRemainingMs
		else -> maxOf(0L, endsAtRealtime - nowRealtime)
	}

	/**
	 * Progress fraction from 0.0f (start) to 1.0f (completed).
	 */
	fun progress(nowRealtime: Long): Float {
		if (phase == Phase.IDLE || totalDurationMs <= 0L) return 0f
		val remaining = remainingMs(nowRealtime)
		val elapsed = totalDurationMs - remaining
		return (elapsed.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
	}
}
