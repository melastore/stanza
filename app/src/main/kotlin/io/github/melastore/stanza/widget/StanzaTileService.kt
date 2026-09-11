package io.github.melastore.stanza.widget

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import io.github.melastore.stanza.R
import io.github.melastore.stanza.StanzaApp
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.TimerIntent

@RequiresApi(Build.VERSION_CODES.N)
class StanzaTileService : TileService() {

	override fun onStartListening() {
		super.onStartListening()
		updateTile()
	}

	override fun onClick() {
		super.onClick()
		val app = application as StanzaApp
		val currentState = app.container.stateStore.get()

		when {
			currentState.isIdle -> {
				app.container.timerController.dispatch(TimerIntent.Start(Phase.FOCUS))
			}

			currentState.isRunning -> {
				app.container.timerController.dispatch(TimerIntent.Pause)
			}

			currentState.isPaused -> {
				app.container.timerController.dispatch(TimerIntent.Resume)
			}
		}

		updateTile()
	}

	private fun updateTile() {
		val tile = qsTile ?: return
		val app = application as StanzaApp
		val state = app.container.stateStore.get()

		when {
			state.isRunning -> {
				tile.state = Tile.STATE_ACTIVE
				tile.label = if (state.phase.isBreak) "Break" else "Focusing"
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					tile.subtitle = "Active"
				}
			}

			state.isPaused -> {
				tile.state = Tile.STATE_INACTIVE
				tile.label = "Paused"
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					tile.subtitle = "Tap to resume"
				}
			}

			else -> {
				tile.state = Tile.STATE_INACTIVE
				tile.label = "Start Stanza"
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					tile.subtitle = "25m Focus"
				}
			}
		}

		tile.icon = Icon.createWithResource(this, R.drawable.ic_timer)
		tile.updateTile()
	}
}
