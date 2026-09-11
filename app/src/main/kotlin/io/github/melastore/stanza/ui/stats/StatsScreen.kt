package io.github.melastore.stanza.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.data.db.SessionRecord
import io.github.melastore.stanza.domain.Phase
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.theme.LocalPalette
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
	todayMinutes: Int,
	weekMinutes: Int,
	streakDays: Int,
	yearHeatmap: Map<String, Int>,
	recentSessions: List<SessionRecord>,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	modifier: Modifier = Modifier,
) {
	LazyColumn(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 20.dp),
		contentPadding = PaddingValues(bottom = 90.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		item {
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = "Insights",
				fontSize = 28.sp,
				fontWeight = FontWeight.Light,
				color = Color.White,
			)
		}

		item {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
			) {
				MetricCard(
					title = "Today",
					value = "${todayMinutes}m",
					modifier = Modifier.weight(1f),
					hazeState = hazeState,
					reduceTransparency = reduceTransparency,
				)
				MetricCard(
					title = "This Week",
					value = "${weekMinutes}m",
					modifier = Modifier.weight(1f),
					hazeState = hazeState,
					reduceTransparency = reduceTransparency,
				)
				MetricCard(
					title = "Streak",
					value = "${streakDays}d",
					modifier = Modifier.weight(1f),
					hazeState = hazeState,
					reduceTransparency = reduceTransparency,
				)
			}
		}

		item {
			GlassSurface(
				hazeState = hazeState,
				reduceTransparency = reduceTransparency,
				variant = GlassVariant.CARD,
				shape = RoundedCornerShape(22.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Annual Focus Rhythm",
						fontSize = 15.sp,
						fontWeight = FontWeight.Medium,
						color = Color.White.copy(alpha = 0.9f),
					)
					Spacer(modifier = Modifier.height(12.dp))

					AnnualHeatmapView(heatmap = yearHeatmap)
				}
			}
		}

		item {
			Text(
				text = "Recent Stanzas",
				fontSize = 18.sp,
				fontWeight = FontWeight.Medium,
				color = Color.White.copy(alpha = 0.85f),
				modifier = Modifier.padding(top = 8.dp),
			)
		}

		if (recentSessions.isEmpty()) {
			item {
				Text(
					text = "Complete your first stanza to start tracking.",
					fontSize = 14.sp,
					color = Color.White.copy(alpha = 0.4f),
				)
			}
		} else {
			items(recentSessions, key = { it.id }) { session ->
				RecentSessionRow(
					session = session,
					hazeState = hazeState,
					reduceTransparency = reduceTransparency,
				)
			}
		}
	}
}

@Composable
private fun MetricCard(
	title: String,
	value: String,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	modifier: Modifier = Modifier,
) {
	val palette = LocalPalette.current
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CARD,
		shape = RoundedCornerShape(20.dp),
		modifier = modifier,
	) {
		Column(
			modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Text(
				text = title,
				fontSize = 12.sp,
				color = Color.White.copy(alpha = 0.55f),
				fontWeight = FontWeight.Normal,
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = value,
				fontSize = 22.sp,
				fontWeight = FontWeight.SemiBold,
				color = palette.focusPrimary,
			)
		}
	}
}

@Composable
private fun AnnualHeatmapView(heatmap: Map<String, Int>) {
	val palette = LocalPalette.current
	val scrollState = rememberScrollState(Int.MAX_VALUE)
	val zone = ZoneId.systemDefault()
	val today = LocalDate.now(zone)
	val formatter = DateTimeFormatter.ISO_LOCAL_DATE

	// Starts scrolled to the newest week
	val totalWeeks = 30
	val startDate = today.minusWeeks(totalWeeks.toLong()).minusDays(today.dayOfWeek.value.toLong() - 1)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(scrollState),
		horizontalArrangement = Arrangement.spacedBy(3.dp),
	) {
		for (w in 0 until totalWeeks) {
			Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
				for (d in 0 until 7) {
					val date = startDate.plusWeeks(w.toLong()).plusDays(d.toLong())
					val dateKey = date.format(formatter)
					val minutes = heatmap[dateKey] ?: 0

					val cellColor = when {
						date.isAfter(today) -> Color.Transparent
						minutes == 0 -> Color.White.copy(alpha = 0.05f)
						minutes < 30 -> palette.focusSecondary.copy(alpha = 0.45f)
						minutes < 60 -> palette.focusPrimary.copy(alpha = 0.75f)
						else -> palette.focusPrimary
					}

					Box(
						modifier = Modifier
							.size(11.dp)
							.clip(RoundedCornerShape(2.5.dp))
							.background(cellColor),
					)
				}
			}
		}
	}
}

@Composable
private fun RecentSessionRow(session: SessionRecord, hazeState: HazeState, reduceTransparency: Boolean,) {
	val palette = LocalPalette.current
	val zone = ZoneId.systemDefault()
	val timeStr = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
		.format(Instant.ofEpochMilli(session.timestamp).atZone(zone))

	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = GlassVariant.CARD,
		shape = RoundedCornerShape(16.dp),
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column {
				Text(
					text = when (session.phase) {
						Phase.SHORT_BREAK -> "Short break"
						Phase.LONG_BREAK -> "Long break"
						else -> "Focus stanza"
					},
					fontSize = 14.sp,
					fontWeight = FontWeight.Medium,
					color = Color.White,
				)
				Text(
					text = timeStr,
					fontSize = 12.sp,
					color = Color.White.copy(alpha = 0.45f),
				)
			}
			Text(
				text = "+${session.durationMinutes}m",
				fontSize = 15.sp,
				fontWeight = FontWeight.SemiBold,
				color = palette.focusPrimary,
			)
		}
	}
}
