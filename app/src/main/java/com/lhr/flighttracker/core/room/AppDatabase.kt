package com.lhr.flighttracker.core.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lhr.flighttracker.core.room.convert.Converters
import com.lhr.flighttracker.core.room.dao.TrackedFlightDao
import com.lhr.flighttracker.core.room.entity.TrackedFlightEntity

@Database(entities = [TrackedFlightEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedFlightDao(): TrackedFlightDao
}
