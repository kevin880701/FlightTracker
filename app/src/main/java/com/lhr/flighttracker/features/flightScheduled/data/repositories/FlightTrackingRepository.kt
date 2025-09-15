package com.lhr.flighttracker.features.flightScheduled.data.repositories

import com.lhr.flighttracker.core.room.dao.TrackedFlightDao
import com.lhr.flighttracker.core.room.entity.TrackedFlightEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightTrackingRepository @Inject constructor(
    private val trackedFlightDao: TrackedFlightDao
) {
    fun getTrackedFlights(): Flow<List<TrackedFlightEntity>> = trackedFlightDao.getTrackedFlights()

    suspend fun trackFlight(flight: TrackedFlightEntity) {
        trackedFlightDao.trackFlight(flight)
    }

    suspend fun untrackFlight(flightId: String) {
        trackedFlightDao.untrackFlight(flightId)
    }
}