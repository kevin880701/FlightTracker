package com.lhr.flighttracker.features.flightScheduled.domain.entity

import androidx.compose.ui.graphics.Color
import com.lhr.flighttracker.core.room.entity.TrackedFlightEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.ui.theme.statusArrived
import com.lhr.flighttracker.core.ui.theme.statusBoarding
import com.lhr.flighttracker.core.ui.theme.statusCancelled
import com.lhr.flighttracker.core.ui.theme.statusDelayed
import com.lhr.flighttracker.core.ui.theme.statusDeparted
import com.lhr.flighttracker.core.ui.theme.statusOnTime
import com.lhr.flighttracker.core.ui.theme.statusUnknown

@JsonClass(generateAdapter = true)
data class FlightStatus(

    @Json(name = "expectTime")
    val expectTime: String,

    @Json(name = "realTime")
    val realTime: String,

    @Json(name = "airLineName")
    val airLineName: LocalizedString,

    @Json(name = "airLineCode")
    val airLineCode: String,

    @Json(name = "airLineLogo")
    val airLineLogo: String,

    @Json(name = "airLineUrl")
    val airLineUrl: String,

    @Json(name = "airLineNum")
    val airLineNum: String,

    @Json(name = "upAirportCode")
    val upAirportCode: String,

    @Json(name = "upAirportName")
    val upAirportName: LocalizedString,

    @Json(name = "airPlaneType")
    val airPlaneType: String,

    @Json(name = "airBoardingGate")
    val airBoardingGate: String,

    @Json(name = "airFlyStatus")
    val airFlyStatus: Int,

    @Json(name = "airFlyDelayCause")
    val airFlyDelayCause: String
){
    fun toTrackedFlightEntity(): TrackedFlightEntity {
        return TrackedFlightEntity(
            flightId = "${this.airLineNum}-${this.upAirportCode}-${this.expectTime}",
            expectTime = this.expectTime,
            realTime = this.realTime,
            airLineName = this.airLineName,
            airLineCode = this.airLineCode,
            airLineLogo = this.airLineLogo,
            airLineUrl = this.airLineUrl,
            airLineNum = this.airLineNum,
            upAirportCode = this.upAirportCode,
            upAirportName = this.upAirportName,
            airPlaneType = this.airPlaneType,
            airBoardingGate = this.airBoardingGate,
            airFlyStatus = this.airFlyStatus,
            airFlyDelayCause = this.airFlyDelayCause
        )
    }

    fun getStatusStringResId(): Int {
        return when (airFlyStatus) {
            0 -> R.string.on_time
            1 -> R.string.delayed
            2 -> R.string.cancelled
            3 -> R.string.departed
            4 -> R.string.arrived
            5 -> R.string.boarding
            else -> R.string.unknown
        }
    }

    /**
     * 根據 airFlyStatus 的整數值回傳對應的顏色。
     *
     * @return 一個 Color 物件。
     */
    fun getStatusColor(): Color {
        return when (airFlyStatus) {
            0 -> statusOnTime
            1 -> statusDelayed
            2 -> statusCancelled
            3 -> statusDeparted
            4 -> statusArrived
            5 -> statusBoarding
            else -> statusUnknown
        }
    }
}