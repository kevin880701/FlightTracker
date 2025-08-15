package com.lhr.flighttracker.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lhr.flighttracker.core.room.entity.TrackedFlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedFlightDao {
    @Query("SELECT * FROM tracked_flights ORDER BY expectTime DESC")
    fun getTrackedFlights(): Flow<List<TrackedFlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun trackFlight(trackedFlight: TrackedFlightEntity)

    @Query("DELETE FROM tracked_flights WHERE flightId = :flightId")
    suspend fun untrackFlight(flightId: String)
}