package io.github.melastore.stanza.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.melastore.stanza.StanzaApp
import io.github.melastore.stanza.domain.TimerIntent

class BootReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		when (intent?.action) {
			Intent.ACTION_BOOT_COMPLETED,
			Intent.ACTION_MY_PACKAGE_REPLACED,
			-> {
				val app = context.applicationContext as StanzaApp
				app.container.timerController.dispatch(TimerIntent.ReconcileOnBoot)
			}
		}
	}
}
