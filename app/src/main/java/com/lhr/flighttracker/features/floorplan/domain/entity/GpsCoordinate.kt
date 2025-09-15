package com.lhr.flighttracker.features.floorplan.domain.entity

/**
 * 用於表示一個地理座標點的資料類別。
 *
 * 緯度和經度，用於精確表示地球上的一個點。
 */
data class GpsCoordinate(
    /**
     * 地理緯度。
     * 單位為度，正值表示北緯，負值表示南緯。
     */
    val latitude: Double,

    /**
     * 地理經度。
     * 單位為度，正值表示東經，負值表示西經。
     */
    val longitude: Double
)