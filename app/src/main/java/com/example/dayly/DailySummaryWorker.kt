package com.example.dayly

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailySummaryWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val activities = DaylyDataStore.loadActivities(context)
        if (activities.isEmpty()) return Result.success()

        val completed = activities.count { it.completed }
        val total = activities.size
        val percent = (completed * 100) / total
        val missed = total - completed

        showNotification(percent, missed)

        return Result.success()
    }

    private fun showNotification(percent: Int, missed: Int) {
        val channelId = "dayly_summary"

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Today's Progress")
            .setContentText("Completed $percent% • Missed $missed tasks")
            .build()

        manager.notify(1, notification)
    }
}
