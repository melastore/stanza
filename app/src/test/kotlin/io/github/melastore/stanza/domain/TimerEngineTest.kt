package io.github.melastore.stanza.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerEngineTest {

	private val clock = FakeClock(currentRealtime = 50_000L, currentWall = 1_700_000_000_000L)
	private val config = TimerConfig(
		focusMinutes = 25,
		shortBreakMinutes = 5,
		longBreakMinutes = 15,
		pomodorosPerCycle = 4,
		autoStartBreaks = false,
		autoStartFocus = false,
	)

	@Test
	fun `start focus session arms alarm and starts service`() {
		val initial = TimerState()
		val transition = TimerEngine.reduce(initial, TimerIntent.Start(Phase.FOCUS, taskId = 42L), config, clock)

		assertEquals(Phase.FOCUS, transition.state.phase)
		assertTrue(transition.state.isRunning)
		assertEquals(42L, transition.state.currentTaskId)
		assertEquals(50_000L + 25 * 60_000L, transition.state.endsAtRealtime)
		assertEquals(1_700_000_000_000L + 25 * 60_000L, transition.state.endsAtWallClock)

		assertTrue(transition.effects.any { it is TimerSideEffect.ArmAlarm })
		assertTrue(transition.effects.contains(TimerSideEffect.StartForegroundService))
		assertTrue(transition.effects.contains(TimerSideEffect.UpdateNotification))
	}

	@Test
	fun `pause captures exact remaining time and cancels alarm`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state
		clock.advanceBy(10 * 60_000L) // 10 minutes pass

		val paused = TimerEngine.reduce(started, TimerIntent.Pause, config, clock)
		assertTrue(paused.state.isPaused)
		assertEquals(15 * 60_000L, paused.state.pausedRemainingMs)
		assertTrue(paused.effects.contains(TimerSideEffect.CancelAlarm))
	}

	@Test
	fun `resume restores countdown and re-arms alarm`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state
		clock.advanceBy(10 * 60_000L)
		val paused = TimerEngine.reduce(started, TimerIntent.Pause, config, clock).state

		clock.advanceBy(5 * 60_000L) // Paused for 5 minutes

		val resumed = TimerEngine.reduce(paused, TimerIntent.Resume, config, clock)
		assertTrue(resumed.state.isRunning)
		assertNull(resumed.state.pausedRemainingMs)
		assertEquals(clock.elapsedRealtime() + 15 * 60_000L, resumed.state.endsAtRealtime)
		assertEquals(clock.currentTimeMillis() + 15 * 60_000L, resumed.state.endsAtWallClock)

		val armAlarm = resumed.effects.filterIsInstance<TimerSideEffect.ArmAlarm>().firstOrNull()
		assertEquals(clock.currentTimeMillis() + 15 * 60_000L, armAlarm?.triggerAtWallClock)
	}

	@Test
	fun `add 5 minutes while running extends end time`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state
		val originalEnd = started.endsAtWallClock

		val extended = TimerEngine.reduce(started, TimerIntent.AddMinutes(5), config, clock)
		assertEquals(originalEnd + 5 * 60_000L, extended.state.endsAtWallClock)
		assertEquals((25 + 5) * 60_000L, extended.state.totalDurationMs)

		val armAlarm = extended.effects.filterIsInstance<TimerSideEffect.ArmAlarm>().firstOrNull()
		assertEquals(originalEnd + 5 * 60_000L, armAlarm?.triggerAtWallClock)
	}

	@Test
	fun `add 5 minutes while paused extends pausedRemainingMs without arming alarm`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state
		clock.advanceBy(20 * 60_000L)
		val paused = TimerEngine.reduce(started, TimerIntent.Pause, config, clock).state

		val extended = TimerEngine.reduce(paused, TimerIntent.AddMinutes(5), config, clock)
		assertEquals(10 * 60_000L, extended.state.pausedRemainingMs)
		assertFalse(extended.effects.any { it is TimerSideEffect.ArmAlarm })
	}

	@Test
	fun `phase completed transitions to short break and logs completed focus session`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS, taskId = 10L), config, clock).state
		clock.advanceBy(25 * 60_000L)

		val completed = TimerEngine.reduce(started, TimerIntent.PhaseCompleted, config, clock)

		assertEquals(Phase.SHORT_BREAK, completed.state.phase)
		assertEquals(1, completed.state.completedInCycle)
		assertTrue(completed.state.isPaused) // autoStartBreaks is false

		val recordEffect = completed.effects.filterIsInstance<TimerSideEffect.RecordCompletedSession>().firstOrNull()
		assertEquals(Phase.FOCUS, recordEffect?.phase)
		assertEquals(25, recordEffect?.durationMinutes)
		assertEquals(10L, recordEffect?.taskId)

		val alertEffect = completed.effects.filterIsInstance<TimerSideEffect.SessionFinishedAlert>().firstOrNull()
		assertEquals(Phase.FOCUS, alertEffect?.completedPhase)
		assertEquals(Phase.SHORT_BREAK, alertEffect?.nextPhase)
	}

	@Test
	fun `fourth completed focus transitions to long break and resets cycle count`() {
		var state = TimerState(completedInCycle = 3)
		val started = TimerEngine.reduce(state, TimerIntent.Start(Phase.FOCUS), config, clock).state
		clock.advanceBy(25 * 60_000L)

		val completed = TimerEngine.reduce(started, TimerIntent.PhaseCompleted, config, clock)

		assertEquals(Phase.LONG_BREAK, completed.state.phase)
		assertEquals(0, completed.state.completedInCycle)
	}

	@Test
	fun `auto start breaks arms alarm immediately on transition`() {
		val autoConfig = config.copy(autoStartBreaks = true)
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), autoConfig, clock).state
		clock.advanceBy(25 * 60_000L)

		val completed = TimerEngine.reduce(started, TimerIntent.PhaseCompleted, autoConfig, clock)

		assertEquals(Phase.SHORT_BREAK, completed.state.phase)
		assertTrue(completed.state.isRunning)
		assertTrue(completed.effects.any { it is TimerSideEffect.ArmAlarm })
	}

	@Test
	fun `reconcile on boot handles expired timer during power off`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state

		// Device turns off and turns back on 30 minutes later (5 minutes past session end)
		clock.advanceBy(30 * 60_000L)

		val reconciled = TimerEngine.reduce(started, TimerIntent.ReconcileOnBoot, config, clock)
		assertEquals(Phase.SHORT_BREAK, reconciled.state.phase)
		assertTrue(reconciled.effects.any { it is TimerSideEffect.RecordCompletedSession })
	}

	@Test
	fun `reconcile on boot restores active countdown if not expired`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state

		// Device reboots after 10 minutes (15 minutes remaining)
		clock.setWallClock(clock.currentTimeMillis() + 10 * 60_000L)
		clock.setRealtime(1_000L) // Realtime reset to 1s after reboot

		val reconciled = TimerEngine.reduce(started, TimerIntent.ReconcileOnBoot, config, clock)
		assertEquals(Phase.FOCUS, reconciled.state.phase)
		assertTrue(reconciled.state.isRunning)
		assertEquals(1_000L + 15 * 60_000L, reconciled.state.endsAtRealtime)

		val armAlarm = reconciled.effects.filterIsInstance<TimerSideEffect.ArmAlarm>().firstOrNull()
		assertEquals(started.endsAtWallClock, armAlarm?.triggerAtWallClock)
	}

	@Test
	fun `stop resets state to IDLE and stops service`() {
		val started = TimerEngine.reduce(TimerState(), TimerIntent.Start(Phase.FOCUS), config, clock).state
		val stopped = TimerEngine.reduce(started, TimerIntent.Stop, config, clock)

		assertEquals(Phase.IDLE, stopped.state.phase)
		assertTrue(stopped.effects.contains(TimerSideEffect.CancelAlarm))
		assertTrue(stopped.effects.contains(TimerSideEffect.StopForegroundService))
	}
}
