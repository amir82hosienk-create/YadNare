package com.example.yadnare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                val now = System.currentTimeMillis()
                val task = TaskStore.all(context).firstOrNull {
                    !it.done && it.snoozedUntil <= now
                }
                if (task != null) Notifier.show(context, task)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                TaskStore.all(context).filter { !it.done }.forEach {
                    val at = if (it.snoozedUntil > System.currentTimeMillis()) it.snoozedUntil
                             else System.currentTimeMillis() + 60_000L
                    ReminderReceiver.schedule(context, it.id, at)
                }
            }
        }
    }
}
