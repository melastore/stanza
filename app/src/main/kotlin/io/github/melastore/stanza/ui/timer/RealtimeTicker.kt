package io.github.melastore.stanza.ui.timer

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// Shared "now" so the edge progress and the digits never drift apart.
@Composable
fun rememberRealtimeTicker(active: Boolean): Long {
	var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
	LaunchedEffect(active) {
		while (active) {
			now = SystemClock.elapsedRealtime()
			delay(500)
		}
		now = SystemClock.elapsedRealtime()
	}
	return now
}
