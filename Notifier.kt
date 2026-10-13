package com.example.yadnare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object Notifier {
    const val CHANNEL = "reminder_alerts"

    fun show(context: Context, task: ReminderTask) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            val channel = NotificationChannel(CHANNEL, "یادآوری‌های مهم", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "هشدار کارهای انجام‌نشده"
            channel.setSound(sound, attrs)
            channel.enableVibration(true)
            nm.createNotificationChannel(channel)
        }

        fun action(label: String, action: String, request: Int): PendingIntent {
            val i = Intent(context, TaskActionReceiver::class.java)
                .setAction(action).putExtra("task_id", task.id)
            return PendingIntent.getBroadcast(context, task.id.hashCode() + request, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= 26)
            android.app.Notification.Builder(context, CHANNEL)
        else @Suppress("DEPRECATION") android.app.Notification.Builder(context)

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("یادت نره! ⏰")
            .setContentText("${task.title} — انجامش دادی؟")
            .setStyle(android.app.Notification.BigTextStyle().bigText("${task.title}\nداداش، انجامش دادی؟"))
            .setContentIntent(open).setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "انجام شد", action("Done", TaskActionReceiver.DONE, 1))
            .addAction(android.R.drawable.ic_popup_reminder, "الان انجام می‌دم", action("Soon", TaskActionReceiver.SOON, 2))
            .addAction(android.R.drawable.ic_menu_recent_history, "بعداً", action("Later", TaskActionReceiver.LATER, 3))
            .build()
        nm.notify(task.id.hashCode(), notification)
    }
}
