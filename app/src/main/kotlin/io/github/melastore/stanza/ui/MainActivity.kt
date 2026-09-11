package io.github.melastore.stanza.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import io.github.melastore.stanza.StanzaApp
import io.github.melastore.stanza.data.datastore.UserPreferences
import io.github.melastore.stanza.data.db.SessionRecord
import io.github.melastore.stanza.data.db.TaskRecord
import io.github.melastore.stanza.domain.ProgressStyle
import io.github.melastore.stanza.domain.TimerIntent
import io.github.melastore.stanza.domain.TimerState
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.glass.MeshGradientBackdrop
import io.github.melastore.stanza.ui.glass.NoiseGrainOverlay
import io.github.melastore.stanza.ui.glass.PerimeterProgress
import io.github.melastore.stanza.ui.permissions.PermissionSheet
import io.github.melastore.stanza.ui.permissions.rememberPermissionStatus
import io.github.melastore.stanza.ui.settings.SettingsScreen
import io.github.melastore.stanza.ui.stats.StatsScreen
import io.github.melastore.stanza.ui.tasks.TasksScreen
import io.github.melastore.stanza.ui.theme.LocalPalette
import io.github.melastore.stanza.ui.theme.StanzaTheme
import io.github.melastore.stanza.ui.theme.paletteFor
import io.github.melastore.stanza.ui.timer.TimerScreen
import io.github.melastore.stanza.ui.timer.rememberRealtimeTicker
import kotlinx.coroutines.launch

enum class ScreenTab {
	TIMER,
	TASKS,
	STATS,
	SETTINGS,
}

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		val app = application as StanzaApp
		val container = app.container

		setContent {
			val timerState by container.stateStore.state.collectAsState()
			val userPrefs by container.settingsStore.preferences.collectAsState(initial = UserPreferences())

			StanzaTheme(palette = paletteFor(userPrefs.themeId, userPrefs.customHue)) {
				val tasks by container.taskRepository.tasks.collectAsState(initial = emptyList())

				LaunchedEffect(timerState.isRunning, userPrefs.keepScreenOn) {
					if (timerState.isRunning && userPrefs.keepScreenOn) {
						window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
					} else {
						window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
					}
				}

				StanzaMainApp(
					timerState = timerState,
					userPrefs = userPrefs,
					tasks = tasks,
					app = app,
				)
			}
		}
	}
}

