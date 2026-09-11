package io.github.melastore.stanza.ui.timer

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.data.db.TaskRecord
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.TimerConfig
import io.github.melastore.stanza.domain.TimerIntent
import io.github.melastore.stanza.domain.TimerState
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.theme.BreakPrimary
import io.github.melastore.stanza.ui.theme.BreakSecondary
import io.github.melastore.stanza.ui.theme.FocusPrimary
import io.github.melastore.stanza.ui.theme.FocusSecondary
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
	state: TimerState,
	config: TimerConfig,
	activeTask: TaskRecord?,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onDispatch: (TimerIntent) -> Unit,
	onSelectTaskClicked: () -> Unit,
	modifier: Modifier = Modifier,
) {
	// Re-calculate remaining time every 500ms when running for fluid UI countdown
	var currentRealtime by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
	LaunchedEffect(state.isRunning) {
		while (state.isRunning) {
			currentRealtime = SystemClock.elapsedRealtime()
			delay(500)
		}
		currentRealtime = SystemClock.elapsedRealtime()
	}

	val remainingMs = state.remainingMs(currentRealtime)
	val progress = state.progress(currentRealtime)

	val minutes = (remainingMs / 1000) / 60
	val seconds = (remainingMs / 1000) % 60
	val timeDisplay = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

	val phaseTitle = when (state.phase) {
		Phase.FOCUS -> "FOCUS STANZA"
		Phase.SHORT_BREAK -> "SHORT BREAK"
		Phase.LONG_BREAK -> "LONG BREAK"
		Phase.IDLE -> "READY TO FOCUS"
	}

	val accentPrimary = if (state.phase.isBreak) BreakPrimary else FocusPrimary
	val accentSecondary = if (state.phase.isBreak) BreakSecondary else FocusSecondary

	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.SpaceBetween,
	) {
		Spacer(modifier = Modifier.height(24.dp))

		// Active Task Pill / Selector
		GlassSurface(
			hazeState = hazeState,
			reduceTransparency = reduceTransparency,
			variant = GlassVariant.CONTROL,
			shape = RoundedCornerShape(32.dp),
			modifier = Modifier
				.clip(RoundedCornerShape(32.dp))
				.clickable { onSelectTaskClicked() },
		) {
			Row(
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = if (activeTask != null) {
						"✦ ${activeTask.title} (${activeTask.completedPomodoros}/${activeTask.estimatedPomodoros})"
					} else {
						"+ Assign Task"
					},
					fontSize = 13.sp,
					fontWeight = FontWeight.Medium,
					color = Color.White.copy(alpha = 0.85f),
				)
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Central Circular Progress & Time Readout
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier.size(290.dp),
		) {
			Canvas(modifier = Modifier.fillMaxSize()) {
				val strokeWidth = 14.dp.toPx()
				val diameter = size.minDimension - strokeWidth
				val arcSize = Size(diameter, diameter)
				val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

				// Background Faint Track
				drawArc(
					color = Color.White.copy(alpha = 0.07f),
					startAngle = 0f,
					sweepAngle = 360f,
					useCenter = false,
					topLeft = topLeft,
					size = arcSize,
					style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
				)

				// Sweep Progress Arc
				if (progress > 0f) {
					drawArc(
						brush = Brush.sweepGradient(
							listOf(accentSecondary, accentPrimary),
							center = center,
						),
						startAngle = -90f,
						sweepAngle = progress * 360f,
						useCenter = false,
						topLeft = topLeft,
						size = arcSize,
						style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
					)
				}
			}

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = phaseTitle,
					fontSize = 12.sp,
					fontWeight = FontWeight.SemiBold,
					letterSpacing = 2.sp,
					color = accentPrimary,
				)

				Spacer(modifier = Modifier.height(4.dp))

				Text(
					text = timeDisplay,
					fontSize = 64.sp,
					fontWeight = FontWeight.Light,
					fontFamily = FontFamily.SansSerif,
					color = Color.White,
				)

				Spacer(modifier = Modifier.height(12.dp))

				// Cycle indicator dots (e.g. 4 dots for standard pomodoro cycle)
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					for (i in 0 until config.pomodorosPerCycle) {
						val isDone = i < state.completedInCycle
						Box(
							modifier = Modifier
								.size(8.dp)
								.clip(CircleShape)
								.background(if (isDone) accentPrimary else Color.White.copy(alpha = 0.15f)),
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Bottom Glass Control Bar
		GlassSurface(
			hazeState = hazeState,
			reduceTransparency = reduceTransparency,
			variant = GlassVariant.RAISED,
			shape = RoundedCornerShape(36.dp),
			modifier = Modifier
				.fillMaxWidth()
				.padding(bottom = 24.dp),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 12.dp, horizontal = 20.dp),
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalAlignment = Alignment.CenterVertically,
			) {
				// +5 Minutes Button
				GlassControlIcon(
					label = "+5m",
					onClick = { onDispatch(TimerIntent.AddMinutes(5)) },
				)

				// Primary Action Button: Play / Pause / Start
				Box(
					modifier = Modifier
						.size(68.dp)
						.clip(CircleShape)
						.background(accentPrimary)
						.clickable {
							when {
								state.isIdle -> onDispatch(TimerIntent.Start())
								state.isRunning -> onDispatch(TimerIntent.Pause)
								state.isPaused -> onDispatch(TimerIntent.Resume)
							}
						},
					contentAlignment = Alignment.Center,
				) {
					Icon(
						imageVector = when {
							state.isRunning -> Icons.Default.Pause
							else -> Icons.Default.PlayArrow
						},
						contentDescription = "Primary Action",
						tint = Color.Black,
						modifier = Modifier.size(32.dp),
					)
				}

				// Skip to Next Phase Button
				GlassControlIcon(
					icon = Icons.Default.SkipNext,
					label = "Skip",
					onClick = { onDispatch(TimerIntent.Skip) },
				)
			}
		}
	}
}

@Composable
private fun GlassControlIcon(
	label: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
	onClick: () -> Unit,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.clip(RoundedCornerShape(12.dp))
			.clickable { onClick() }
			.padding(horizontal = 12.dp, vertical = 6.dp),
	) {
		if (icon != null) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				tint = Color.White.copy(alpha = 0.85f),
				modifier = Modifier.size(24.dp),
			)
		} else {
			Text(
				text = label,
				fontSize = 15.sp,
				fontWeight = FontWeight.SemiBold,
				color = Color.White.copy(alpha = 0.85f),
			)
		}
	}
}

// (End of TimerScreen)
