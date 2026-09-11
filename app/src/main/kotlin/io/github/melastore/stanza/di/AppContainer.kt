package io.github.melastore.stanza.di

import android.content.Context
import io.github.melastore.stanza.data.datastore.SettingsStore
import io.github.melastore.stanza.data.datastore.TimerStateStore
import io.github.melastore.stanza.data.db.StanzaDatabase
import io.github.melastore.stanza.data.repository.SessionRepository
import io.github.melastore.stanza.data.repository.SqliteSessionRepository
import io.github.melastore.stanza.data.repository.SqliteTaskRepository
import io.github.melastore.stanza.data.repository.TaskRepository
import io.github.melastore.stanza.domain.Clock
import io.github.melastore.stanza.domain.SystemClockImpl
import io.github.melastore.stanza.service.AlarmScheduler
import io.github.melastore.stanza.service.TimerController

/**
 * Manual, lean dependency container for Stanza.
 * Zero reflection, zero annotation processing, fast builds.
 */
class AppContainer(context: Context) {
	val clock: Clock = SystemClockImpl
	val database = StanzaDatabase(context)
	val sessionRepository: SessionRepository = SqliteSessionRepository(database)
	val taskRepository: TaskRepository = SqliteTaskRepository(database)
	val settingsStore = SettingsStore(context)
	val stateStore = TimerStateStore(context)
	val alarmScheduler = AlarmScheduler(context)

	val timerController = TimerController(
		context = context,
		stateStore = stateStore,
		settingsStore = settingsStore,
		sessionRepository = sessionRepository,
		taskRepository = taskRepository,
		alarmScheduler = alarmScheduler,
		clock = clock,
	)
}
