package com.lhr.flighttracker.features.flightScheduled.domain.usecase

import com.lhr.flighttracker.features.flightScheduled.domain.entity.FlightStatus
import com.lhr.flighttracker.features.flightScheduled.data.repositories.FlightRepository
import javax.inject.Inject

class GetFlightScheduleUseCase @Inject constructor(
    private val repository: FlightRepository
) {
    suspend operator fun invoke(): Result<List<FlightStatus>> {
        return try {
            Result.success(repository.getArrivalFlights())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}