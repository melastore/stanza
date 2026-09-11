package io.github.melastore.stanza.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.melastore.stanza.StanzaApp
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.TimerIntent

class StanzaGlanceWidget : GlanceAppWidget() {

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val app = context.applicationContext as StanzaApp
		val state = app.container.stateStore.get()

		val statusText = when {
			state.isRunning -> if (state.phase.isBreak) "Resting" else "Focusing"
			state.isPaused -> "Paused"
			else -> "Idle"
		}

		val phaseLabel = when (state.phase) {
			Phase.FOCUS -> "FOCUS STANZA"
			Phase.SHORT_BREAK -> "SHORT BREAK"
			Phase.LONG_BREAK -> "LONG BREAK"
			Phase.IDLE -> "READY"
		}

		provideContent {
			Box(
				modifier = GlanceModifier
					.fillMaxSize()
					.background(ColorProvider(Color(0xE610121A)))
					.cornerRadius(20.dp)
					.padding(14.dp),
				contentAlignment = Alignment.Center,
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(
						text = phaseLabel,
						style = TextStyle(
							color = ColorProvider(Color(0xFF8B64FF)),
							fontSize = 11.sp,
							fontWeight = FontWeight.Bold,
						),
					)

					Spacer(modifier = GlanceModifier.height(4.dp))

					Text(
						text = statusText,
						style = TextStyle(
							color = ColorProvider(Color.White),
							fontSize = 20.sp,
							fontWeight = FontWeight.Medium,
						),
					)

					Spacer(modifier = GlanceModifier.height(8.dp))

					Box(
						modifier = GlanceModifier
							.background(ColorProvider(Color(0xFF8B64FF)))
							.cornerRadius(16.dp)
							.padding(horizontal = 14.dp, vertical = 6.dp)
							.clickable(actionRunCallback<ToggleTimerActionCallback>()),
					) {
						Text(
							text = if (state.isRunning) "Pause" else "Start",
							style = TextStyle(
								color = ColorProvider(Color.Black),
								fontSize = 13.sp,
								fontWeight = FontWeight.Bold,
							),
						)
					}
				}
			}
		}
	}
}

class ToggleTimerActionCallback : androidx.glance.appwidget.action.ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: androidx.glance.action.ActionParameters,
	) {
		val app = context.applicationContext as StanzaApp
		val state = app.container.stateStore.get()
		when {
			state.isIdle -> app.container.timerController.dispatch(TimerIntent.Start())
			state.isRunning -> app.container.timerController.dispatch(TimerIntent.Pause)
			state.isPaused -> app.container.timerController.dispatch(TimerIntent.Resume)
		}
		StanzaGlanceWidget().update(context, glanceId)
	}
}

class StanzaGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
	override val glanceAppWidget: GlanceAppWidget = StanzaGlanceWidget()
}
