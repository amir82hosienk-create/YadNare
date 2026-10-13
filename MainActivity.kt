package com.example.yadnare

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private lateinit var list: LinearLayout
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 32, 28, 24)
            setBackgroundColor(0xFFF7F5F0.toInt())
        }
        val title = TextView(this).apply {
            text = "یادت نره! ⏰"
            textSize = 30f; gravity = Gravity.CENTER
            setTextColor(0xFF202124.toInt())
        }
        val subtitle = TextView(this).apply {
            text = "کارهای مهمت رو از قلم ننداز، برار!"
            textSize = 16f; gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }
        input = EditText(this).apply {
            hint = "مثلاً: پرداخت قسط بانک"
            textSize = 17f
            setSingleLine(true)
        }
        val add = Button(this).apply {
            text = "＋ افزودن یادآور"
            setOnClickListener {
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val tasks = TaskStore.all(this@MainActivity)
                    tasks.add(0, ReminderTask(title = name))
                    TaskStore.save(this@MainActivity, tasks)
                    input.setText("")
                    renderTasks()
                }
            }
        }
        val sensor = Button(this).apply {
            text = "فعال‌کردن تشخیص برداشتن گوشی"
            setOnClickListener {
                try {
                    if (Build.VERSION.SDK_INT >= 26) startForegroundService(Intent(this@MainActivity, PickupService::class.java))
                    else startService(Intent(this@MainActivity, PickupService::class.java))
                    Toast.makeText(this@MainActivity, "سرویس تشخیص حرکت شروع شد (اگر حسگر پشتیبانی کند)", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "اندروید اجازه شروع سرویس را نداد؛ از اعلان و روشن شدن صفحه استفاده کن.", Toast.LENGTH_LONG).show()
                }
            }
        }
        val info = TextView(this).apply {
            text = "هشدار صفحه روشن/بازشدن قفل فعال است. برای تشخیص حرکت، دکمه زیر را بزن. ممکن است مدل گوشی از حسگر حرکت پشتیبانی نکند."
            textSize = 13f; setPadding(0, 8, 0, 12)
        }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(title)
        root.addView(subtitle)
        root.addView(input, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(add)
        root.addView(sensor)
        root.addView(info)
        root.addView(list)
        setContentView(ScrollView(this).apply { addView(root) })
        renderTasks()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 42)
        }
    }

    private fun renderTasks() {
        list.removeAllViews()
        val tasks = TaskStore.all(this)
        if (tasks.isEmpty()) {
            list.addView(TextView(this).apply { text = "هنوز یادآوری نداری؛ اولین موردت رو اضافه کن 🙂"; textSize = 16f; setPadding(0, 18, 0, 8) })
        }
        tasks.forEach { task ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 10, 14, 10)
                setBackgroundColor(if (task.done) 0xFFE0E9E0.toInt() else 0xFFFFFFFF.toInt())
            }
            val label = TextView(this).apply {
                text = (if (task.done) "✅ " else "🔔 ") + task.title
                textSize = 18f; setTextColor(0xFF202124.toInt())
            }
            row.addView(label)
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            if (!task.done) {
                actions.addView(Button(this@MainActivity).apply {
                    text = "انجام شد"
                    setOnClickListener {
                        TaskStore.update(this@MainActivity, task.id) { it.copy(done = true) }
                        renderTasks()
                    }
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                actions.addView(Button(this@MainActivity).apply {
                    text = "یادآوری ۱ ساعت بعد"
                    setOnClickListener {
                        val at = System.currentTimeMillis() + 60 * 60 * 1000L
                        TaskStore.update(this@MainActivity, task.id) { it.copy(snoozedUntil = at) }
                        ReminderReceiver.schedule(this@MainActivity, task.id, at)
                        Toast.makeText(this@MainActivity, "باشه، یک ساعت دیگه!", Toast.LENGTH_SHORT).show()
                        renderTasks()
                    }
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            row.addView(actions)
            val params = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 8, 0, 8)
            list.addView(row, params)
        }
    }
}
