package com.example.yadnare

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ReminderTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val done: Boolean = false,
    val snoozedUntil: Long = 0L
)

object TaskStore {
    private const val PREFS = "yadnare_tasks"
    private const val KEY = "tasks"

    fun all(context: Context): MutableList<ReminderTask> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<ReminderTask>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(ReminderTask(
                id = o.optString("id", UUID.randomUUID().toString()),
                title = o.optString("title", ""),
                done = o.optBoolean("done", false),
                snoozedUntil = o.optLong("snoozedUntil", 0L)
            ))
        }
        return out
    }

    fun save(context: Context, tasks: List<ReminderTask>) {
        val arr = JSONArray()
        tasks.forEach { task ->
            arr.put(JSONObject().put("id", task.id).put("title", task.title)
                .put("done", task.done).put("snoozedUntil", task.snoozedUntil))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }

    fun update(context: Context, id: String, transform: (ReminderTask) -> ReminderTask) {
        save(context, all(context).map { if (it.id == id) transform(it) else it })
    }
}
