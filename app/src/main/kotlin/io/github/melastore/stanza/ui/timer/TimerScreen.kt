package io.github.melastore.stanza.ui.timer

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.data.db.TaskRecord
import io.github.melastore.stanza.domain.BlockAlign
import io.github.melastore.stanza.domain.BlockGravity
import io.github.melastore.stanza.domain.DigitStyle
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.ProgressStyle
import io.github.melastore.stanza.domain.TimerConfig
import io.github.melastore.stanza.domain.TimerIntent
import io.github.melastore.stanza.domain.TimerLayout
import io.github.melastore.stanza.domain.TimerState
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.theme.LocalPalette
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val WideBreakpoint = 640.dp
private val ControlsMaxWidth = 340.dp
private val RingSize = 300.dp

@Composable
fun TimerScreen(
	state: TimerState,
	config: TimerConfig,
	layout: TimerLayout,
	activeTask: TaskRecord?,
	nowRealtime: Long,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	hapticsEnabled: Boolean,
	onDispatch: (TimerIntent) -> Unit,
	onSelectTaskClicked: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val remainingMs = if (state.isIdle) {
		config.durationMsFor(Phase.FOCUS)
	} else {
		state.remainingMs(nowRealtime)
	}
	val totalSeconds = (remainingMs + 999) / 1000
	val minutes = String.format(Locale.US, "%02d", totalSeconds / 60)
	val seconds = String.format(Locale.US, "%02d", totalSeconds % 60)
	val progress = state.progress(nowRealtime)

	val palette = LocalPalette.current
	val accent = palette.primaryFor(state.phase.isBreak)
	val accentSecondary = palette.secondaryFor(state.phase.isBreak)
	val resetAlpha by animateFloatAsState(if (state.isIdle) 0f else 1f, label = "reset_alpha")

	val view = LocalView.current
	val haptics = remember(hapticsEnabled, view) { Haptics(view, hapticsEnabled) }

	// Pulse the digits during the last minute.
	val endingSoon = state.isRunning && remainingMs in 1..60_000
	val pulse by rememberInfiniteTransition(label = "ending_pulse").animateFloat(
		initialValue = 1f,
		targetValue = 0.45f,
		animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
		label = "pulse",
	)
	val secondsAlpha = when {
		state.isIdle -> 0.35f
		endingSoon -> pulse
		else -> 0.9f
	}

	BoxWithConstraints(modifier = modifier.fillMaxSize()) {
		val wide = maxWidth >= WideBreakpoint && maxWidth > maxHeight
		val ringed = layout.progress == ProgressStyle.RING
		// Stacking needs vertical room; a short landscape window has none.
		val stacked = layout.digits == DigitStyle.STACKED && (!wide || maxHeight >= 520.dp)
		val available = if (wide) (maxWidth - 152.dp) / 2 else maxWidth - 64.dp
		val lineSize = when {
			ringed && stacked -> 74f
			ringed -> RingSize.value * 0.32f
			stacked -> (available.value * 0.42f).coerceIn(64f, 150f)
			else -> (available.value / 2.62f).coerceIn(56f, 150f)
		}.sp

		val horizontal = when (layout.align) {
			BlockAlign.START -> Alignment.Start
			BlockAlign.CENTER -> Alignment.CenterHorizontally
			BlockAlign.END -> Alignment.End
		}

		val block = @Composable {
			Column(horizontalAlignment = horizontal) {
				Box(contentAlignment = Alignment.Center) {
					if (ringed) {
						ProgressRing(
							progress = progress,
							accentPrimary = accent,
							accentSecondary = accentSecondary,
							modifier = Modifier.size(RingSize),
						)
					}
					Readout(
						minutes = minutes,
						seconds = seconds,
						phase = state.phase,
						isIdle = state.isIdle,
						accent = accent,
						secondsAlpha = secondsAlpha,
						align = if (ringed) Alignment.CenterHorizontally else horizontal,
						stacked = stacked,
						fontSize = lineSize,
						showPhaseLabel = layout.showPhaseLabel,
					)
				}

				if (layout.progress == ProgressStyle.BAR) {
					Spacer(modifier = Modifier.height(22.dp))
					ProgressBar(progress = progress, accent = accent, width = available)
				}

				if (layout.showStanzaLines) {
					Spacer(modifier = Modifier.height(26.dp))
					StanzaLines(
						total = config.pomodorosPerCycle,
						completed = state.completedInCycle,
						isFocusActive = state.phase == Phase.FOCUS,
						accent = accent,
						align = horizontal,
					)
				}
			}
		}

		val header = @Composable {
			if (layout.showTask || layout.showReset) {
				Row(
					modifier = if (wide) Modifier else Modifier.fillMaxWidth(),
					horizontalArrangement = if (wide) Arrangement.spacedBy(12.dp) else Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					if (layout.showTask) {
						TaskPill(
							activeTask = activeTask,
							hazeState = hazeState,
							reduceTransparency = reduceTransparency,
							onClick = onSelectTaskClicked,
						)
					} else {
						Spacer(modifier = Modifier.width(1.dp))
					}
					if (layout.showReset) {
						ResetButton(
							hazeState = hazeState,
							reduceTransparency = reduceTransparency,
							onClick = {
								haptics.reject()
								onDispatch(TimerIntent.Stop)
							},
							modifier = Modifier.alpha(resetAlpha),
							enabled = !state.isIdle,
						)
					}
				}
			}
		}

		val controls = @Composable {
			Controls(
				state = state,
				layout = layout,
				accent = accent,
				hazeState = hazeState,
				reduceTransparency = reduceTransparency,
				haptics = haptics,
				onDispatch = onDispatch,
			)
		}

		if (wide) {
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 48.dp, vertical = 32.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Column(
					modifier = Modifier
						.weight(1f)
						.fillMaxHeight(),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.End,
				) {
					block()
				}
				Spacer(modifier = Modifier.width(56.dp))
				Column(
					modifier = Modifier
						.weight(1f)
						.fillMaxHeight(),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.Start,
				) {
					header()
					Spacer(modifier = Modifier.height(28.dp))
					controls()
				}
			}
		} else {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 32.dp),
				horizontalAlignment = horizontal,
			) {
				Spacer(modifier = Modifier.height(20.dp))
				header()
				if (layout.gravity == BlockGravity.TOP) {
					Spacer(modifier = Modifier.height(36.dp))
				} else {
					Spacer(modifier = Modifier.weight(1f))
				}
				block()
				Spacer(modifier = Modifier.weight(1f))
				Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					controls()
				}
				Spacer(modifier = Modifier.height(24.dp))
			}
		}
	}
}

