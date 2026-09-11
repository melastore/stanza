package io.github.melastore.stanza

import android.app.Application
import android.os.Build
import io.github.melastore.stanza.di.AppContainer
import io.github.melastore.stanza.domain.TimerIntent
import io.github.melastore.stanza.service.NotificationChannels

class StanzaApp : Application() {

	lateinit var container: AppContainer
		private set

	override fun onCreate() {
		super.onCreate()
		container = AppContainer(this)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannels.createChannels(this)
		}
		// Re-arm alarm and service after force-stop or an OEM task kill, not just reboot.
		container.timerController.dispatch(TimerIntent.ReconcileOnBoot)
	}
}
