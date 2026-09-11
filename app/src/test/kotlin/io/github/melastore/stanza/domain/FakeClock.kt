package io.github.melastore.stanza.domain

class FakeClock(private var currentRealtime: Long = 100_000L, private var currentWall: Long = 1_700_000_000_000L,) :
	Clock {

	override fun elapsedRealtime(): Long = currentRealtime

	override fun currentTimeMillis(): Long = currentWall

	fun advanceBy(millis: Long) {
		currentRealtime += millis
		currentWall += millis
	}

	fun setWallClock(wall: Long) {
		currentWall = wall
	}

	fun setRealtime(realtime: Long) {
		currentRealtime = realtime
	}
}
