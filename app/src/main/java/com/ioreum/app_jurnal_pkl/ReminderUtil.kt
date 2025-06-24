package com.ioreum.app_jurnal_pkl

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderUtil {

    fun scheduleDailyReminder(context: Context) {
        scheduleReminder(context, 7, 15, "masuk", "ReminderMasuk")
        scheduleReminder(context, 16, 0, "pulang", "ReminderPulang")
    }

    private fun scheduleReminder(
        context: Context,
        hour: Int,
        minute: Int,
        type: String,
        uniqueName: String
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val data = workDataOf("type" to type)

        val workRequest = OneTimeWorkRequestBuilder<JurnalReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
