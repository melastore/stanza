package io.github.melastore.stanza.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmScheduler(private val context: Context) {

	private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

	fun schedule(triggerAtWallClock: Long) {
		val intent = Intent(context, AlarmReceiver::class.java).apply {
			action = ACTION_PHASE_ALARM
		}
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			ALARM_REQUEST_CODE,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
		)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (alarmManager.canScheduleExactAlarms()) {
				alarmManager.setExactAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					triggerAtWallClock,
					pendingIntent,
				)
			} else {
				// Fallback to window alarm if exact alarm permission was revoked
				alarmManager.setAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					triggerAtWallClock,
					pendingIntent,
				)
			}
		} else {
			alarmManager.setExactAndAllowWhileIdle(
				AlarmManager.RTC_WAKEUP,
				triggerAtWallClock,
				pendingIntent,
			)
		}
	}

	fun cancel() {
		val intent = Intent(context, AlarmReceiver::class.java).apply {
			action = ACTION_PHASE_ALARM
		}
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			ALARM_REQUEST_CODE,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
		)
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent)
			pendingIntent.cancel()
		}
	}

	companion object {
		const val ACTION_PHASE_ALARM = "io.github.melastore.stanza.ACTION_PHASE_ALARM"
		private const val ALARM_REQUEST_CODE = 4001
	}
}
