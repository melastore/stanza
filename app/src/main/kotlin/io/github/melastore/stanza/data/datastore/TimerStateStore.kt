package io.github.melastore.stanza.data.datastore

import android.content.Context
import android.system.Os
import android.system.OsConstants
import io.github.melastore.stanza.domain.TimerState
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

// Write is temp file -> fsync -> rename, with a backup generation, so a kill mid-write can't
// leave a half-written state behind.
class TimerStateStore(context: Context) {

	private val file = File(context.filesDir, "timer_state.json")
	private val backup = File(context.filesDir, "timer_state.json.bak")
	private val json = Json { ignoreUnknownKeys = true }
	private val mutex = Mutex()

	private val _state = MutableStateFlow(loadInitial())
	val state: StateFlow<TimerState> = _state.asStateFlow()

	suspend fun update(transform: (TimerState) -> TimerState): TimerState = mutex.withLock {
		val next = transform(_state.value)
		if (next == _state.value) return next

		withContext(Dispatchers.IO) {
			persist(next)
		}
		_state.value = next
		next
	}

	fun get(): TimerState = _state.value

	private fun loadInitial(): TimerState {
		parse(file)?.let { return it }
		parse(backup)?.let { return it }
		return TimerState()
	}

	private fun persist(state: TimerState) {
		val temp = File(file.parentFile, "${file.name}.tmp")
		val serialized = json.encodeToString(TimerState.serializer(), state)
		temp.writeSynced(serialized)

		parse(file)?.let {
			backup.writeSynced(json.encodeToString(TimerState.serializer(), it))
		}

		if (temp.renameTo(file)) {
			syncParentDirectory()
		}
	}

	private fun parse(target: File): TimerState? {
		if (!target.isFile || target.length() == 0L) return null
		return runCatching {
			json.decodeFromString(TimerState.serializer(), target.readText())
		}.getOrNull()
	}

	private fun File.writeSynced(text: String) {
		FileOutputStream(this).use { out ->
			out.write(text.toByteArray())
			out.fd.sync()
		}
	}

	private fun syncParentDirectory() {
		val dir = file.parentFile ?: return
		runCatching {
			val fd = Os.open(dir.path, OsConstants.O_RDONLY, 0)
			try {
				Os.fsync(fd)
			} finally {
				Os.close(fd)
			}
		}
	}
}