@Composable
fun StanzaMainApp(timerState: TimerState, userPrefs: UserPreferences, tasks: List<TaskRecord>, app: StanzaApp,) {
	val hazeState = remember { HazeState() }
	var currentTab by remember { mutableStateOf(ScreenTab.TIMER) }
	val scope = rememberCoroutineScope()

	val palette = LocalPalette.current
	val nowRealtime = rememberRealtimeTicker(timerState.isRunning)
	val permissions = rememberPermissionStatus()

	val activeTask = remember(tasks, timerState.currentTaskId) {
		tasks.firstOrNull { it.id == timerState.currentTaskId }
	}

	var todayMinutes by remember { mutableIntStateOf(0) }
	var weekMinutes by remember { mutableIntStateOf(0) }
	var streakDays by remember { mutableIntStateOf(0) }
	var yearHeatmap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
	var recentSessions by remember { mutableStateOf<List<SessionRecord>>(emptyList()) }

	LaunchedEffect(currentTab, timerState.completedInCycle) {
		if (currentTab == ScreenTab.STATS) {
			todayMinutes = app.container.sessionRepository.getTodayFocusMinutes()
			weekMinutes = app.container.sessionRepository.getWeekFocusMinutes()
			streakDays = app.container.sessionRepository.getCurrentStreak()
			yearHeatmap = app.container.sessionRepository.getYearHeatmap()
			recentSessions = app.container.sessionRepository.getRecentSessions()
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		MeshGradientBackdrop(
			phase = timerState.phase,
			modifier = Modifier
				.fillMaxSize()
				.hazeSource(state = hazeState),
		)

		NoiseGrainOverlay()

		PerimeterProgress(
			progress = timerState.progress(nowRealtime),
			accent = palette.primaryFor(timerState.phase.isBreak),
			visible = !timerState.isIdle && userPrefs.layout.progress == ProgressStyle.PERIMETER,
		)

		Scaffold(
			containerColor = Color.Transparent,
			bottomBar = {
				FloatingGlassNavBar(
					currentTab = currentTab,
					hazeState = hazeState,
					reduceTransparency = userPrefs.reduceTransparency,
					onTabSelected = { currentTab = it },
				)
			},
		) { padding ->
			Crossfade(
				targetState = currentTab,
				label = "screen_crossfade",
				modifier = Modifier
					.fillMaxSize()
					.padding(padding),
			) { tab ->
				when (tab) {
					ScreenTab.TIMER -> TimerScreen(
						state = timerState,
						config = userPrefs.config,
						layout = userPrefs.layout,
						activeTask = activeTask,
						nowRealtime = nowRealtime,
						hazeState = hazeState,
						reduceTransparency = userPrefs.reduceTransparency,
						hapticsEnabled = userPrefs.vibrationEnabled,
						onDispatch = { app.container.timerController.dispatch(it) },
						onSelectTaskClicked = { currentTab = ScreenTab.TASKS },
					)

					ScreenTab.TASKS -> TasksScreen(
						tasks = tasks,
						activeTaskId = timerState.currentTaskId,
						hazeState = hazeState,
						reduceTransparency = userPrefs.reduceTransparency,
						onSelectTask = { id ->
							app.container.timerController.dispatch(
								TimerIntent.Start(phase = timerState.phase, taskId = id),
							)
						},
						onToggleComplete = { id, comp ->
							scope.launch { app.container.taskRepository.toggleTaskComplete(id, comp) }
						},
						onDeleteTask = { id ->
							scope.launch { app.container.taskRepository.deleteTask(id) }
						},
						onCreateTask = { title, est ->
							scope.launch { app.container.taskRepository.createTask(title, est) }
						},
					)

					ScreenTab.STATS -> StatsScreen(
						todayMinutes = todayMinutes,
						weekMinutes = weekMinutes,
						streakDays = streakDays,
						yearHeatmap = yearHeatmap,
						recentSessions = recentSessions,
						hazeState = hazeState,
						reduceTransparency = userPrefs.reduceTransparency,
					)

					ScreenTab.SETTINGS -> SettingsScreen(
						preferences = userPrefs,
						hazeState = hazeState,
						onSelectTheme = { scope.launch { app.container.settingsStore.setThemeId(it) } },
						onUpdateLayout = { scope.launch { app.container.settingsStore.setLayout(it) } },
						onUpdateCustomHue = { scope.launch { app.container.settingsStore.setCustomHue(it) } },
						onUpdateFocusMinutes = {
							scope.launch { app.container.settingsStore.updateFocusMinutes(it) }
						},
						onUpdateShortBreakMinutes = {
							scope.launch { app.container.settingsStore.updateShortBreakMinutes(it) }
						},
						onUpdateLongBreakMinutes = {
							scope.launch { app.container.settingsStore.updateLongBreakMinutes(it) }
						},
						onUpdatePomodorosPerCycle = {
							scope.launch { app.container.settingsStore.updatePomodorosPerCycle(it) }
						},
						onToggleAutoStartBreaks = {
							scope.launch { app.container.settingsStore.setAutoStartBreaks(it) }
						},
						onToggleAutoStartFocus = {
							scope.launch { app.container.settingsStore.setAutoStartFocus(it) }
						},
						onToggleReduceTransparency = {
							scope.launch { app.container.settingsStore.setReduceTransparency(it) }
						},
						onToggleSound = {
							scope.launch { app.container.settingsStore.setSoundEnabled(it) }
						},
						onToggleVibration = {
							scope.launch { app.container.settingsStore.setVibrationEnabled(it) }
						},
						onToggleKeepScreenOn = {
							scope.launch { app.container.settingsStore.setKeepScreenOn(it) }
						},
					)
				}
			}
		}

		// Also shown again if notification permission gets revoked, the timer is useless without it.
		if (!userPrefs.permissionPromptSeen || permissions.missingRequired) {
			PermissionSheet(
				status = permissions,
				hazeState = hazeState,
				reduceTransparency = userPrefs.reduceTransparency,
				onDismiss = { scope.launch { app.container.settingsStore.setPermissionPromptSeen() } },
			)
		}
	}
}

@Composable
fun FloatingGlassNavBar(
	currentTab: ScreenTab,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onTabSelected: (ScreenTab) -> Unit,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 24.dp, vertical = 16.dp),
		contentAlignment = Alignment.Center,
	) {
		GlassSurface(
			hazeState = hazeState,
			reduceTransparency = reduceTransparency,
			variant = GlassVariant.RAISED,
			shape = RoundedCornerShape(32.dp),
			modifier = Modifier
				.widthIn(max = 440.dp)
				.fillMaxWidth(),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 10.dp, horizontal = 12.dp),
				horizontalArrangement = Arrangement.SpaceAround,
				verticalAlignment = Alignment.CenterVertically,
			) {
				NavBarItem(
					icon = Icons.Default.HourglassBottom,
					label = "Timer",
					selected = currentTab == ScreenTab.TIMER,
					onClick = { onTabSelected(ScreenTab.TIMER) },
				)
				NavBarItem(
					icon = Icons.Default.Checklist,
					label = "Tasks",
					selected = currentTab == ScreenTab.TASKS,
					onClick = { onTabSelected(ScreenTab.TASKS) },
				)
				NavBarItem(
					icon = Icons.Default.BarChart,
					label = "Stats",
					selected = currentTab == ScreenTab.STATS,
					onClick = { onTabSelected(ScreenTab.STATS) },
				)
				NavBarItem(
					icon = Icons.Default.Tune,
					label = "Settings",
					selected = currentTab == ScreenTab.SETTINGS,
					onClick = { onTabSelected(ScreenTab.SETTINGS) },
				)
			}
		}
	}
}

@Composable
private fun NavBarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit,) {
	val tint = if (selected) LocalPalette.current.focusPrimary else Color.White.copy(alpha = 0.45f)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.clip(RoundedCornerShape(16.dp))
			.clickable { onClick() }
			.padding(horizontal = 14.dp, vertical = 6.dp),
	) {
		Icon(
			imageVector = icon,
			contentDescription = label,
			tint = tint,
			modifier = Modifier.size(22.dp),
		)
		Text(
			text = label,
			fontSize = 10.sp,
			color = tint,
			fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
		)
	}
}
