package com.lhr.flighttracker.features.floorplan.domain.entity

import androidx.compose.ui.geometry.Offset

/**
 * 地圖邊界和座標系統定義
 */
data class MapBounds(
    /** 地圖東北角GPS座標 */
    val northEast: GpsCoordinate,

    /** 地圖西南角GPS座標 */
    val southWest: GpsCoordinate,

    /** 地圖圖片寬度（像素） */
    val mapWidth: Float,

    /** 地圖圖片高度（像素） */
    val mapHeight: Float
) {
    /**
     * 將GPS座標轉換為像素座標
     */
    fun gpsToPixel(gpsCoordinate: GpsCoordinate): Offset {
        val latRange = northEast.latitude - southWest.latitude
        val lonRange = northEast.longitude - southWest.longitude

        val x = ((gpsCoordinate.longitude - southWest.longitude) / lonRange * mapWidth).toFloat()
        val y = ((northEast.latitude - gpsCoordinate.latitude) / latRange * mapHeight).toFloat()

        return Offset(x, y)
    }

    /**
     * 將像素座標轉換為GPS座標
     */
    fun pixelToGps(pixelCoordinate: Offset): GpsCoordinate {
        val latRange = northEast.latitude - southWest.latitude
        val lonRange = northEast.longitude - southWest.longitude

        val longitude = southWest.longitude + (pixelCoordinate.x / mapWidth * lonRange)
        val latitude = northEast.latitude - (pixelCoordinate.y / mapHeight * latRange)

        return GpsCoordinate(latitude, longitude)
    }
}