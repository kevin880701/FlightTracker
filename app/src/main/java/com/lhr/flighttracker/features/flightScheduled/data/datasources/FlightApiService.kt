package com.lhr.flighttracker.features.flightScheduled.data.datasources

import com.lhr.flighttracker.features.flightScheduled.data.models.response.AirlineDataResponse
import com.lhr.flighttracker.features.flightScheduled.data.models.response.AirportDataResponse
import com.lhr.flighttracker.features.flightScheduled.data.models.response.RawArrivalFlight
import retrofit2.http.GET

interface FlightApiService {

    // 取得原始航班資料
    @GET("https://ccc.kia.gov.tw/fids/json/web/arr.php")
    suspend fun getRawArrivalFlights(): List<RawArrivalFlight>

    // 取得機場對照表
    @GET("https://www.kia.gov.tw/data/airport2.json")
    suspend fun getAirportData(): AirportDataResponse

    // 取得航空公司對照表
    @GET("https://www.kia.gov.tw/data/airline2.json")
    suspend fun getAirlineData(): AirlineDataResponse
}