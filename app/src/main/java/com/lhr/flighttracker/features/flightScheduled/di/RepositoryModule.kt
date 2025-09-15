package com.lhr.flighttracker.features.flightScheduled.di

import com.lhr.flighttracker.features.flightScheduled.data.repositories.FlightRepository
import com.lhr.flighttracker.features.flightScheduled.data.repositories.FlightRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFlightRepository(
        flightRepositoryImpl: FlightRepositoryImpl
    ): FlightRepository
}