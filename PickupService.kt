package com.example.yadnare

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder

/**
 * Optional sensor-based pickup detector. Android may stop this service unless the user
 * explicitly starts it and grants required notification/background permissions.
 */
class PickupService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lastAlert = 0L
    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel("pickup_monitor", "تشخیص برداشتن گوشی", NotificationManager.IMPORTANCE_LOW))
        }
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "pickup_monitor")
                else @Suppress("DEPRECATION") Notification.Builder(this)
        startForeground(99, b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("یادت نره!").setContentText("یادآور هوشمند فعاله").build())
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    override fun onSensorChanged(event: SensorEvent?) {
        val now = System.currentTimeMillis()
        if (now - lastAlert < 60_000L) return
        val task = TaskStore.all(this).firstOrNull { !it.done && it.snoozedUntil <= now } ?: return
        lastAlert = now
        Notifier.show(this, task)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onDestroy() { sensorManager.unregisterListener(this); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