// Single line when there's width for it, stacked otherwise.
@Composable
private fun Readout(
	minutes: String,
	seconds: String,
	phase: Phase,
	isIdle: Boolean,
	accent: Color,
	secondsAlpha: Float,
	align: Alignment.Horizontal,
	stacked: Boolean,
	fontSize: TextUnit,
	showPhaseLabel: Boolean,
) {
	val kicker = when (phase) {
		Phase.FOCUS -> "FOCUS"
		Phase.SHORT_BREAK -> "SHORT BREAK"
		Phase.LONG_BREAK -> "LONG BREAK"
		Phase.IDLE -> "READY"
	}
	val minuteColor = Color.White.copy(alpha = if (isIdle) 0.5f else 1f)
	val digitStyle = TextStyle(
		fontSize = fontSize,
		lineHeight = fontSize * 0.9f,
		fontWeight = FontWeight.Light,
		fontFamily = FontFamily.SansSerif,
		fontFeatureSettings = "tnum",
		letterSpacing = if (stacked) (-5).sp else (-3).sp,
	)

	Column(horizontalAlignment = align) {
		if (showPhaseLabel) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(width = 18.dp, height = 2.dp)
						.background(if (isIdle) Color.White.copy(alpha = 0.3f) else accent),
				)
				Spacer(modifier = Modifier.width(10.dp))
				Text(
					text = kicker,
					fontSize = 11.sp,
					fontWeight = FontWeight.SemiBold,
					letterSpacing = 3.sp,
					color = Color.White.copy(alpha = if (isIdle) 0.4f else 0.7f),
				)
			}
			Spacer(modifier = Modifier.height(if (stacked) 18.dp else 12.dp))
		}

		if (stacked) {
			Text(text = minutes, style = digitStyle, color = minuteColor)
			Text(text = seconds, style = digitStyle, color = accent.copy(alpha = secondsAlpha))
		} else {
			Text(
				text = buildAnnotatedString {
					withStyle(SpanStyle(color = minuteColor)) { append(minutes) }
					withStyle(SpanStyle(color = Color.White.copy(alpha = 0.25f))) { append(":") }
					withStyle(SpanStyle(color = accent.copy(alpha = secondsAlpha))) { append(seconds) }
				},
				style = digitStyle,
				maxLines = 1,
			)
		}
	}
}

