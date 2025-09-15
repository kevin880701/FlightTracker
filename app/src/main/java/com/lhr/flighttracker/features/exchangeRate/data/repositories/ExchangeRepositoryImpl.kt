package com.lhr.flighttracker.features.exchangeRate.data.repositories

import com.lhr.flighttracker.features.exchangeRate.data.datasources.ExchangeApiService
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val exchangeApiService: ExchangeApiService
) : ExchangeRateRepository {

    private val apiKey = "fca_live_Ue7nhDe9E1wJjrynbzBzv2PAqLyjAJ7rFn0cr0ee"

    override suspend fun getLatestRates(
        baseCurrency: String?,
        currencies: String?
    ): Map<String, Double> {
        return exchangeApiService.getLatestRates(
            apiKey = apiKey,
            baseCurrency = baseCurrency,
            currencies = currencies
        ).data
    }
}

interface ExchangeRateRepository {
    suspend fun getLatestRates(
        baseCurrency: String? = "USD",
        currencies: String? = null
    ): Map<String, Double>
}



