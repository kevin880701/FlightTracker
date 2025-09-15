package com.lhr.flighttracker.core.notification

import android.content.Context
import androidx.work.*
import com.lhr.flighttracker.features.flightScheduled.domain.entity.FlightStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleNotification(flight: FlightStatus, langCode: String) {
        // 1. 計算延遲時間
        val delay = calculateDelay(flight.expectTime)
        if (delay <= 0) return // 如果時間已過，則不排程

        // 2. 準備要傳遞給 Worker 的資料
        val flightId = generateFlightId(flight)
        val flightInfo = "${flight.airLineName.get(langCode)} ${flight.airLineNum}"
        val flightTime = "預計 ${flight.expectTime}"

        val inputData = workDataOf(
            NotificationWorker.KEY_FLIGHT_ID to flightId,
            NotificationWorker.KEY_FLIGHT_INFO_TEXT to flightInfo,
            NotificationWorker.KEY_FLIGHT_TIME_TEXT to flightTime
        )

        // 3. 建立 WorkRequest
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        // 4. 加入佇列，使用唯一的 ID 以便之後可以取消
        workManager.enqueueUniqueWork(
            flightId,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelNotification(flightId: String) {
        workManager.cancelUniqueWork(flightId)
    }

    private fun calculateDelay(expectTime: String): Long {
//        return 3000L
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            // 假設航班日期是今天
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val departureDateTime = sdf.parse("$todayDate $expectTime") ?: return -1

            val tenMinutesInMillis = TimeUnit.MINUTES.toMillis(10)
            val triggerTime = departureDateTime.time - tenMinutesInMillis
            val currentTime = System.currentTimeMillis()

            return triggerTime - currentTime
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    private fun generateFlightId(flight: FlightStatus): String {
        return "${flight.airLineNum}-${flight.upAirportCode}-${flight.expectTime}"
    }
}