@Composable
private fun ProgressRing(
	progress: Float,
	accentPrimary: Color,
	accentSecondary: Color,
	modifier: Modifier = Modifier,
) {
	val sweep by animateFloatAsState(
		targetValue = progress * 360f,
		animationSpec = spring(stiffness = 120f),
		label = "ring_sweep",
	)

	Canvas(modifier = modifier) {
		val stroke = 12.dp.toPx()
		val diameter = size.minDimension - stroke * 2
		val arcSize = Size(diameter, diameter)
		val topLeft = Offset(stroke, stroke)
		val radius = diameter / 2f

		drawArc(
			color = Color.White.copy(alpha = 0.06f),
			startAngle = 0f,
			sweepAngle = 360f,
			useCenter = false,
			topLeft = topLeft,
			size = arcSize,
			style = Stroke(width = stroke),
		)

		if (sweep <= 0f) return@Canvas

		rotate(-90f) {
			drawArc(
				color = accentPrimary.copy(alpha = 0.18f),
				startAngle = 0f,
				sweepAngle = sweep,
				useCenter = false,
				topLeft = topLeft,
				size = arcSize,
				style = Stroke(width = stroke * 2.2f, cap = StrokeCap.Round),
			)
			drawArc(
				brush = Brush.sweepGradient(
					0f to accentSecondary,
					0.5f to accentPrimary,
					1f to accentSecondary,
					center = center,
				),
				startAngle = 0f,
				sweepAngle = sweep,
				useCenter = false,
				topLeft = topLeft,
				size = arcSize,
				style = Stroke(width = stroke, cap = StrokeCap.Round),
			)
		}

		val headAngle = Math.toRadians((sweep - 90f).toDouble())
		val head = Offset(
			x = center.x + radius * cos(headAngle).toFloat(),
			y = center.y + radius * sin(headAngle).toFloat(),
		)
		drawCircle(color = accentPrimary.copy(alpha = 0.30f), radius = stroke * 1.4f, center = head)
		drawCircle(color = Color.White, radius = stroke * 0.34f, center = head)
	}
}

@Composable
private fun ProgressBar(progress: Float, accent: Color, width: Dp) {
	val value by animateFloatAsState(progress.coerceIn(0f, 1f), spring(stiffness = 120f), label = "bar")
	Box(
		modifier = Modifier
			.width(width)
			.height(4.dp)
			.clip(RoundedCornerShape(2.dp))
			.background(Color.White.copy(alpha = 0.10f)),
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth(value)
				.fillMaxHeight()
				.clip(RoundedCornerShape(2.dp))
				.background(accent),
		)
	}
}

// One line per focus session in the cycle, current one highlighted.
@Composable
private fun StanzaLines(
	total: Int,
	completed: Int,
	isFocusActive: Boolean,
	accent: Color,
	align: Alignment.Horizontal,
) {
	val widths = remember(total) { List(total) { 104.dp - 22.dp * (it % 3) } }

	Column(
		horizontalAlignment = align,
		verticalArrangement = Arrangement.spacedBy(7.dp),
	) {
		for (i in 0 until total) {
			val isDone = i < completed
			val isCurrent = isFocusActive && i == completed
			Box(
				modifier = Modifier
					.width(widths[i])
					.height(if (isCurrent) 4.dp else 3.dp)
					.clip(RoundedCornerShape(2.dp))
					.background(
						when {
							isDone -> accent
							isCurrent -> accent.copy(alpha = 0.45f)
							else -> Color.White.copy(alpha = 0.12f)
						},
					),
			)
		}
	}
}

