package io.github.melastore.stanza.domain

/**
 * Abstraction over system time so [TimerEngine] can be tested deterministically with a fake clock.
 */
interface Clock {
	/** Milliseconds since boot, including deep sleep (SystemClock.elapsedRealtime()). */
	fun elapsedRealtime(): Long

	/** Current wall-clock epoch milliseconds (System.currentTimeMillis()). */
	fun currentTimeMillis(): Long
}
