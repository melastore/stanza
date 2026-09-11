package io.github.melastore.stanza.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.data.datastore.UserPreferences
import io.github.melastore.stanza.domain.BlockAlign
import io.github.melastore.stanza.domain.BlockGravity
import io.github.melastore.stanza.domain.DigitStyle
import io.github.melastore.stanza.domain.ProgressStyle
import io.github.melastore.stanza.domain.TimerLayout
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.permissions.PermissionStatusList
import io.github.melastore.stanza.ui.permissions.rememberPermissionStatus
import io.github.melastore.stanza.ui.theme.CUSTOM_THEME_ID
import io.github.melastore.stanza.ui.theme.LocalPalette
import io.github.melastore.stanza.ui.theme.StanzaPalettes
import io.github.melastore.stanza.ui.theme.paletteFor
import io.github.melastore.stanza.ui.theme.paletteFromHue

@Composable
fun SettingsScreen(
	preferences: UserPreferences,
	hazeState: HazeState,
	onSelectTheme: (String) -> Unit,
	onUpdateLayout: (TimerLayout) -> Unit,
	onUpdateCustomHue: (Float) -> Unit,
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

		item {
			SectionHeader(title = "PERMISSIONS")
		}

		item {
			GlassSurface(
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				variant = GlassVariant.CARD,
				shape = RoundedCornerShape(20.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 2.dp)) {
					PermissionStatusList(
						status = rememberPermissionStatus(),
						accent = LocalPalette.current.focusPrimary,
					)
				}
			}
		}

		item {
			SectionHeader(title = "LAYOUT")
		}

		item {
			LayoutPresetRow(
				layout = preferences.layout,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onSelect = onUpdateLayout,
			)
		}

		item {
			LayoutDetailCard(
				layout = preferences.layout,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onUpdateLayout = onUpdateLayout,
			)
		}

		item {
			SectionHeader(title = "THEME")
		}

		item {
			ThemeCard(
				selectedId = preferences.themeId,
				customHue = preferences.customHue,
				hazeState = hazeState,
				reduceTransparency = preferences.reduceTransparency,
				onSelectTheme = onSelectTheme,
				onUpdateCustomHue = onUpdateCustomHue,
			)
		}

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
	val palette = LocalPalette.current
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
					color = palette.focusPrimary,
				)
			}
			Slider(
				value = value.toFloat(),
				onValueChange = onValueChange,
				valueRange = range,
				steps = (range.endInclusive - range.start).toInt() - 1,
				colors = SliderDefaults.colors(
					thumbColor = palette.focusPrimary,
					activeTrackColor = palette.focusPrimary,
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
	val palette = LocalPalette.current
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
					checkedTrackColor = palette.focusPrimary,
					uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
					uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
				),
			)
		}
	}
}

@Composable
private fun ThemeCard(
	selectedId: String,
	customHue: Float,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onSelectTheme: (String) -> Unit,
	onUpdateCustomHue: (Float) -> Unit,
) {
	val palette = LocalPalette.current
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CARD,
		shape = RoundedCornerShape(20.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(modifier = Modifier.padding(18.dp)) {
			Text(
				text = if (selectedId == CUSTOM_THEME_ID) "Custom" else paletteFor(selectedId, customHue).name,
				fontSize = 15.sp,
				fontWeight = FontWeight.SemiBold,
				color = Color.White,
			)
			Text(
				text = "Recolours the whole app, backdrop and glass included",
				fontSize = 12.sp,
				color = Color.White.copy(alpha = 0.55f),
			)

			Spacer(modifier = Modifier.height(16.dp))

			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				StanzaPalettes.forEach { option ->
					Swatch(
						focus = option.focusPrimary,
						breakColor = option.breakPrimary,
						selected = option.id == selectedId,
						onClick = { onSelectTheme(option.id) },
					)
				}
				Swatch(
					focus = paletteFromHue(customHue).focusPrimary,
					breakColor = paletteFromHue(customHue).breakPrimary,
					selected = selectedId == CUSTOM_THEME_ID,
					anyColour = true,
					onClick = { onSelectTheme(CUSTOM_THEME_ID) },
				)
			}

			if (selectedId == CUSTOM_THEME_ID) {
				Spacer(modifier = Modifier.height(18.dp))
				Text(
					text = "Hue",
					fontSize = 12.sp,
					fontWeight = FontWeight.Medium,
					color = Color.White.copy(alpha = 0.7f),
				)
				Slider(
					value = customHue,
					onValueChange = onUpdateCustomHue,
					valueRange = 0f..359f,
					colors = SliderDefaults.colors(
						thumbColor = palette.focusPrimary,
						activeTrackColor = palette.focusPrimary,
						inactiveTrackColor = Color.White.copy(alpha = 0.12f),
					),
				)
				// Hue wheel behind the slider thumb.
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(8.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(
							Brush.horizontalGradient(
								(0..6).map { paletteFromHue(it * 60f).focusPrimary },
							),
						),
				)
			}
		}
	}
}

