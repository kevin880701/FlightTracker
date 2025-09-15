package com.lhr.flighttracker.features.exchangeRate.di

import com.lhr.flighttracker.features.exchangeRate.data.repositories.ExchangeRateRepository
import com.lhr.flighttracker.features.exchangeRate.data.repositories.ExchangeRateRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExchangeRateRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(
        flightRepositoryImpl: ExchangeRateRepositoryImpl
    ): ExchangeRateRepository
}