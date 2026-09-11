package io.github.melastore.stanza.ui.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

data class PermissionStatus(val notifications: Boolean, val exactAlarms: Boolean, val batteryUnrestricted: Boolean,) {
	// Without notifications there is no ongoing timer and no end-of-session alert.
	val missingRequired: Boolean get() = !notifications

	val allGranted: Boolean get() = notifications && exactAlarms && batteryUnrestricted
}

fun readPermissionStatus(context: Context): PermissionStatus {
	val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		ContextCompat.checkSelfPermission(
			context,
			Manifest.permission.POST_NOTIFICATIONS,
		) == PackageManager.PERMISSION_GRANTED
	} else {
		true
	}

	val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

	return PermissionStatus(
		notifications = notifications,
		exactAlarms = alarmManager.canScheduleExactAlarms(),
		batteryUnrestricted = powerManager.isIgnoringBatteryOptimizations(context.packageName),
	)
}

// Re-read on resume: the exact-alarm and battery screens are system settings, so the answer
// only arrives when the user comes back.
@Composable
fun rememberPermissionStatus(): PermissionStatus {
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	var status by remember { mutableStateOf(readPermissionStatus(context)) }

	DisposableEffect(lifecycleOwner) {
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME) {
				status = readPermissionStatus(context)
			}
		}
		lifecycleOwner.lifecycle.addObserver(observer)
		onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
	}

	return status
}

fun exactAlarmSettingsIntent(context: Context): Intent =
	Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${context.packageName}".toUri())

fun batteryExemptionIntent(context: Context): Intent =
	Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:${context.packageName}".toUri())

fun appSettingsIntent(context: Context): Intent =
	Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())

private fun String.toUri(): Uri = Uri.parse(this)