@Composable
private fun Swatch(
	focus: Color,
	breakColor: Color,
	selected: Boolean,
	onClick: () -> Unit,
	anyColour: Boolean = false,
) {
	// Full hue wheel on the custom swatch so it doesn't look like another preset.
	val brush = if (anyColour) {
		Brush.sweepGradient((0..6).map { paletteFromHue(it * 60f).focusPrimary })
	} else {
		Brush.linearGradient(listOf(focus, breakColor))
	}
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.size(38.dp)
			.clip(CircleShape)
			.background(if (selected) Color.White.copy(alpha = 0.18f) else Color.Transparent)
			.clickable { onClick() },
	) {
		Box(
			modifier = Modifier
				.size(if (selected) 28.dp else 24.dp)
				.clip(CircleShape)
				.background(brush),
		)
	}
}

@Composable
private fun LayoutPresetRow(
	layout: TimerLayout,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onSelect: (TimerLayout) -> Unit,
) {
	val palette = LocalPalette.current
	Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
		TimerLayout.presets.chunked(3).forEach { row ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				row.forEach { preset ->
					val selected = preset.presetId == layout.presetId
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier.weight(1f),
					) {
						GlassSurface(
							hazeState = hazeState,
							reduceTransparency = reduceTransparency,
							variant = GlassVariant.CARD,
							shape = RoundedCornerShape(14.dp),
							borderWidth = if (selected) 2.dp else 1.dp,
							modifier = Modifier
								.fillMaxWidth()
								.height(120.dp)
								.clip(RoundedCornerShape(14.dp))
								.clickable { onSelect(preset) },
						) {
							LayoutThumbnail(
								preset = preset,
								accent = if (selected) palette.focusPrimary else Color.White.copy(alpha = 0.35f),
							)
						}
						Spacer(modifier = Modifier.height(6.dp))
						Text(
							text = TimerLayout.labelFor(preset.presetId),
							fontSize = 11.sp,
							fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
							color = if (selected) palette.focusPrimary else Color.White.copy(alpha = 0.55f),
						)
					}
				}
				repeat(3 - row.size) {
					Spacer(modifier = Modifier.weight(1f))
				}
			}
		}
		if (layout.presetId == TimerLayout.CUSTOM) {
			Text(
				text = "Custom layout",
				fontSize = 12.sp,
				color = palette.focusPrimary,
			)
		}
	}
}

