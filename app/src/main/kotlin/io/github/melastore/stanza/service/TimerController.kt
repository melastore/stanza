package io.github.melastore.stanza.service

import android.app.NotificationManager
import android.content.Context
import io.github.melastore.stanza.data.datastore.SettingsStore
import io.github.melastore.stanza.data.datastore.TimerStateStore
import io.github.melastore.stanza.data.repository.SessionRepository
import io.github.melastore.stanza.data.repository.TaskRepository
import io.github.melastore.stanza.domain.Clock
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.TimerEngine
import io.github.melastore.stanza.domain.TimerIntent
import io.github.melastore.stanza.domain.TimerSideEffect
import io.github.melastore.stanza.domain.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TimerController(
	private val context: Context,
	val stateStore: TimerStateStore,
	private val settingsStore: SettingsStore,
	private val sessionRepository: SessionRepository,
	private val taskRepository: TaskRepository,
	private val alarmScheduler: AlarmScheduler,
	private val clock: Clock,
	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {

	private val mutex = Mutex()

	fun dispatch(intent: TimerIntent) {
		scope.launch {
			mutex.withLock {
				val config = settingsStore.preferences.first().config
				val currentState = stateStore.get()
				val transition = TimerEngine.reduce(currentState, intent, config, clock)

				stateStore.update { transition.state }
				executeSideEffects(transition.effects, transition.state)
			}
		}
	}

	private suspend fun executeSideEffects(effects: List<TimerSideEffect>, state: TimerState) {
		for (effect in effects) {
			when (effect) {
				is TimerSideEffect.ArmAlarm -> {
					alarmScheduler.schedule(effect.triggerAtWallClock)
				}

				is TimerSideEffect.CancelAlarm -> {
					alarmScheduler.cancel()
				}

				is TimerSideEffect.StartForegroundService -> {
					TimerService.start(context)
				}

				is TimerSideEffect.StopForegroundService -> {
					TimerService.stop(context)
				}

				is TimerSideEffect.UpdateNotification -> {
					if (state.phase != Phase.IDLE) {
						TimerService.update(context)
					}
				}

				is TimerSideEffect.SessionFinishedAlert -> {
					val notification = NotificationFactory.buildAlertNotification(
						context,
						effect.completedPhase,
						effect.nextPhase,
					)
					val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
					manager.notify(NotificationFactory.NOTIFICATION_ID_ALERT, notification)
				}

				is TimerSideEffect.RecordCompletedSession -> {
					sessionRepository.logSession(
						phase = effect.phase,
						durationMinutes = effect.durationMinutes,
						timestamp = effect.timestamp,
						taskId = effect.taskId,
					)
					if (effect.taskId != null) {
						taskRepository.incrementTaskPomodoro(effect.taskId)
					}
				}
			}
		}
	}
}
