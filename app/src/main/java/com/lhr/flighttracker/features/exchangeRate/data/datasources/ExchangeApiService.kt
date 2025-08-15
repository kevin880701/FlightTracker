package com.lhr.flighttracker.features.exchangeRate.data.datasources

import com.lhr.flighttracker.features.exchangeRate.data.models.response.ExchangeData
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeApiService {

    @GET("v1/latest")
    suspend fun getLatestRates(
        @Query("apikey") apiKey: String,
        @Query("base_currency") baseCurrency: String? = null,
        @Query("currencies") currencies: String? = null
    ): ExchangeData
}