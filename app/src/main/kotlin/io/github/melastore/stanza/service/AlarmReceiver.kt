package io.github.melastore.stanza.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.melastore.stanza.StanzaApp
import io.github.melastore.stanza.domain.TimerIntent

class AlarmReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent?.action == AlarmScheduler.ACTION_PHASE_ALARM) {
			val app = context.applicationContext as StanzaApp
			app.container.timerController.dispatch(TimerIntent.PhaseCompleted)
		}
	}
}
