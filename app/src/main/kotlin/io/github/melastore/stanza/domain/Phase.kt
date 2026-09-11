package io.github.melastore.stanza.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Phase {
	IDLE,
	FOCUS,
	SHORT_BREAK,
	LONG_BREAK;

	val isBreak: Boolean get() = this == SHORT_BREAK || this == LONG_BREAK
	val isFocus: Boolean get() = this == FOCUS
}
