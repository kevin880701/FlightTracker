package com.lhr.flighttracker.core.location.domain.model

/**
 * 用於儲存位置和方向感測器數據的資料類別。
 */
data class LocationData(
    /**
     * 地理緯度。
     * 單位為度，正值表示北緯，負值表示南緯。
     */
    val latitude: Double,

    /**
     * 地理經度。
     * 單位為度，正值表示東經，負值表示西經。
     */
    val longitude: Double,

    /**
     * 位置的精度。
     * 單位為公尺。數值越小，表示位置越精確。
     */
    val accuracy: Float,

    /**
     * 裝置的移動方向，也稱為方位角 (Bearing) 或航向 (Course)。
     * 單位為度，範圍從 0.0 到 360.0，表示相對於正北方的順時針角度。
     * 0 = 正北方, 90 = 正東方, 180 = 正南方, 270 = 正西方。
     * 預設值為 0f。
     */
    val azimuth: Float = 0f,

    /**
     * 此位置數據被記錄時的時間戳記。
     * 單位為毫秒 (milliseconds)
     */
    val timestamp: Long = System.currentTimeMillis()
)