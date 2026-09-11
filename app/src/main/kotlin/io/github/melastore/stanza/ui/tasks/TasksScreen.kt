package io.github.melastore.stanza.ui.tasks

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.data.db.TaskRecord
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.theme.LocalPalette

@Composable
fun TasksScreen(
	tasks: List<TaskRecord>,
	activeTaskId: Long?,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onSelectTask: (Long?) -> Unit,
	onToggleComplete: (Long, Boolean) -> Unit,
	onDeleteTask: (Long) -> Unit,
	onCreateTask: (String, Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	var showAddDialog by remember { mutableStateOf(false) }

	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 20.dp),
	) {
		Spacer(modifier = Modifier.height(20.dp))

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = "Tasks",
				fontSize = 28.sp,
				fontWeight = FontWeight.Light,
				color = Color.White,
			)

			GlassSurface(
				hazeState = hazeState,
				reduceTransparency = reduceTransparency,
				variant = GlassVariant.CONTROL,
				shape = CircleShape,
				modifier = Modifier
					.size(42.dp)
					.clip(CircleShape)
					.clickable { showAddDialog = true },
			) {
				Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = "New Task",
						tint = Color.White,
						modifier = Modifier.size(22.dp),
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		if (tasks.isEmpty()) {
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = "No tasks yet.\nTap + to plan your stanzas.",
					fontSize = 15.sp,
					color = Color.White.copy(alpha = 0.5f),
					textAlign = androidx.compose.ui.text.style.TextAlign.Center,
				)
			}
		} else {
			LazyColumn(
				contentPadding = PaddingValues(bottom = 90.dp),
				verticalArrangement = Arrangement.spacedBy(10.dp),
			) {
				items(tasks, key = { it.id }) { task ->
					val isCurrent = task.id == activeTaskId
					TaskRow(
						task = task,
						isCurrent = isCurrent,
						hazeState = hazeState,
						reduceTransparency = reduceTransparency,
						onSelect = { onSelectTask(if (isCurrent) null else task.id) },
						onToggleComplete = { onToggleComplete(task.id, it) },
						onDelete = { onDeleteTask(task.id) },
					)
				}
			}
		}
	}

	if (showAddDialog) {
		CreateTaskDialog(
			onDismiss = { showAddDialog = false },
			onConfirm = { title, est ->
				onCreateTask(title, est)
				showAddDialog = false
			},
		)
	}
}

@Composable
private fun TaskRow(
	task: TaskRecord,
	isCurrent: Boolean,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onSelect: () -> Unit,
	onToggleComplete: (Boolean) -> Unit,
	onDelete: () -> Unit,
) {
	val palette = LocalPalette.current
	GlassSurface(
		hazeState = hazeState,
		reduceTransparency = reduceTransparency,
		variant = if (isCurrent) GlassVariant.RAISED else GlassVariant.CARD,
		shape = RoundedCornerShape(18.dp),
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(18.dp))
			.clickable { onSelect() },
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 14.dp, vertical = 10.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Checkbox(
				checked = task.isCompleted,
				onCheckedChange = { onToggleComplete(it) },
				colors = CheckboxDefaults.colors(
					checkedColor = palette.focusPrimary,
					uncheckedColor = Color.White.copy(alpha = 0.4f),
				),
			)

			Spacer(modifier = Modifier.width(8.dp))

			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = task.title,
					fontSize = 16.sp,
					fontWeight = FontWeight.Medium,
					color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White,
					textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
				)

				Spacer(modifier = Modifier.height(2.dp))

				Text(
					text = "${task.completedPomodoros}/${task.estimatedPomodoros} stanzas${if (isCurrent) " • ACTIVE" else ""}",
					fontSize = 12.sp,
					color = if (isCurrent) palette.focusPrimary else Color.White.copy(alpha = 0.5f),
					fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
				)
			}

			IconButton(onClick = onDelete) {
				Icon(
					imageVector = Icons.Default.Delete,
					contentDescription = "Delete task",
					tint = Color.White.copy(alpha = 0.35f),
					modifier = Modifier.size(20.dp),
				)
			}
		}
	}
}

@Composable
private fun CreateTaskDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit,) {
	val palette = LocalPalette.current
	var title by remember { mutableStateOf("") }
	var estimated by remember { mutableIntStateOf(2) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("New Task") },
		text = {
			Column {
				OutlinedTextField(
					value = title,
					onValueChange = { title = it },
					label = { Text("What are you working on?") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth(),
				)

				Spacer(modifier = Modifier.height(16.dp))

				Text("Estimated stanzas: $estimated")
				Row(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.padding(top = 8.dp),
				) {
					for (i in 1..5) {
						TextButton(
							onClick = { estimated = i },
							modifier = Modifier.weight(1f),
						) {
							Text(
								text = i.toString(),
								fontWeight = if (estimated == i) FontWeight.Bold else FontWeight.Normal,
								color = if (estimated == i) palette.focusPrimary else Color.White.copy(alpha = 0.6f),
							)
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					if (title.isNotBlank()) {
						onConfirm(title, estimated)
					}
				},
			) {
				Text("Create", color = palette.focusPrimary)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Cancel")
			}
		},
	)
}
