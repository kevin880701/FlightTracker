package com.lhr.flighttracker.core.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lhr.flighttracker.features.flightScheduled.domain.entity.LocalizedString

@Entity(tableName = "tracked_flights")
data class TrackedFlightEntity(
    @PrimaryKey
    val flightId: String, // 複合id: "NX658-TPE-2025/08/12 18:00"
    val expectTime: String,
    val realTime: String,
    val airLineName: LocalizedString,
    val airLineCode: String,
    val airLineLogo: String,
    val airLineUrl: String,
    val airLineNum: String,
    val upAirportCode: String,
    val upAirportName: LocalizedString,
    val airPlaneType: String,
    val airBoardingGate: String,
    val airFlyStatus: Int,
    val airFlyDelayCause: String
)