@Composable
private fun Controls(
	state: TimerState,
	layout: TimerLayout,
	accent: Color,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	haptics: Haptics,
	onDispatch: (TimerIntent) -> Unit,
) {
	val spread = layout.showAddFive || layout.showSkip
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.RAISED,
		shape = RoundedCornerShape(40.dp),
		modifier = if (spread) {
			Modifier
				.widthIn(max = ControlsMaxWidth)
				.fillMaxWidth()
		} else {
			Modifier
		},
	) {
		Row(
			modifier = if (spread) {
				Modifier
					.fillMaxWidth()
					.padding(vertical = 14.dp, horizontal = 22.dp)
			} else {
				Modifier.padding(10.dp)
			},
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			if (layout.showAddFive) {
				SecondaryControl(
					label = "+5m",
					enabled = !state.isIdle,
					onClick = {
						haptics.tick()
						onDispatch(TimerIntent.AddMinutes(5))
					},
				)
			}

			PrimaryControl(
				isRunning = state.isRunning,
				accent = accent,
				onClick = {
					haptics.confirm()
					when {
						state.isIdle -> onDispatch(TimerIntent.Start())
						state.isRunning -> onDispatch(TimerIntent.Pause)
						else -> onDispatch(TimerIntent.Resume)
					}
				},
			)

			if (layout.showSkip) {
				SecondaryControl(
					icon = Icons.Default.SkipNext,
					label = "Skip",
					enabled = !state.isIdle,
					onClick = {
						haptics.tick()
						onDispatch(TimerIntent.Skip)
					},
				)
			}
		}
	}
}

@Composable
private fun TaskPill(activeTask: TaskRecord?, hazeState: HazeState, reduceTransparency: Boolean, onClick: () -> Unit,) {
	val shape = RoundedCornerShape(32.dp)
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CONTROL,
		shape = shape,
		modifier = Modifier
			.clip(shape)
			.clickable { onClick() },
	) {
		Text(
			text = if (activeTask != null) {
				"✦ ${activeTask.title}  ${activeTask.completedPomodoros}/${activeTask.estimatedPomodoros}"
			} else {
				"+ Assign task"
			},
			fontSize = 13.sp,
			fontWeight = FontWeight.Medium,
			color = Color.White.copy(alpha = 0.85f),
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
		)
	}
}

@Composable
private fun ResetButton(
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onClick: () -> Unit,
	enabled: Boolean,
	modifier: Modifier = Modifier,
) {
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CONTROL,
		shape = CircleShape,
		modifier = modifier
			.size(40.dp)
			.clip(CircleShape)
			.clickable(enabled = enabled) { onClick() },
	) {
		Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
			Icon(
				imageVector = Icons.Default.Refresh,
				contentDescription = "Reset",
				tint = Color.White.copy(alpha = 0.75f),
				modifier = Modifier.size(18.dp),
			)
		}
	}
}

@Composable
private fun PrimaryControl(isRunning: Boolean, accent: Color, onClick: () -> Unit) {
	val interaction = remember { MutableInteractionSource() }
	val pressed by interaction.collectIsPressedAsState()
	val scale by animateFloatAsState(
		targetValue = if (pressed) 0.92f else 1f,
		animationSpec = spring(stiffness = 900f),
		label = "primary_press",
	)

	Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
		Box(
			modifier = Modifier
				.scale(scale)
				.size(76.dp)
				.clip(CircleShape)
				.background(Brush.radialGradient(listOf(accent.copy(alpha = 0.28f), Color.Transparent))),
		)
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.scale(scale)
				.size(64.dp)
				.clip(CircleShape)
				.background(accent)
				.clickable(interactionSource = interaction, indication = null) { onClick() },
		) {
			Icon(
				imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
				contentDescription = if (isRunning) "Pause" else "Start",
				tint = LocalPalette.current.ink,
				modifier = Modifier.size(30.dp),
			)
		}
	}
}

@Composable
private fun SecondaryControl(label: String, icon: ImageVector? = null, enabled: Boolean = true, onClick: () -> Unit,) {
	val alpha = if (enabled) 0.85f else 0.28f
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.size(56.dp)
			.clip(CircleShape)
			.clickable(enabled = enabled) { onClick() },
	) {
		if (icon != null) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				tint = Color.White.copy(alpha = alpha),
				modifier = Modifier.size(24.dp),
			)
		} else {
			Text(
				text = label,
				fontSize = 15.sp,
				fontWeight = FontWeight.SemiBold,
				color = Color.White.copy(alpha = alpha),
			)
		}
	}
}

// Short taps go through the platform so they follow the system haptic setting, not just ours.
class Haptics(private val view: View, private val enabled: Boolean) {

	fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK)

	fun confirm() = perform(HapticFeedbackConstants.CONFIRM)

	fun reject() = perform(HapticFeedbackConstants.REJECT)

	private fun perform(constant: Int) {
		if (enabled) view.performHapticFeedback(constant)
	}
}
