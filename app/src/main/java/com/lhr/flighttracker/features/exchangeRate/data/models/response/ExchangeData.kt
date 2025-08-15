package com.lhr.flighttracker.features.exchangeRate.data.models.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeData(
    @Json(name = "data") val data: Map<String, Double>

)