package com.jarly.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jarly.R
import com.jarly.domain.Freshness
import com.jarly.domain.FreshLogic
import java.time.LocalDate
import java.time.ZoneId

object Alerts {

    const val CHANNEL_ID = "jarly_reminders"
    private const val REQUEST_CODE = 9001

    fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_helper),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    fun refresh(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snapshot = JarlyStore.sync(context)
        val today = LocalDate.now()
        val due = snapshot.pantry
            .mapNotNull { item ->
                item.expiryEpochDay?.let { expiry ->
                    Triple(item, expiry, FreshLogic.of(expiry, today))
                }
            }
            .filter { (_, _, freshness) -> freshness != Freshness.LATER && freshness != Freshness.NO_DATE }
            .sortedBy { it.second }

        if (due.isEmpty()) {
            alarmManager.cancel(pendingIntent(context))
            return
        }

        val next = due.first().second
        val triggerAt = LocalDate.ofEpochDay(next)
            .atTime(9, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pi = pendingIntent(context)
        alarmManager.cancel(pi)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 15 * 60 * 1000L, pi)
        }
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, AlertReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun notification(context: Context, lines: List<String>): android.app.Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(lines.joinToString(" · "))
            .setStyle(
                NotificationCompat.InboxStyle().also { style ->
                    lines.take(3).forEach { style.addLine(it) }
                },
            )
            .setAutoCancel(true)
            .build()
}

class AlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val snapshot = JarlyStore.sync(context)
        val today = LocalDate.now()
        val lines = snapshot.pantry
            .mapNotNull { item ->
                item.expiryEpochDay?.let { expiry -> item to FreshLogic.of(expiry, today) }
            }
            .filter { (_, freshness) -> freshness != Freshness.LATER && freshness != Freshness.NO_DATE }
            .sortedBy { (item, _) -> item.expiryEpochDay }
            .take(3)
            .map { (item, _) -> item.name }
        if (lines.isEmpty()) {
            Alerts.refresh(context)
            return
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(2901, Alerts.notification(context, lines))
        Alerts.refresh(context)
    }
}

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Alerts.refresh(context)
        }
    }
}
