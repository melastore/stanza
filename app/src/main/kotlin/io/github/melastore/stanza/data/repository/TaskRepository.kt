package io.github.melastore.stanza.data.repository

import io.github.melastore.stanza.data.db.StanzaDatabase
import io.github.melastore.stanza.data.db.TaskRecord
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
	val tasks: Flow<List<TaskRecord>>
	suspend fun createTask(title: String, estimatedPomodoros: Int): Long
	suspend fun toggleTaskComplete(id: Long, completed: Boolean)
	suspend fun incrementTaskPomodoro(id: Long)
	suspend fun deleteTask(id: Long)
}

class SqliteTaskRepository(private val db: StanzaDatabase) : TaskRepository {
	override val tasks: Flow<List<TaskRecord>> = db.tasksFlow

	override suspend fun createTask(title: String, estimatedPomodoros: Int): Long =
		db.insertTask(title, estimatedPomodoros)

	override suspend fun toggleTaskComplete(id: Long, completed: Boolean) = db.toggleTaskComplete(id, completed)

	override suspend fun incrementTaskPomodoro(id: Long) = db.incrementTaskPomodoro(id)

	override suspend fun deleteTask(id: Long) = db.deleteTask(id)
}
