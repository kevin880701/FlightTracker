package com.lhr.flighttracker.features.flightScheduled.data.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TableContainer<T>(
    @Json(name = "type") val type: String,
    @Json(name = "name") val name: String?,
    @Json(name = "data") val data: List<T>?
)