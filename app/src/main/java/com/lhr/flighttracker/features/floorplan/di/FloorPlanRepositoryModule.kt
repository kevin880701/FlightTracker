package com.lhr.flighttracker.features.floorplan.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.lhr.flighttracker.features.floorplan.domain.repository.LocationRepository
import com.lhr.flighttracker.features.floorplan.domain.repository.MapViewRepository
import com.lhr.flighttracker.features.floorplan.data.repository.LocationRepositoryImpl
import com.lhr.flighttracker.features.floorplan.data.repository.MapViewRepositoryImpl
import com.lhr.flighttracker.features.floorplan.data.source.LocationProvider
import com.lhr.flighttracker.features.floorplan.data.source.LocationProviderImpl
import com.lhr.flighttracker.features.floorplan.domain.PathfindingService
import com.lhr.flighttracker.features.floorplan.domain.usecase.CalculateMarkersPositionsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * FloorPlan Repository 模組
 * 提供所有 Repository 和基礎服務的依賴注入
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FloorPlanRepositoryModule {

    @Binds
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    abstract fun bindMapViewRepository(
        mapViewRepositoryImpl: MapViewRepositoryImpl
    ): MapViewRepository


    companion object {
        @Provides
        @Singleton
        fun provideLocationProvider(
            @ApplicationContext context: Context
        ): LocationProvider {
            return LocationProviderImpl(context)
        }

        @Provides
        @Singleton
        fun providePathfindingService(): PathfindingService {
            return PathfindingService()
        }

        @Provides
        @Singleton
        fun provideCalculateMarkersPositionsUseCase(
            mapViewRepository: MapViewRepository
        ): CalculateMarkersPositionsUseCase {
            return CalculateMarkersPositionsUseCase(mapViewRepository)
        }
    }
}