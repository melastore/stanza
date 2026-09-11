package io.github.melastore.stanza.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.melastore.stanza.R

object NotificationChannels {

	const val CHANNEL_ONGOING = "channel_ongoing"
	const val CHANNEL_ALERTS = "channel_alerts_v2"
	private const val CHANNEL_ALERTS_LEGACY = "channel_alerts"

	@RequiresApi(Build.VERSION_CODES.O)
	fun createChannels(context: Context) {
		val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// The app plays the chime itself so the in-app toggles work. A channel's sound can't be
		// changed after creation, so the old channel is deleted and a new id is used.
		manager.deleteNotificationChannel(CHANNEL_ALERTS_LEGACY)

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

		val alertsChannel = NotificationChannel(
			CHANNEL_ALERTS,
			context.getString(R.string.channel_alerts_name),
			NotificationManager.IMPORTANCE_HIGH,
		).apply {
			description = context.getString(R.string.channel_alerts_description)
			enableVibration(false)
			setSound(null, null)
		}

		manager.createNotificationChannel(ongoingChannel)
		manager.createNotificationChannel(alertsChannel)
	}
}
