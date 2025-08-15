package com.lhr.flighttracker.core.di

import android.content.Context
import androidx.room.Room
import com.lhr.flighttracker.core.room.AppDatabase
import com.lhr.flighttracker.core.room.dao.TrackedFlightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flight_tracker_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackedFlightDao(appDatabase: AppDatabase): TrackedFlightDao {
        return appDatabase.trackedFlightDao()
    }
}