package io.github.melastore.stanza.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import io.github.melastore.stanza.R

object NotificationChannels {

	const val CHANNEL_ONGOING = "channel_ongoing"
	const val CHANNEL_ALERTS = "channel_alerts"

	@RequiresApi(Build.VERSION_CODES.O)
	fun createChannels(context: Context) {
		val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// 1. Silent, low-importance channel for the live ongoing chronometer
		val ongoingChannel = NotificationChannel(
			CHANNEL_ONGOING,
			context.getString(R.string.channel_ongoing_name),
			NotificationManager.IMPORTANCE_LOW,
		).apply {
			description = context.getString(R.string.channel_ongoing_description)
			setShowBadge(false)
			enableVibration(false)
			setSound(null, null)
		}

		// 2. High-importance channel for session completion alerts
		val alertsChannel = NotificationChannel(
			CHANNEL_ALERTS,
			context.getString(R.string.channel_alerts_name),
			NotificationManager.IMPORTANCE_HIGH,
		).apply {
			description = context.getString(R.string.channel_alerts_description)
			enableVibration(true)
			vibrationPattern = longArrayOf(0, 300, 200, 300)
			val audioAttributes = AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.setUsage(AudioAttributes.USAGE_ALARM)
				.build()
			setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
		}

		manager.createNotificationChannel(ongoingChannel)
		manager.createNotificationChannel(alertsChannel)
	}
}
