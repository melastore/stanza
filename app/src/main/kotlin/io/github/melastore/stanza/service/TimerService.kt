package io.github.melastore.stanza.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import io.github.melastore.stanza.StanzaApp

class TimerService : Service() {

	override fun onBind(intent: Intent?): IBinder? = null

	override fun onCreate() {
		super.onCreate()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannels.createChannels(this)
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		val action = intent?.action ?: ACTION_START_SERVICE
		val app = application as StanzaApp
		val currentState = app.container.stateStore.get()

		when (action) {
			ACTION_STOP_SERVICE -> {
				ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
				stopSelf()
			}

			ACTION_UPDATE_NOTIFICATION -> {
				val notification = NotificationFactory.buildOngoingNotification(this, currentState)
				val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
				manager.notify(NotificationFactory.NOTIFICATION_ID_ONGOING, notification)
			}

			else -> {
				val notification = NotificationFactory.buildOngoingNotification(this, currentState)
				val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
					ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
				} else {
					0
				}
				ServiceCompat.startForeground(
					this,
					NotificationFactory.NOTIFICATION_ID_ONGOING,
					notification,
					fgsType,
				)
			}
		}

		return START_STICKY
	}

	companion object {
		const val ACTION_START_SERVICE = "io.github.melastore.stanza.ACTION_START_SERVICE"
		const val ACTION_UPDATE_NOTIFICATION = "io.github.melastore.stanza.ACTION_UPDATE_NOTIFICATION"
		const val ACTION_STOP_SERVICE = "io.github.melastore.stanza.ACTION_STOP_SERVICE"

		fun start(context: Context) {
			val intent = Intent(context, TimerService::class.java).apply {
				action = ACTION_START_SERVICE
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(intent)
			} else {
				context.startService(intent)
			}
		}

		fun update(context: Context) {
			val intent = Intent(context, TimerService::class.java).apply {
				action = ACTION_UPDATE_NOTIFICATION
			}
			context.startService(intent)
		}

		fun stop(context: Context) {
			val intent = Intent(context, TimerService::class.java).apply {
				action = ACTION_STOP_SERVICE
			}
			context.startService(intent)
		}
	}
}
