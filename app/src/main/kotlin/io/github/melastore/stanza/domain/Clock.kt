package io.github.melastore.stanza.domain

// Behind an interface so tests can use a fake clock.
interface Clock {
	// SystemClock.elapsedRealtime(): ms since boot, keeps counting in deep sleep.
	fun elapsedRealtime(): Long

	fun currentTimeMillis(): Long
}
