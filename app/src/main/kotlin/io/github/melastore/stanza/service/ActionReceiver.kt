package io.github.melastore.stanza.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.melastore.stanza.StanzaApp
import io.github.melastore.stanza.domain.TimerIntent

class ActionReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		val action = intent?.action ?: return
		val app = context.applicationContext as StanzaApp
		val controller = app.container.timerController

		when (action) {
			NotificationFactory.ACTION_PAUSE -> controller.dispatch(TimerIntent.Pause)
			NotificationFactory.ACTION_RESUME -> controller.dispatch(TimerIntent.Resume)
			NotificationFactory.ACTION_SKIP -> controller.dispatch(TimerIntent.Skip)
			NotificationFactory.ACTION_ADD_FIVE -> controller.dispatch(TimerIntent.AddMinutes(5))
			NotificationFactory.ACTION_STOP -> controller.dispatch(TimerIntent.Stop)
			NotificationFactory.ACTION_START_NEXT -> controller.dispatch(TimerIntent.Start())
		}
	}
}
