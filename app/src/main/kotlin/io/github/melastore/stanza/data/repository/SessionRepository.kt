package io.github.melastore.stanza.data.repository

import io.github.melastore.stanza.data.db.SessionRecord
import io.github.melastore.stanza.data.db.StanzaDatabase
import io.github.melastore.stanza.domain.Phase
import java.time.LocalDate
import java.time.ZoneId

interface SessionRepository {
	suspend fun logSession(phase: Phase, durationMinutes: Int, timestamp: Long, taskId: Long?): Long
	suspend fun getTodayFocusMinutes(): Int
	suspend fun getWeekFocusMinutes(): Int
	suspend fun getYearHeatmap(): Map<String, Int>
	suspend fun getRecentSessions(limit: Int = 30): List<SessionRecord>
	suspend fun getCurrentStreak(): Int
}

class SqliteSessionRepository(private val db: StanzaDatabase) : SessionRepository {

	override suspend fun logSession(phase: Phase, durationMinutes: Int, timestamp: Long, taskId: Long?,): Long =
		db.insertSession(phase, durationMinutes, timestamp, taskId)

	override suspend fun getTodayFocusMinutes(): Int {
		val zone = ZoneId.systemDefault()
		val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
		return db.getTodayFocusMinutes(startOfToday)
	}

	override suspend fun getWeekFocusMinutes(): Int {
		val zone = ZoneId.systemDefault()
		val startOfWeek = LocalDate.now(zone).minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
		return db.getWeekFocusMinutes(startOfWeek)
	}

	override suspend fun getYearHeatmap(): Map<String, Int> {
		val zone = ZoneId.systemDefault()
		val oneYearAgo = LocalDate.now(zone).minusDays(365).atStartOfDay(zone).toInstant().toEpochMilli()
		return db.getYearHeatmap(oneYearAgo)
	}

	override suspend fun getRecentSessions(limit: Int): List<SessionRecord> = db.getRecentSessions(limit)

	override suspend fun getCurrentStreak(): Int = db.calculateStreak()
}
