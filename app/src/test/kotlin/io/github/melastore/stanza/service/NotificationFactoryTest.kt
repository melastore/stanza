package io.github.melastore.stanza.service

import android.app.Notification
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.domain.TimerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationFactoryTest {

	private val context: Context = ApplicationProvider.getApplicationContext()

	@Test
	fun ongoingNotification_whenRunning_usesChronometer() {
		val state = TimerState(
			phase = Phase.FOCUS,
			endsAtRealtime = 25 * 60_000L,
			endsAtWallClock = System.currentTimeMillis() + 25 * 60_000L,
		)
		val notification = NotificationFactory.buildOngoingNotification(context, state)

		assertNotNull(notification)
		assertEquals(NotificationChannels.CHANNEL_ONGOING, notification.channelId)
		assertTrue((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0)
	}

	@Test
	fun alertNotification_usesAlertChannelAndHasActions() {
		val notification = NotificationFactory.buildAlertNotification(
			context,
			completedPhase = Phase.FOCUS,
			nextPhase = Phase.SHORT_BREAK,
		)

		assertNotNull(notification)
		assertEquals(NotificationChannels.CHANNEL_ALERTS, notification.channelId)
		assertTrue(notification.actions != null && notification.actions.isNotEmpty())
	}
}
