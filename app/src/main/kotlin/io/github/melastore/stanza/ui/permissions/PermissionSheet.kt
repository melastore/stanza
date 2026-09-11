package io.github.melastore.stanza.ui.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import io.github.melastore.stanza.ui.glass.GlassSurface
import io.github.melastore.stanza.ui.glass.GlassVariant
import io.github.melastore.stanza.ui.theme.LocalPalette

// First-launch sheet. The system dialog is only shown when the user taps a row.
@Composable
fun PermissionSheet(
	status: PermissionStatus,
	hazeState: HazeState,
	reduceTransparency: Boolean,
	onDismiss: () -> Unit,
) {
	val context = LocalContext.current
	val palette = LocalPalette.current

	val notificationLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
	) { }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.55f))
			// Swallow taps: a disabled clickable lets them through to the screen underneath.
			.pointerInput(Unit) { detectTapGestures { } }
			.padding(24.dp),
	) {
		GlassSurface(
			hazeState = hazeState,
			reduceTransparency = reduceTransparency,
			variant = GlassVariant.RAISED,
			shape = RoundedCornerShape(26.dp),
			modifier = Modifier.widthIn(max = 420.dp),
		) {
			Column(modifier = Modifier.padding(24.dp)) {
				Text(
					text = "Before you start",
					fontSize = 20.sp,
					color = Color.White,
				)
				Spacer(modifier = Modifier.height(6.dp))
				Text(
					text = "Stanza keeps time while you are in another app or the screen is off. " +
						"Three things make that reliable.",
					fontSize = 13.sp,
					color = Color.White.copy(alpha = 0.6f),
				)

				Spacer(modifier = Modifier.height(22.dp))

				PermissionRow(
					title = "Notifications",
					detail = "Shows the countdown and tells you when a stanza ends",
					required = true,
					granted = status.notifications,
					accent = palette.focusPrimary,
					onGrant = {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
							notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
						} else {
							context.startActivity(appSettingsIntent(context))
						}
					},
				)

				PermissionRow(
					title = "Exact alarms",
					detail = "Ends a session on the minute instead of whenever the system wakes up",
					required = false,
					granted = status.exactAlarms,
					accent = palette.focusPrimary,
					onGrant = { context.startActivity(exactAlarmSettingsIntent(context)) },
				)

				PermissionRow(
					title = "Unrestricted battery",
					detail = "Stops aggressive power saving from killing a running timer",
					required = false,
					granted = status.batteryUnrestricted,
					accent = palette.focusPrimary,
					onGrant = { context.startActivity(batteryExemptionIntent(context)) },
				)

				Spacer(modifier = Modifier.height(18.dp))

				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.fillMaxWidth()
						.clip(RoundedCornerShape(14.dp))
						.background(palette.focusPrimary)
						.clickable { onDismiss() }
						.padding(vertical = 13.dp),
				) {
					Text(
						text = if (status.allGranted) "Start focusing" else "Continue",
						fontSize = 14.sp,
						color = palette.ink,
					)
				}

				if (!status.allGranted) {
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "You can change these later in Settings.",
						fontSize = 11.sp,
						color = Color.White.copy(alpha = 0.45f),
						modifier = Modifier.fillMaxWidth(),
					)
				}
			}
		}
	}
}

@Composable
fun PermissionRow(
	title: String,
	detail: String,
	required: Boolean,
	granted: Boolean,
	accent: Color,
	onGrant: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Column(modifier = Modifier.weight(1f)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(text = title, fontSize = 14.sp, color = Color.White)
				if (required && !granted) {
					Spacer(modifier = Modifier.size(6.dp))
					Text(
						text = "REQUIRED",
						fontSize = 9.sp,
						letterSpacing = 1.sp,
						color = accent,
					)
				}
			}
			Text(
				text = detail,
				fontSize = 12.sp,
				color = Color.White.copy(alpha = 0.55f),
			)
		}

		Spacer(modifier = Modifier.size(12.dp))

		if (granted) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.size(28.dp)
					.clip(CircleShape)
					.background(accent.copy(alpha = 0.2f)),
			) {
				Icon(
					imageVector = Icons.Default.Check,
					contentDescription = "Granted",
					tint = accent,
					modifier = Modifier.size(16.dp),
				)
			}
		} else {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.clip(RoundedCornerShape(10.dp))
					.background(Color.White.copy(alpha = 0.12f))
					.clickable { onGrant() }
					.padding(horizontal = 14.dp, vertical = 7.dp),
			) {
				Text(text = "Allow", fontSize = 12.sp, color = Color.White)
			}
		}
	}
}

@Composable
fun PermissionStatusList(status: PermissionStatus, accent: Color) {
	val context = LocalContext.current
	val notificationLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
	) { }

	Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
		PermissionRow(
			title = "Notifications",
			detail = if (status.notifications) "Granted" else "Timer and alerts cannot be shown",
			required = true,
			granted = status.notifications,
			accent = accent,
			onGrant = {
				// A second denial is permanent, so send repeat visits to app settings.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
				} else {
					context.startActivity(appSettingsIntent(context))
				}
			},
		)
		PermissionRow(
			title = "Exact alarms",
			detail = if (status.exactAlarms) "Granted" else "Sessions may end late",
			required = false,
			granted = status.exactAlarms,
			accent = accent,
			onGrant = { context.startActivity(exactAlarmSettingsIntent(context)) },
		)
		PermissionRow(
			title = "Unrestricted battery",
			detail = if (status.batteryUnrestricted) "Granted" else "Power saving may kill the timer",
			required = false,
			granted = status.batteryUnrestricted,
			accent = accent,
			onGrant = { context.startActivity(batteryExemptionIntent(context)) },
		)
	}
}
