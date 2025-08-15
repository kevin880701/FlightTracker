package com.lhr.flighttracker.features.flightScheduled.data.repositories

import com.lhr.flighttracker.core.utils.LanguageManager
import com.lhr.flighttracker.features.flightScheduled.data.datasources.FlightApiService
import com.lhr.flighttracker.features.flightScheduled.data.models.mappers.mapToFlightStatus
import com.lhr.flighttracker.features.flightScheduled.domain.entity.FlightStatus
import com.lhr.flighttracker.features.flightScheduled.domain.entity.LocalizedString
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FlightRepositoryImpl @Inject constructor(
    private val apiService: FlightApiService,
    private val languageManager: LanguageManager
) : FlightRepository {

    override suspend fun getArrivalFlights(): List<FlightStatus> = coroutineScope {
        // 使用 async 並行呼叫三支 API
        val rawArrivalsDeferred = async { apiService.getRawArrivalFlights() }
        val airportDataDeferred = async { apiService.getAirportData() }
        val airlineDataDeferred = async { apiService.getAirlineData() }

        // 等待所有 API 回應
        val rawArrivals = rawArrivalsDeferred.await()
        val airportData = airportDataDeferred.await()
            .firstOrNull { it.type == "table" }?.data ?: emptyList()
        val airlineData = airlineDataDeferred.await()
            .firstOrNull { it.type == "table" }?.data ?: emptyList()

        // 取得今天的日期字串 "yyyy-MM-dd"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).format(Date())

        rawArrivals
//            .filter { it.flightDate == today } // 只保留今天的航班
            .map { rawFlight ->
                val flightStatusList = mapToFlightStatus(rawArrivals, airportData, airlineData)

                return@coroutineScope flightStatusList.sortedByDescending { it.expectTime }
            }
    }
}

interface FlightRepository {
    suspend fun getArrivalFlights(): List<FlightStatus>
}
