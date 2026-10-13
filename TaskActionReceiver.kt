package com.example.yadnare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class TaskActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("task_id") ?: return
        when (intent.action) {
            DONE -> TaskStore.update(context, id) { it.copy(done = true, snoozedUntil = 0L) }
            SOON -> {
                val at = System.currentTimeMillis() + 10 * 60 * 1000L
                TaskStore.update(context, id) { it.copy(snoozedUntil = at) }
                ReminderReceiver.schedule(context, id, at)
            }
            LATER -> {
                val at = System.currentTimeMillis() + 60 * 60 * 1000L
                TaskStore.update(context, id) { it.copy(snoozedUntil = at) }
                ReminderReceiver.schedule(context, id, at)
            }
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.cancel(id.hashCode())
        val msg = if (intent.action == DONE) "آفرین! انجام شد ثبت شد" else "باشه، دوباره یادت می‌اندازم"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val DONE = "com.example.yadnare.DONE"
        const val SOON = "com.example.yadnare.SOON"
        const val LATER = "com.example.yadnare.LATER"
    }
}
