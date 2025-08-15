package com.lhr.flighttracker.core.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lhr.flighttracker.R

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "flight_reminder_channel"
        const val KEY_FLIGHT_ID = "key_flight_id"
        const val KEY_FLIGHT_INFO_TEXT = "key_flight_info_text"
        const val KEY_FLIGHT_TIME_TEXT = "key_flight_time_text"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        // 從傳入的參數中獲取航班資訊
        val flightId = inputData.getString(KEY_FLIGHT_ID) ?: return Result.failure()
        val flightInfoText = inputData.getString(KEY_FLIGHT_INFO_TEXT) ?: ""
        val flightTimeText = inputData.getString(KEY_FLIGHT_TIME_TEXT) ?: ""

        // 發送通知
        sendNotification(
            notificationId = flightId.hashCode(),
            title = context.getString(R.string.flight_notification),
            contentText = "$flightInfoText $flightTimeText"
        )

        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(notificationId: Int, title: String, contentText: String) {
        val notificationManager = NotificationManagerCompat.from(context)

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_add) // 請換成您的 App 圖示
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // 檢查通知權限
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, builder.build())
        }
    }
}