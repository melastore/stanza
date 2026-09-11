package io.github.melastore.stanza.domain

data class TimerTransition(val state: TimerState, val effects: List<TimerSideEffect> = emptyList(),)
