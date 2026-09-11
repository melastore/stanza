package io.github.melastore.stanza.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.data.datastore.UserPreferences
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.theme.FocusPrimary

@Composable
fun SettingsScreen(
	preferences: UserPreferences,
	hazeState: HazeState,
	onUpdateFocusMinutes: (Int) -> Unit,
	onUpdateShortBreakMinutes: (Int) -> Unit,
	onUpdateLongBreakMinutes: (Int) -> Unit,
	onUpdatePomodorosPerCycle: (Int) -> Unit,
	onToggleAutoStartBreaks: (Boolean) -> Unit,
	onToggleAutoStartFocus: (Boolean) -> Unit,
	onToggleReduceTransparency: (Boolean) -> Unit,
	onToggleSound: (Boolean) -> Unit,
	onToggleVibration: (Boolean) -> Unit,
	onToggleKeepScreenOn: (Boolean) -> Unit,
	modifier: Modifier = Modifier,
) {
	LazyColumn(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 20.dp),
		contentPadding = PaddingValues(bottom = 90.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		item {
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = "Settings",
				fontSize = 28.sp,
				fontWeight = FontWeight.Light,
				color = Color.White,
			)
		}

		// Durations Group
		item {
			SectionHeader(title = "DURATIONS")
		}

		item {
			DurationSliderCard(
				title = "Focus Stanza",
				value = preferences.config.focusMinutes,
				range = 5f..90f,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onValueChange = { onUpdateFocusMinutes(it.toInt()) },
			)
		}

		item {
			DurationSliderCard(
				title = "Short Break",
				value = preferences.config.shortBreakMinutes,
				range = 1f..30f,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onValueChange = { onUpdateShortBreakMinutes(it.toInt()) },
			)
		}

		item {
			DurationSliderCard(
				title = "Long Break",
				value = preferences.config.longBreakMinutes,
				range = 5f..60f,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onValueChange = { onUpdateLongBreakMinutes(it.toInt()) },
			)
		}

		item {
			DurationSliderCard(
				title = "Stanzas per Cycle",
				value = preferences.config.pomodorosPerCycle,
				unit = "stanzas",
				range = 2f..8f,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onValueChange = { onUpdatePomodorosPerCycle(it.toInt()) },
			)
		}

		// Flow Automation Group
		item {
			SectionHeader(title = "AUTOMATION")
		}

		item {
			ToggleSettingCard(
				title = "Auto-start Breaks",
				subtitle = "Automatically begin break timer when focus concludes",
				checked = preferences.config.autoStartBreaks,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onCheckedChange = onToggleAutoStartBreaks,
			)
		}

		item {
			ToggleSettingCard(
				title = "Auto-start Focus",
				subtitle = "Automatically begin next stanza when break finishes",
				checked = preferences.config.autoStartFocus,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onCheckedChange = onToggleAutoStartFocus,
			)
		}

		// Craft & Accessibility Group
		item {
			SectionHeader(title = "DISPLAY & SENSORY")
		}

		item {
			ToggleSettingCard(
				title = "Reduce Transparency",
				subtitle = "Use solid surfaces instead of real-time backdrop blur",
				checked = preferences.reduceTransparency,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onCheckedChange = onToggleReduceTransparency,
			)
		}

		item {
			ToggleSettingCard(
				title = "Sound Alerts",
				subtitle = "Play chime when a stanza ends",
				checked = preferences.soundEnabled,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onCheckedChange = onToggleSound,
			)
		}

		item {
			ToggleSettingCard(
				title = "Vibration",
				subtitle = "Haptic pulse on phase transitions",
				checked = preferences.vibrationEnabled,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onCheckedChange = onToggleVibration,
			)
		}

		item {
			ToggleSettingCard(
				title = "Keep Screen On",
				subtitle = "Prevent screen from locking while focus timer is active",
				checked = preferences.keepScreenOn,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onCheckedChange = onToggleKeepScreenOn,
			)
		}
	}
}

@Composable
private fun SectionHeader(title: String) {
	Text(
		text = title,
		fontSize = 11.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 1.5.sp,
		color = Color.White.copy(alpha = 0.45f),
		modifier = Modifier.padding(start = 4.dp, top = 8.dp),
	)
}

@Composable
private fun DurationSliderCard(
	title: String,
	value: Int,
	range: ClosedFloatingPointRange<Float>,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	unit: String = "min",
	onValueChange: (Float) -> Unit,
) {
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CARD,
		shape = RoundedCornerShape(18.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = title,
					fontSize = 15.sp,
					fontWeight = FontWeight.Medium,
					color = Color.White,
				)
				Text(
					text = "$value $unit",
					fontSize = 15.sp,
					fontWeight = FontWeight.SemiBold,
					color = FocusPrimary,
				)
			}
			Slider(
				value = value.toFloat(),
				onValueChange = onValueChange,
				valueRange = range,
				steps = (range.endInclusive - range.start).toInt() - 1,
				colors = SliderDefaults.colors(
					thumbColor = FocusPrimary,
					activeTrackColor = FocusPrimary,
					inactiveTrackColor = Color.White.copy(alpha = 0.1f),
				),
			)
		}
	}
}

@Composable
private fun ToggleSettingCard(
	title: String,
	subtitle: String,
	checked: Boolean,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onCheckedChange: (Boolean) -> Unit,
) {
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CARD,
		shape = RoundedCornerShape(18.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 14.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
				Text(
					text = title,
					fontSize = 15.sp,
					fontWeight = FontWeight.Medium,
					color = Color.White,
				)
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = subtitle,
					fontSize = 12.sp,
					color = Color.White.copy(alpha = 0.45f),
				)
			}
			Switch(
				checked = checked,
				onCheckedChange = onCheckedChange,
				colors = SwitchDefaults.colors(
					checkedThumbColor = Color.White,
					checkedTrackColor = FocusPrimary,
					uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
					uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
				),
			)
		}
	}
}
