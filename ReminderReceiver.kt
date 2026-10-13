package com.example.yadnare

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("task_id") ?: return
        val task = TaskStore.all(context).firstOrNull { it.id == id && !it.done } ?: return
        if (task.snoozedUntil > System.currentTimeMillis()) {
            schedule(context, task.id, task.snoozedUntil)
            return
        }
        Notifier.show(context, task)
    }

    companion object {
        fun schedule(context: Context, id: String, at: Long) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(context, ReminderReceiver::class.java).putExtra("task_id", id)
            val pi = PendingIntent.getBroadcast(context, id.hashCode(), i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.set(AlarmManager.RTC_WAKEUP, at, pi)
            }
        }
    }
}
