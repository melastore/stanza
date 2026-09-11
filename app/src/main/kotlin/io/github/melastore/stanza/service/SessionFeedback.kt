package io.github.melastore.stanza.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.VibrationEffect
import android.os.VibratorManager
import io.github.melastore.stanza.domain.Phase
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Chime is synthesised instead of bundled. Focus ends on a falling pair of notes, a break on a
// rising pair.
class SessionFeedback(private val context: Context) {

	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

	fun play(completedPhase: Phase, sound: Boolean, vibration: Boolean) {
		if (vibration) vibrate(completedPhase)
		if (sound) scope.launch { chime(completedPhase) }
	}

	private fun vibrate(completedPhase: Phase) {
		val vibrator = context.getSystemService(VibratorManager::class.java)?.defaultVibrator ?: return
		if (!vibrator.hasVibrator()) return

		val pattern = if (completedPhase == Phase.FOCUS) {
			longArrayOf(0, 130, 90, 130, 90, 280)
		} else {
			longArrayOf(0, 90, 80, 90)
		}
		runCatching { vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
	}

	private suspend fun chime(completedPhase: Phase) {
		val notes = if (completedPhase == Phase.FOCUS) {
			listOf(880.0, 587.33)
		} else {
			listOf(587.33, 880.0)
		}

		val samples = render(notes)
		val track = runCatching {
			AudioTrack.Builder()
				.setAudioAttributes(
					AudioAttributes.Builder()
						.setUsage(AudioAttributes.USAGE_ALARM)
						.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
						.build(),
				)
				.setAudioFormat(
					AudioFormat.Builder()
						.setEncoding(AudioFormat.ENCODING_PCM_16BIT)
						.setSampleRate(SAMPLE_RATE)
						.setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
						.build(),
				)
				.setBufferSizeInBytes(samples.size * 2)
				.setTransferMode(AudioTrack.MODE_STATIC)
				.build()
		}.getOrNull() ?: return

		runCatching {
			track.write(samples, 0, samples.size)
			track.play()
			delay(TAIL_MS)
		}
		runCatching { track.release() }
	}

	private fun render(notes: List<Double>): ShortArray {
		val total = ((NOTE_SECONDS + OFFSET_SECONDS * (notes.size - 1)) * SAMPLE_RATE).toInt()
		val buffer = ShortArray(total)
		val noteSamples = (NOTE_SECONDS * SAMPLE_RATE).toInt()

		notes.forEachIndexed { index, frequency ->
			val start = (OFFSET_SECONDS * index * SAMPLE_RATE).toInt()
			for (n in 0 until noteSamples) {
				val at = start + n
				if (at >= total) break
				val t = n.toDouble() / SAMPLE_RATE
				// Short attack ramp, otherwise the note starts with a click.
				val envelope = exp(-t * 3.6) * (1 - exp(-t * 500.0))
				val wave = sin(TWO_PI * frequency * t) + sin(TWO_PI * frequency * 2 * t) * 0.22
				val mixed = buffer[at] + wave * envelope * 0.3 * Short.MAX_VALUE
				buffer[at] = mixed.coerceIn(MIN_PCM, MAX_PCM).toInt().toShort()
			}
		}
		return buffer
	}

	private companion object {
		const val SAMPLE_RATE = 44_100
		const val NOTE_SECONDS = 0.95
		const val OFFSET_SECONDS = 0.19
		const val TAIL_MS = 1_400L
		const val TWO_PI = 2 * PI
		const val MIN_PCM = -32_768.0
		const val MAX_PCM = 32_767.0
	}
}
