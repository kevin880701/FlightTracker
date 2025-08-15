package com.lhr.flighttracker.features.flightScheduled.data.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// airline2.json 的整體結構
typealias AirlineDataResponse = List<TableContainer<AirlineInfo>>

@JsonClass(generateAdapter = true)
data class AirlineInfo(
    @Json(name = "AirlineIATA") val airlineIATA: String,
    @Json(name = "AirlineICAO") val airlineICAO: String,
    @Json(name = "AirlineChineseAlias") val chineseAlias: String,
    @Json(name = "AirlineEnglishName") val englishName: String?,
    @Json(name = "AirlineJapaneseAlias") val japaneseAlias: String?,
    @Json(name = "AirlineKoreanAlias") val koreanAlias: String?
)