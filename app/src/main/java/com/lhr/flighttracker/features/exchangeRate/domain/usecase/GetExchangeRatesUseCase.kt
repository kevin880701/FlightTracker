package com.lhr.flighttracker.features.exchangeRate.domain.usecase

import com.lhr.flighttracker.features.exchangeRate.data.repositories.ExchangeRateRepository
import javax.inject.Inject

class GetExchangeRatesUseCase @Inject constructor(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(
        baseCurrency: String? = "USD",
        currencies: String? = null
    ): Result<Map<String, Double>> {
        return try {
            val rates = repository.getLatestRates(baseCurrency, currencies)
            Result.success(rates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}