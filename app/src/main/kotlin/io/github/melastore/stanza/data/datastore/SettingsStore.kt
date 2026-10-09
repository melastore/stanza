package io.github.melastore.stanza.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.melastore.stanza.domain.TimerConfig
import io.github.melastore.stanza.domain.TimerLayout
import io.github.melastore.stanza.ui.theme.DEFAULT_CUSTOM_HUE
import io.github.melastore.stanza.ui.theme.DEFAULT_THEME_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stanza_settings")

data class UserPreferences(
	val config: TimerConfig = TimerConfig(),
	val reduceTransparency: Boolean = false,
	val soundEnabled: Boolean = true,
	val vibrationEnabled: Boolean = true,
	val keepScreenOn: Boolean = false,
	val themeId: String = DEFAULT_THEME_ID,
	val customHue: Float = DEFAULT_CUSTOM_HUE,
	val layout: TimerLayout = TimerLayout(),
	val permissionPromptSeen: Boolean = false,
)

class SettingsStore(private val context: Context) {

	private val json = Json { ignoreUnknownKeys = true }

	private object Keys {
		val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
		val SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
		val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
		val POMODOROS_PER_CYCLE = intPreferencesKey("pomodoros_per_cycle")
		val AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks")
		val AUTO_START_FOCUS = booleanPreferencesKey("auto_start_focus")
		val REDUCE_TRANSPARENCY = booleanPreferencesKey("reduce_transparency")
		val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
		val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
		val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
		val THEME_ID = stringPreferencesKey("theme_id")
		val CUSTOM_HUE = floatPreferencesKey("custom_hue")
		val LAYOUT = stringPreferencesKey("timer_layout")
		val PERMISSION_PROMPT_SEEN = booleanPreferencesKey("permission_prompt_seen")
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
			soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
			vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
			keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false,
			themeId = prefs[Keys.THEME_ID] ?: DEFAULT_THEME_ID,
			customHue = prefs[Keys.CUSTOM_HUE] ?: DEFAULT_CUSTOM_HUE,
			layout = prefs[Keys.LAYOUT]?.let { stored ->
				runCatching { json.decodeFromString(TimerLayout.serializer(), stored) }.getOrNull()
			} ?: TimerLayout(),
			permissionPromptSeen = prefs[Keys.PERMISSION_PROMPT_SEEN] ?: false,
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

	suspend fun setSoundEnabled(enabled: Boolean) {
		context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
	}

	suspend fun setVibrationEnabled(enabled: Boolean) {
		context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
	}

	suspend fun setKeepScreenOn(enabled: Boolean) {
		context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
	}

	suspend fun setThemeId(id: String) {
		context.dataStore.edit { it[Keys.THEME_ID] = id }
	}

	suspend fun setPermissionPromptSeen() {
		context.dataStore.edit { it[Keys.PERMISSION_PROMPT_SEEN] = true }
	}

	suspend fun setLayout(layout: TimerLayout) {
		val encoded = json.encodeToString(TimerLayout.serializer(), layout)
		context.dataStore.edit { it[Keys.LAYOUT] = encoded }
	}

	suspend fun setCustomHue(hue: Float) {
		context.dataStore.edit { it[Keys.CUSTOM_HUE] = ((hue % 360f) + 360f) % 360f }
	}
}
