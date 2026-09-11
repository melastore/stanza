package io.github.melastore.stanza.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.melastore.stanza.domain.TimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stanza_settings")

data class UserPreferences(
	val config: TimerConfig = TimerConfig(),
	val reduceTransparency: Boolean = false,
	val dndEnabled: Boolean = false,
	val soundEnabled: Boolean = true,
	val vibrationEnabled: Boolean = true,
	val keepScreenOn: Boolean = false,
)

class SettingsStore(private val context: Context) {

	private object Keys {
		val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
		val SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
		val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
		val POMODOROS_PER_CYCLE = intPreferencesKey("pomodoros_per_cycle")
		val AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
		val AUTO_START_FOCUS = booleanPreferencesKey("auto_start_focus")
		val REDUCE_TRANSPARENCY = booleanPreferencesKey("reduce_transparency")
		val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
		val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
		val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
		val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
	}

	val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
		val config = TimerConfig(
			focusMinutes = prefs[Keys.FOCUS_MINUTES] ?: 25,
			shortBreakMinutes = prefs[Keys.SHORT_BREAK_MINUTES] ?: 5,
			longBreakMinutes = prefs[Keys.LONG_BREAK_MINUTES] ?: 15,
			pomodorosPerCycle = prefs[Keys.POMODOROS_PER_CYCLE] ?: 4,
			autoStartBreaks = prefs[Keys.AUTO_START_BREAKS] ?: false,
			autoStartFocus = prefs[Keys.AUTO_START_FOCUS] ?: false,
		)
		UserPreferences(
			config = config,
			reduceTransparency = prefs[Keys.REDUCE_TRANSPARENCY] ?: false,
			dndEnabled = prefs[Keys.DND_ENABLED] ?: false,
			soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
			vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
			keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false,
		)
	}

	suspend fun updateFocusMinutes(minutes: Int) {
		context.dataStore.edit { it[Keys.FOCUS_MINUTES] = minutes.coerceIn(1, 120) }
	}

	suspend fun updateShortBreakMinutes(minutes: Int) {
		context.dataStore.edit { it[Keys.SHORT_BREAK_MINUTES] = minutes.coerceIn(1, 60) }
	}

	suspend fun updateLongBreakMinutes(minutes: Int) {
		context.dataStore.edit { it[Keys.LONG_BREAK_MINUTES] = minutes.coerceIn(1, 60) }
	}

	suspend fun updatePomodorosPerCycle(count: Int) {
		context.dataStore.edit { it[Keys.POMODOROS_PER_CYCLE] = count.coerceIn(1, 12) }
	}

	suspend fun setAutoStartBreaks(enabled: Boolean) {
		context.dataStore.edit { it[Keys.AUTO_START_BREAKS] = enabled }
	}

	suspend fun setAutoStartFocus(enabled: Boolean) {
		context.dataStore.edit { it[Keys.AUTO_START_FOCUS] = enabled }
	}

	suspend fun setReduceTransparency(enabled: Boolean) {
		context.dataStore.edit { it[Keys.REDUCE_TRANSPARENCY] = enabled }
	}

	suspend fun setDndEnabled(enabled: Boolean) {
		context.dataStore.edit { it[Keys.DND_ENABLED] = enabled }
	}

	suspend fun setSoundEnabled(enabled: Boolean) {
		context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
	}

	suspend fun setVibrationEnabled(enabled: Boolean) {
		context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
	}

	suspend fun setKeepScreenOn(enabled: Boolean) {
		context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
	}
}
