package io.github.melastore.stanza.domain

// No Android imports here, keeps it testable on the JVM.
object TimerEngine {

	fun reduce(state: TimerState, intent: TimerIntent, config: TimerConfig, clock: Clock,): TimerTransition =
		when (intent) {
			is TimerIntent.Start -> handleStart(state, intent, config, clock)
			is TimerIntent.Pause -> handlePause(state, clock)
			is TimerIntent.Resume -> handleResume(state, clock)
			is TimerIntent.Skip -> handleSkip(state, config, clock)
			is TimerIntent.AddMinutes -> handleAddMinutes(state, intent.minutes)
			is TimerIntent.PhaseCompleted -> handlePhaseCompleted(state, config, clock)
			is TimerIntent.Stop -> handleStop(state)
			is TimerIntent.ReconcileOnBoot -> handleReconcileOnBoot(state, config, clock)
		}

	private fun handleStart(
		state: TimerState,
		intent: TimerIntent.Start,
		config: TimerConfig,
		clock: Clock,
	): TimerTransition {
		val durationMs = config.durationMsFor(intent.phase)
		val endsAtRealtime = clock.elapsedRealtime() + durationMs
		val endsAtWallClock = clock.currentTimeMillis() + durationMs
		val taskId = intent.taskId ?: state.currentTaskId

		val nextState = TimerState(
			phase = intent.phase,
			endsAtRealtime = endsAtRealtime,
			endsAtWallClock = endsAtWallClock,
			pausedRemainingMs = null,
			completedInCycle = state.completedInCycle,
			currentTaskId = taskId,
			totalDurationMs = durationMs,
		)

		val effects = listOf(
			TimerSideEffect.ArmAlarm(endsAtWallClock),
			TimerSideEffect.StartForegroundService,
			TimerSideEffect.UpdateNotification,
		)
		return TimerTransition(nextState, effects)
	}

	private fun handlePause(state: TimerState, clock: Clock): TimerTransition {
		if (!state.isRunning) return TimerTransition(state)

		val remaining = state.remainingMs(clock.elapsedRealtime())
		val nextState = state.copy(pausedRemainingMs = remaining)
		val effects = listOf(
			TimerSideEffect.CancelAlarm,
			TimerSideEffect.UpdateNotification,
		)
		return TimerTransition(nextState, effects)
	}

	private fun handleResume(state: TimerState, clock: Clock): TimerTransition {
		if (!state.isPaused) return TimerTransition(state)

		val remaining = state.pausedRemainingMs ?: return TimerTransition(state)
		val endsAtRealtime = clock.elapsedRealtime() + remaining
		val endsAtWallClock = clock.currentTimeMillis() + remaining

		val nextState = state.copy(
			endsAtRealtime = endsAtRealtime,
			endsAtWallClock = endsAtWallClock,
			pausedRemainingMs = null,
		)
		val effects = listOf(
			TimerSideEffect.ArmAlarm(endsAtWallClock),
			TimerSideEffect.StartForegroundService,
			TimerSideEffect.UpdateNotification,
		)
		return TimerTransition(nextState, effects)
	}

	private fun handleSkip(state: TimerState, config: TimerConfig, clock: Clock,): TimerTransition {
		if (state.phase == Phase.IDLE) return TimerTransition(state)
		val (nextPhase, nextCycleCount) = computeNextPhase(state, config)
		return advanceToPhase(nextPhase, nextCycleCount, state.currentTaskId, config, clock)
	}

	private fun handleAddMinutes(state: TimerState, minutes: Int): TimerTransition {
		if (state.phase == Phase.IDLE || minutes <= 0) return TimerTransition(state)

		val addMs = minutes * 60_000L
		val nextTotalDuration = state.totalDurationMs + addMs

		return if (state.isPaused) {
			val currentRemaining = state.pausedRemainingMs ?: 0L
			val nextState = state.copy(
				pausedRemainingMs = currentRemaining + addMs,
				totalDurationMs = nextTotalDuration,
			)
			TimerTransition(nextState, listOf(TimerSideEffect.UpdateNotification))
		} else {
			val nextEndsRealtime = state.endsAtRealtime + addMs
			val nextEndsWallClock = state.endsAtWallClock + addMs
			val nextState = state.copy(
				endsAtRealtime = nextEndsRealtime,
				endsAtWallClock = nextEndsWallClock,
				totalDurationMs = nextTotalDuration,
			)
			val effects = listOf(
				TimerSideEffect.ArmAlarm(nextEndsWallClock),
				TimerSideEffect.UpdateNotification,
			)
			TimerTransition(nextState, effects)
		}
	}

