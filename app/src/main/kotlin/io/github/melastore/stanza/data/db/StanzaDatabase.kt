package io.github.melastore.stanza.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.github.melastore.stanza.domain.Phase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SessionRecord(
	val id: Long,
	val phase: Phase,
	val durationMinutes: Int,
	val timestamp: Long,
	val taskId: Long?,
)

data class TaskRecord(
	val id: Long,
	val title: String,
	val estimatedPomodoros: Int,
	val completedPomodoros: Int,
	val isCompleted: Boolean,
	val createdAt: Long,
)

// Hand-rolled SQLiteOpenHelper. Sessions are indexed by day for the heatmap and streak queries.
class StanzaDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

	private val _tasksFlow = MutableStateFlow<List<TaskRecord>>(emptyList())
	val tasksFlow: Flow<List<TaskRecord>> = _tasksFlow.asStateFlow()

	init {
		setWriteAheadLoggingEnabled(true)
		refreshTasksSync()
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(
			"""
			CREATE TABLE sessions (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				phase TEXT NOT NULL,
				duration_minutes INTEGER NOT NULL,
				timestamp INTEGER NOT NULL,
				task_id INTEGER
			);
			""".trimIndent(),
		)
		db.execSQL("CREATE INDEX idx_sessions_timestamp ON sessions(timestamp);")

		db.execSQL(
			"""
			CREATE TABLE tasks (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				title TEXT NOT NULL,
				estimated_pomodoros INTEGER NOT NULL DEFAULT 1,
				completed_pomodoros INTEGER NOT NULL DEFAULT 0,
				is_completed INTEGER NOT NULL DEFAULT 0,
				created_at INTEGER NOT NULL
			);
			""".trimIndent(),
		)
		db.execSQL("CREATE INDEX idx_tasks_created ON tasks(created_at);")
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
	}

	suspend fun insertSession(phase: Phase, durationMinutes: Int, timestamp: Long, taskId: Long?,): Long =
		withContext(Dispatchers.IO) {
			val values = ContentValues().apply {
				put("phase", phase.name)
				put("duration_minutes", durationMinutes)
				put("timestamp", timestamp)
				put("task_id", taskId)
			}
			writableDatabase.insert("sessions", null, values)
		}

	suspend fun getTodayFocusMinutes(todayStartMillis: Long): Int = withContext(Dispatchers.IO) {
		val cursor = readableDatabase.rawQuery(
			"""
			SELECT COALESCE(SUM(duration_minutes), 0)
			FROM sessions
			WHERE phase = ? AND timestamp >= ?
			""".trimIndent(),
			arrayOf(Phase.FOCUS.name, todayStartMillis.toString()),
		)
		cursor.use {
			if (it.moveToFirst()) it.getInt(0) else 0
		}
	}

	suspend fun getWeekFocusMinutes(weekStartMillis: Long): Int = withContext(Dispatchers.IO) {
		val cursor = readableDatabase.rawQuery(
			"""
			SELECT COALESCE(SUM(duration_minutes), 0)
			FROM sessions
			WHERE phase = ? AND timestamp >= ?
			""".trimIndent(),
			arrayOf(Phase.FOCUS.name, weekStartMillis.toString()),
		)
		cursor.use {
			if (it.moveToFirst()) it.getInt(0) else 0
		}
	}

	suspend fun getYearHeatmap(sinceMillis: Long): Map<String, Int> = withContext(Dispatchers.IO) {
		val cursor = readableDatabase.rawQuery(
			"""
			SELECT date(timestamp / 1000, 'unixepoch', 'localtime') AS day,
				   SUM(duration_minutes) AS total_minutes
			FROM sessions
			WHERE phase = ? AND timestamp >= ?
			GROUP BY day
			ORDER BY day ASC
			""".trimIndent(),
			arrayOf(Phase.FOCUS.name, sinceMillis.toString()),
		)
		val heatmap = mutableMapOf<String, Int>()
		cursor.use {
			while (it.moveToNext()) {
				val day = it.getString(0) ?: continue
				val minutes = it.getInt(1)
				heatmap[day] = minutes
			}
		}
		heatmap
	}

	suspend fun getRecentSessions(limit: Int = 30): List<SessionRecord> = withContext(Dispatchers.IO) {
		val cursor = readableDatabase.rawQuery(
			"""
			SELECT id, phase, duration_minutes, timestamp, task_id
			FROM sessions
			ORDER BY timestamp DESC
			LIMIT ?
			""".trimIndent(),
			arrayOf(limit.toString()),
		)
		val list = mutableListOf<SessionRecord>()
		cursor.use {
			while (it.moveToNext()) {
				list.add(
					SessionRecord(
						id = it.getLong(0),
						phase = Phase.valueOf(it.getString(1)),
						durationMinutes = it.getInt(2),
						timestamp = it.getLong(3),
						taskId = if (it.isNull(4)) null else it.getLong(4),
					),
				)
			}
		}
		list
	}

	suspend fun calculateStreak(): Int = withContext(Dispatchers.IO) {
		val cursor = readableDatabase.rawQuery(
			"""
			SELECT DISTINCT date(timestamp / 1000, 'unixepoch', 'localtime') AS day
			FROM sessions
			WHERE phase = ?
			ORDER BY day DESC
			""".trimIndent(),
			arrayOf(Phase.FOCUS.name),
		)

		val days = mutableListOf<LocalDate>()
		val formatter = DateTimeFormatter.ISO_LOCAL_DATE
		cursor.use {
			while (it.moveToNext()) {
				val str = it.getString(0) ?: continue
				try {
					days.add(LocalDate.parse(str, formatter))
				} catch (_: Exception) {
				}
			}
		}

		if (days.isEmpty()) return@withContext 0

		val today = LocalDate.now(ZoneId.systemDefault())
		val yesterday = today.minusDays(1)

		val firstDay = days.first()
		if (firstDay != today && firstDay != yesterday) {
			return@withContext 0
		}

		var streak = 1
		var expected = firstDay.minusDays(1)

		for (i in 1 until days.size) {
			val day = days[i]
			if (day == expected) {
				streak++
				expected = expected.minusDays(1)
			} else {
				break
			}
		}
		streak
	}

	suspend fun insertTask(title: String, estimatedPomodoros: Int): Long = withContext(Dispatchers.IO) {
		val values = ContentValues().apply {
			put("title", title.trim())
			put("estimated_pomodoros", estimatedPomodoros.coerceAtLeast(1))
			put("completed_pomodoros", 0)
			put("is_completed", 0)
			put("created_at", System.currentTimeMillis())
		}
		val id = writableDatabase.insert("tasks", null, values)
		refreshTasksSync()
		id
	}

	suspend fun toggleTaskComplete(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
		val values = ContentValues().apply {
			put("is_completed", if (completed) 1 else 0)
		}
		writableDatabase.update("tasks", values, "id = ?", arrayOf(id.toString()))
		refreshTasksSync()
	}

	suspend fun incrementTaskPomodoro(id: Long) = withContext(Dispatchers.IO) {
		writableDatabase.execSQL(
			"UPDATE tasks SET completed_pomodoros = completed_pomodoros + 1 WHERE id = ?",
			arrayOf(id.toString()),
		)
		refreshTasksSync()
	}

	suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
		writableDatabase.delete("tasks", "id = ?", arrayOf(id.toString()))
		refreshTasksSync()
	}

	private fun refreshTasksSync() {
		val cursor = readableDatabase.rawQuery(
			"""
			SELECT id, title, estimated_pomodoros, completed_pomodoros, is_completed, created_at
			FROM tasks
			ORDER BY is_completed ASC, created_at DESC
			""".trimIndent(),
			null,
		)
		val list = mutableListOf<TaskRecord>()
		cursor.use {
			while (it.moveToNext()) {
				list.add(
					TaskRecord(
						id = it.getLong(0),
						title = it.getString(1),
						estimatedPomodoros = it.getInt(2),
						completedPomodoros = it.getInt(3),
						isCompleted = it.getInt(4) == 1,
						createdAt = it.getLong(5),
					),
				)
			}
		}
		_tasksFlow.value = list
	}

	companion object {
		private const val DATABASE_NAME = "stanza.db"
		private const val DATABASE_VERSION = 1
	}
}
