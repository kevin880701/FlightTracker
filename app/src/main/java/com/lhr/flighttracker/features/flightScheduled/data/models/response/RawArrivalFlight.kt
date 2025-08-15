package com.lhr.flighttracker.features.flightScheduled.data.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawArrivalFlight(
    @Json(name = "FDATE") val flightDate: String?,
    @Json(name = "airLineIATA") val airlineIATA: String?,
    @Json(name = "airLineNum") val flightNumber: String?,
    @Json(name = "airPlaneType") val airplaneType: String?,
    @Json(name = "statusarr") val arrivalStatus: String?,
    @Json(name = "notearr") val arrivalNote: String?,
    @Json(name = "STA") val scheduledTime: String?,
    @Json(name = "ATA") val actualTime: String?,
    @Json(name = "arrtime") val estimatedTime: String?,
    @Json(name = "Bay") val bay: String?,
    @Json(name = "DepartureAirportIATA") val departureAirportIATA: String?
)