// Small preview of a layout preset.
@Composable
private fun LayoutThumbnail(preset: TimerLayout, accent: Color) {
	val dim = Color.White.copy(alpha = 0.22f)
	val arrangement = when (preset.gravity) {
		BlockGravity.TOP -> Arrangement.Top
		BlockGravity.BOTTOM -> Arrangement.Bottom
		BlockGravity.CENTER -> Arrangement.Center
	}
	val alignment = when (preset.align) {
		BlockAlign.START -> Alignment.Start
		BlockAlign.CENTER -> Alignment.CenterHorizontally
		BlockAlign.END -> Alignment.End
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(10.dp),
		verticalArrangement = arrangement,
		horizontalAlignment = alignment,
	) {
		if (preset.showTask) {
			Box(
				modifier = Modifier
					.size(width = 26.dp, height = 5.dp)
					.clip(RoundedCornerShape(3.dp))
					.background(dim),
			)
			Spacer(modifier = Modifier.height(8.dp))
		}

		if (preset.progress == ProgressStyle.RING) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.size(34.dp),
			) {
				Canvas(modifier = Modifier.fillMaxSize()) {
					drawCircle(
						color = accent,
						radius = size.minDimension / 2 - 1.dp.toPx(),
						style = Stroke(width = 2.dp.toPx()),
					)
				}
				Box(
					modifier = Modifier
						.size(width = 16.dp, height = 8.dp)
						.clip(RoundedCornerShape(2.dp))
						.background(accent),
				)
			}
		} else if (preset.digits == DigitStyle.STACKED) {
			Box(
				modifier = Modifier
					.size(width = 22.dp, height = 11.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(accent),
			)
			Spacer(modifier = Modifier.height(3.dp))
			Box(
				modifier = Modifier
					.size(width = 22.dp, height = 11.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(accent.copy(alpha = 0.55f)),
			)
		} else {
			Box(
				modifier = Modifier
					.size(width = 46.dp, height = 13.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(accent),
			)
		}

		if (preset.progress == ProgressStyle.BAR) {
			Spacer(modifier = Modifier.height(6.dp))
			Box(
				modifier = Modifier
					.size(width = 46.dp, height = 3.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(accent.copy(alpha = 0.6f)),
			)
		}

		if (preset.showStanzaLines) {
			Spacer(modifier = Modifier.height(7.dp))
			repeat(3) { i ->
				Box(
					modifier = Modifier
						.size(width = (22 - i * 5).dp, height = 2.dp)
						.clip(RoundedCornerShape(1.dp))
						.background(dim),
				)
				Spacer(modifier = Modifier.height(3.dp))
			}
		}
	}
}

@Composable
private fun LayoutDetailCard(
	layout: TimerLayout,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onUpdateLayout: (TimerLayout) -> Unit,
) {
	// Any manual change makes it custom.
	fun apply(change: TimerLayout.() -> TimerLayout) {
		val next = layout.change()
		val matched = TimerLayout.presets.firstOrNull { it == next.copy(presetId = it.presetId) }
		onUpdateLayout(next.copy(presetId = matched?.presetId ?: TimerLayout.CUSTOM))
	}

	var expanded by remember { mutableStateOf(false) }
	val palette = LocalPalette.current
	val chevron by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
	val summary = listOf(
		if (layout.digits == DigitStyle.SINGLE_LINE) "One line" else "Stacked",
		when (layout.align) {
			BlockAlign.START -> "Left"
			BlockAlign.CENTER -> "Centre"
			BlockAlign.END -> "Right"
		},
		when (layout.progress) {
			ProgressStyle.PERIMETER -> "Edge"
			ProgressStyle.RING -> "Ring"
			ProgressStyle.BAR -> "Bar"
			ProgressStyle.NONE -> "No progress"
		},
	).joinToString(" · ")

	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CARD,
		shape = RoundedCornerShape(20.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(modifier = Modifier.animateContentSize()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { expanded = !expanded }
					.padding(18.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically,
			) {
				Column {
					Text(
						text = "Customise",
						fontSize = 15.sp,
						fontWeight = FontWeight.SemiBold,
						color = Color.White,
					)
					Text(
						text = summary,
						fontSize = 12.sp,
						color = Color.White.copy(alpha = 0.55f),
					)
				}
				Icon(
					imageVector = Icons.Default.ExpandMore,
					contentDescription = if (expanded) "Collapse" else "Expand",
					tint = palette.focusPrimary,
					modifier = Modifier
						.size(22.dp)
						.rotate(chevron),
				)
			}

			if (!expanded) return@Column

			Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp)) {
				ChoiceRow(
					label = "Clock",
					options = listOf("One line", "Stacked"),
					selectedIndex = if (layout.digits == DigitStyle.SINGLE_LINE) 0 else 1,
					onSelect = { i ->
						apply { copy(digits = if (i == 0) DigitStyle.SINGLE_LINE else DigitStyle.STACKED) }
					},
				)
				ChoiceRow(
					label = "Align",
					options = listOf("Left", "Centre", "Right"),
					selectedIndex = BlockAlign.entries.indexOf(layout.align),
					onSelect = { i -> apply { copy(align = BlockAlign.entries[i]) } },
				)
				ChoiceRow(
					label = "Position",
					options = listOf("Top", "Middle", "Bottom"),
					selectedIndex = BlockGravity.entries.indexOf(layout.gravity),
					onSelect = { i -> apply { copy(gravity = BlockGravity.entries[i]) } },
				)
				ChoiceRow(
					label = "Progress",
					options = listOf("Edge", "Ring", "Bar", "Off"),
					selectedIndex = ProgressStyle.entries.indexOf(layout.progress),
					onSelect = { i -> apply { copy(progress = ProgressStyle.entries[i]) } },
				)

				Spacer(modifier = Modifier.height(6.dp))
				Text(
					text = "SHOW",
					fontSize = 10.sp,
					fontWeight = FontWeight.SemiBold,
					letterSpacing = 2.sp,
					color = Color.White.copy(alpha = 0.4f),
				)
				Spacer(modifier = Modifier.height(10.dp))

				ElementToggle("Task chip", layout.showTask) { apply { copy(showTask = it) } }
				ElementToggle("Phase label", layout.showPhaseLabel) { apply { copy(showPhaseLabel = it) } }
				ElementToggle("Stanza lines", layout.showStanzaLines) { apply { copy(showStanzaLines = it) } }
				ElementToggle("+5m button", layout.showAddFive) { apply { copy(showAddFive = it) } }
				ElementToggle("Skip button", layout.showSkip) { apply { copy(showSkip = it) } }
				ElementToggle("Reset button", layout.showReset) { apply { copy(showReset = it) } }
			}
		}
	}
}

@Composable
private fun ChoiceRow(label: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
	val palette = LocalPalette.current
	Column(modifier = Modifier.padding(bottom = 14.dp)) {
		Text(
			text = label,
			fontSize = 12.sp,
			fontWeight = FontWeight.Medium,
			color = Color.White.copy(alpha = 0.6f),
		)
		Spacer(modifier = Modifier.height(8.dp))
		Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
			options.forEachIndexed { index, option ->
				val selected = index == selectedIndex
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.clip(RoundedCornerShape(10.dp))
						.background(
							if (selected) palette.focusPrimary.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f),
						)
						.clickable { onSelect(index) }
						.padding(horizontal = 12.dp, vertical = 7.dp),
				) {
					Text(
						text = option,
						fontSize = 12.sp,
						fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
						color = if (selected) palette.focusPrimary else Color.White.copy(alpha = 0.7f),
					)
				}
			}
		}
	}
}

@Composable
private fun ElementToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
	val palette = LocalPalette.current
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 2.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
		Switch(
			checked = checked,
			onCheckedChange = onChange,
			colors = SwitchDefaults.colors(
				checkedThumbColor = Color.White,
				checkedTrackColor = palette.focusPrimary,
				uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
			),
		)
	}
}
