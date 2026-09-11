package io.github.melastore.stanza.domain

import android.os.SystemClock

object SystemClockImpl : Clock {
	override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()
	override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
