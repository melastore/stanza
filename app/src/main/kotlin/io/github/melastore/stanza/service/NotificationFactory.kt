package io.github.melastore.stanza.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import io.github.melastore.stanza.R
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.TimerState
import io.github.melastore.stanza.ui.MainActivity
import java.util.Locale

object NotificationFactory {

	const val NOTIFICATION_ID_ONGOING = 1001
	const val NOTIFICATION_ID_ALERT = 1002

	const val ACTION_PAUSE = "io.github.melastore.stanza.ACTION_PAUSE"
	const val ACTION_RESUME = "io.github.melastore.stanza.ACTION_RESUME"
	const val ACTION_SKIP = "io.github.melastore.stanza.ACTION_SKIP"
	const val ACTION_ADD_FIVE = "io.github.melastore.stanza.ACTION_ADD_FIVE"
	const val ACTION_STOP = "io.github.melastore.stanza.ACTION_STOP"
	const val ACTION_START_NEXT = "io.github.melastore.stanza.ACTION_START_NEXT"

	fun buildOngoingNotification(context: Context, state: TimerState): Notification {
		val openAppIntent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		val contentPendingIntent = PendingIntent.getActivity(
			context,
			0,
			openAppIntent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)

		val title = when (state.phase) {
			Phase.FOCUS -> "Focus Stanza"
			Phase.SHORT_BREAK -> "Short Break"
			Phase.LONG_BREAK -> "Long Break"
			Phase.IDLE -> "Stanza Idle"
		}

		val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_ONGOING)
			.setSmallIcon(R.drawable.ic_timer)
			.setContentTitle(title)
			.setContentIntent(contentPendingIntent)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setCategory(NotificationCompat.CATEGORY_STOPWATCH)
			.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

		if (state.isRunning) {
			val remainingMs = state.remainingMs(SystemClock.elapsedRealtime())
			val targetWallTime = System.currentTimeMillis() + remainingMs

			builder
				.setUsesChronometer(true)
				.setChronometerCountDown(true)
				.setWhen(targetWallTime)
				.setContentText("Focus session in progress")
				.addAction(
					android.R.drawable.ic_media_pause,
					"Pause",
					createActionPendingIntent(context, ACTION_PAUSE, 1),
				)
		} else if (state.isPaused) {
			val remainingMs = state.pausedRemainingMs ?: 0L
			val minutes = (remainingMs / 1000) / 60
			val seconds = (remainingMs / 1000) % 60
			val formattedTime = String.format(Locale.getDefault(), "%02d:%02d (Paused)", minutes, seconds)

			builder
				.setUsesChronometer(false)
				.setContentText(formattedTime)
				.addAction(
					android.R.drawable.ic_media_play,
					"Resume",
					createActionPendingIntent(context, ACTION_RESUME, 2),
				)
		} else {
			builder
				.setUsesChronometer(false)
				.setContentText("Ready")
		}

		builder
			.addAction(
				0,
				"+5m",
				createActionPendingIntent(context, ACTION_ADD_FIVE, 3),
			)
			.addAction(
				0,
				"Skip",
				createActionPendingIntent(context, ACTION_SKIP, 4),
			)
			.addAction(
				0,
				"Stop",
				createActionPendingIntent(context, ACTION_STOP, 5),
			)

		return builder.build()
	}

	fun buildAlertNotification(context: Context, completedPhase: Phase, nextPhase: Phase,): Notification {
		val openAppIntent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
		val contentPendingIntent = PendingIntent.getActivity(
			context,
			0,
			openAppIntent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)

		val (title, text) = when (completedPhase) {
			Phase.FOCUS ->
				"Stanza Complete!" to
					"Take a well-earned ${if (nextPhase == Phase.LONG_BREAK) "long" else "short"} break."

			Phase.SHORT_BREAK, Phase.LONG_BREAK -> "Break Over!" to "Ready to begin your next focus stanza?"

			Phase.IDLE -> "Session Complete" to "Great work!"
		}

		return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_ALERTS)
			.setSmallIcon(R.drawable.ic_timer)
			.setContentTitle(title)
			.setContentText(text)
			.setContentIntent(contentPendingIntent)
			.setAutoCancel(true)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setCategory(NotificationCompat.CATEGORY_ALARM)
			.addAction(
				android.R.drawable.ic_media_play,
				"Start Next",
				createActionPendingIntent(context, ACTION_START_NEXT, 6),
			)
			.build()
	}

	private fun createActionPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
		val intent = Intent(context, ActionReceiver::class.java).apply {
			this.action = action
		}
		return PendingIntent.getBroadcast(
			context,
			requestCode,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)
	}
}
