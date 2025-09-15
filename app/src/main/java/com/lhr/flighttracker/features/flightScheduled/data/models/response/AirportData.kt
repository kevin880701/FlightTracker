package com.lhr.flighttracker.features.flightScheduled.data.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias AirportDataResponse = List<TableContainer<AirportInfo>>

@JsonClass(generateAdapter = true)
data class AirportInfo(
    @Json(name = "IATA") val iata: String,
    @Json(name = "ICAO") val icao: String,
    @Json(name = "BN") val chineseName: String,
    @Json(name = "EnglishBN") val englishName: String,
    @Json(name = "JapaneseBN") val japaneseName: String?,
    @Json(name = "KoreanBN") val koreanName: String?
)