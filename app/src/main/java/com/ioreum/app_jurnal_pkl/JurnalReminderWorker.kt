package com.ioreum.app_jurnal_pkl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class JurnalReminderWorker(
    context: Context,
    private val params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val type = params.inputData.getString("type") ?: "masuk"
        val (title, message, id) = when (type) {
            "masuk" -> Triple("Isi Jurnal Masuk", "Hai, jangan lupa isi jurnal masuk hari ini!", 101)
            "pulang" -> Triple("Isi Jurnal Pulang", "Hai, jangan lupa isi jurnal pulang sebelum pulang!", 102)
            else -> Triple("Isi Jurnal", "Isi jurnal harian PKL", 100)
        }

        val channelId = "jurnal_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pengingat Jurnal", NotificationManager.IMPORTANCE_HIGH)
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)

        return Result.success()
    }
}