	private fun handlePhaseCompleted(state: TimerState, config: TimerConfig, clock: Clock,): TimerTransition {
		if (state.phase == Phase.IDLE) return TimerTransition(state)

		val completedPhase = state.phase
		val (nextPhase, nextCycleCount) = computeNextPhase(state, config)
		val transition = advanceToPhase(nextPhase, nextCycleCount, state.currentTaskId, config, clock)

		val alertEffects = mutableListOf<TimerSideEffect>(
			TimerSideEffect.SessionFinishedAlert(
				completedPhase = completedPhase,
				nextPhase = nextPhase,
				taskId = state.currentTaskId,
			),
		)

		if (completedPhase == Phase.FOCUS) {
			val durationMinutes = (state.totalDurationMs / 60_000L).toInt().coerceAtLeast(1)
			alertEffects.add(
				TimerSideEffect.RecordCompletedSession(
					phase = Phase.FOCUS,
					durationMinutes = durationMinutes,
					timestamp = clock.currentTimeMillis(),
					taskId = state.currentTaskId,
				),
			)
		}

		return transition.copy(effects = alertEffects + transition.effects)
	}

	private fun handleStop(state: TimerState): TimerTransition {
		val nextState = TimerState(
			phase = Phase.IDLE,
			completedInCycle = state.completedInCycle,
			currentTaskId = state.currentTaskId,
		)
		val effects = listOf(
			TimerSideEffect.CancelAlarm,
			TimerSideEffect.StopForegroundService,
			TimerSideEffect.UpdateNotification,
		)
		return TimerTransition(nextState, effects)
	}

	private fun handleReconcileOnBoot(state: TimerState, config: TimerConfig, clock: Clock,): TimerTransition {
		if (state.phase == Phase.IDLE || state.isPaused) {
			return TimerTransition(state)
		}

		val nowWall = clock.currentTimeMillis()
		return if (nowWall >= state.endsAtWallClock) {
			// Ended while the phone was off
			handlePhaseCompleted(state, config, clock)
		} else {
			// Still running, elapsedRealtime reset on boot so re-anchor it
			val remainingMs = state.endsAtWallClock - nowWall
			val restoredEndsRealtime = clock.elapsedRealtime() + remainingMs
			val nextState = state.copy(endsAtRealtime = restoredEndsRealtime)
			val effects = listOf(
				TimerSideEffect.ArmAlarm(state.endsAtWallClock),
				TimerSideEffect.StartForegroundService,
				TimerSideEffect.UpdateNotification,
			)
			TimerTransition(nextState, effects)
		}
	}

	private fun computeNextPhase(state: TimerState, config: TimerConfig): Pair<Phase, Int> =
		if (state.phase == Phase.FOCUS) {
			val nextCount = state.completedInCycle + 1
			if (nextCount >= config.pomodorosPerCycle) {
				Phase.LONG_BREAK to 0
			} else {
				Phase.SHORT_BREAK to nextCount
			}
		} else {
			Phase.FOCUS to state.completedInCycle
		}

	private fun advanceToPhase(
		nextPhase: Phase,
		nextCycleCount: Int,
		taskId: Long?,
		config: TimerConfig,
		clock: Clock,
	): TimerTransition {
		val durationMs = config.durationMsFor(nextPhase)
		val shouldAutoStart = if (nextPhase.isBreak) config.autoStartBreaks else config.autoStartFocus

		return if (shouldAutoStart) {
			val endsAtRealtime = clock.elapsedRealtime() + durationMs
			val endsAtWallClock = clock.currentTimeMillis() + durationMs
			val nextState = TimerState(
				phase = nextPhase,
				endsAtRealtime = endsAtRealtime,
				endsAtWallClock = endsAtWallClock,
				pausedRemainingMs = null,
				completedInCycle = nextCycleCount,
				currentTaskId = taskId,
				totalDurationMs = durationMs,
			)
			val effects = listOf(
				TimerSideEffect.ArmAlarm(endsAtWallClock),
				TimerSideEffect.UpdateNotification,
			)
			TimerTransition(nextState, effects)
		} else {
			val nextState = TimerState(
				phase = nextPhase,
				endsAtRealtime = 0L,
				endsAtWallClock = 0L,
				pausedRemainingMs = durationMs,
				completedInCycle = nextCycleCount,
				currentTaskId = taskId,
				totalDurationMs = durationMs,
			)
			val effects = listOf(
				TimerSideEffect.CancelAlarm,
				TimerSideEffect.UpdateNotification,
			)
			TimerTransition(nextState, effects)
		}
	}
}
